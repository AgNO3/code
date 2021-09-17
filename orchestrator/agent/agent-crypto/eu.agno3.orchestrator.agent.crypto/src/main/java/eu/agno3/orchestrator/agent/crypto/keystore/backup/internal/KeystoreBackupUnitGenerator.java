/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.backup.internal;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.crypto.keystore.backup.KeystoreBackupUnit;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = BackupUnitGenerator.class )
public class KeystoreBackupUnitGenerator implements BackupUnitGenerator<KeystoreBackupUnit> {

    /**
     * 
     */
    private static final String PIN_TXT = "pin.txt"; //$NON-NLS-1$
    private static final String VALIDATION_TRUSTSTORE = "validation.truststore"; //$NON-NLS-1$
    private static final String KEYSTORE_SERVICE = "keystore-softhsm"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(KeystoreBackupUnitGenerator.class);

    private Path keystorePath = Paths.get("/etc/keystores/"); //$NON-NLS-1$

    private KeystoresManager ksManager;
    private ServiceSystem serviceSystem;


    @Reference
    protected synchronized void setKeystoresManager ( KeystoresManager ksm ) {
        this.ksManager = ksm;
    }


    protected synchronized void unsetKeystoresManager ( KeystoresManager ksm ) {
        if ( this.ksManager == ksm ) {
            this.ksManager = null;
        }
    }


    @Reference
    protected synchronized void setServiceSystem ( ServiceSystem ss ) {
        this.serviceSystem = ss;
    }


    protected synchronized void unsetServiceSystem ( ServiceSystem ss ) {
        if ( this.serviceSystem == ss ) {
            this.serviceSystem = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#getUnitType()
     */
    @Override
    public Class<KeystoreBackupUnit> getUnitType () {
        return KeystoreBackupUnit.class;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#backup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.nio.file.Path)
     */
    @Override
    public void backup ( ServiceStructuralObject service, KeystoreBackupUnit unit, Path tempDir ) throws BackupException {
        if ( !Files.exists(getKeystorePath()) ) {
            return;
        }

        try {
            for ( Path t : Files.list(getKeystorePath()).collect(Collectors.toList()) ) {
                Path relativize = getKeystorePath().relativize(t);
                if ( t.getFileName().toString().charAt(0) == '.' ) { // $NON-NLS-1$
                    Files.copy(t, tempDir.resolve(relativize));
                }
                else if ( Files.isDirectory(t) ) {
                    Path tgt = tempDir.resolve(t.getFileName());
                    if ( !Files.exists(t.resolve("type")) ) { //$NON-NLS-1$
                        continue;
                    }

                    Files.createDirectory(tgt, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
                    if ( log.isDebugEnabled() ) {
                        log.debug("Backup up keystore " + t.getFileName()); //$NON-NLS-1$
                    }
                    backupKeystore(service, unit, t, tgt);
                }
            }
        }
        catch ( IOException e ) {
            throw new BackupException("Failed to backup keystores", e); //$NON-NLS-1$
        }

    }


    /**
     * @param service
     * @param unit
     * @param source
     * @param tempDir
     * @throws IOException
     */
    private static void backupKeystore ( ServiceStructuralObject service, KeystoreBackupUnit unit, Path source, Path tempDir ) throws IOException {
        Files.walkFileTree(source, EnumSet.noneOf(FileVisitOption.class), 10, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {

                if ( dir.equals(source) ) {
                    return FileVisitResult.CONTINUE;
                }

                Path relativize = source.relativize(dir);
                Path resolve = tempDir.resolve(relativize);
                if ( !Files.exists(resolve) ) {
                    Files.createDirectory(resolve, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
                }
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                if ( !attrs.isRegularFile() ) {
                    if ( "libpkcs11.so".equals(file.getFileName().toString()) ) { //$NON-NLS-1$
                        Path relativize = source.relativize(file);
                        Path tgt = tempDir.resolve(relativize);
                        Files.write(tgt, Files.readSymbolicLink(file).toString().getBytes(StandardCharsets.UTF_8));
                    }
                    return FileVisitResult.CONTINUE;
                }

                Path relativize = source.relativize(file);
                Path tgt = tempDir.resolve(relativize);
                Files.copy(file, tgt, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFileFailed ( Path file, IOException exc ) throws IOException {
                throw exc;
            }


            @Override
            public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {
                return FileVisitResult.CONTINUE;
            }

        });
    }


    Path getKeystorePath () {
        return this.keystorePath;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.backup.BackupUnitGenerator#restore(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.jobs.agent.backup.BackupUnit, java.util.zip.ZipFile, java.lang.String,
     *      java.util.SortedSet)
     */
    @Override
    public void restore ( ServiceStructuralObject service, KeystoreBackupUnit unit, ZipFile data, String prefix, SortedSet<String> unitFiles )
            throws BackupException {

        Map<String, SortedSet<String>> perKeystoreFiles = new HashMap<>();
        for ( String file : unitFiles ) {
            int sepPos = file.indexOf('/');
            if ( sepPos < 0 ) {
                continue;
            }
            String ks = file.substring(0, sepPos);
            if ( !perKeystoreFiles.containsKey(ks) ) {
                perKeystoreFiles.put(ks, new TreeSet<>());
            }
            perKeystoreFiles.get(ks).add(file.substring(sepPos + 1));
        }

        for ( Entry<String, SortedSet<String>> entry : perKeystoreFiles.entrySet() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Restore keystore " + entry.getKey()); //$NON-NLS-1$
            }

            doRestoreKeystore(data, prefix, entry.getKey(), entry.getValue());
        }
    }


    /**
     * @param data
     * @param prefix
     * @param entry
     * @throws BackupException
     */
    void doRestoreKeystore ( ZipFile data, String prefix, String ksName, SortedSet<String> files ) throws BackupException {
        UserPrincipal keystoreUser;
        GroupPrincipal keystoreGroup;

        try {
            String type = readKeystoreFile(data, prefix, ksName, "type"); //$NON-NLS-1$
            if ( !"SoftHSM".equals(type) ) { //$NON-NLS-1$
                log.error(String.format("Unsupported keystore type %s for %s", type, ksName)); //$NON-NLS-1$
                return;
            }

            if ( !this.ksManager.hasKeyStore(ksName) ) {
                log.info("Creating keystore " + ksName); //$NON-NLS-1$
                this.ksManager.createKeyStore(ksName, files.contains(".internal")); //$NON-NLS-1$
            }

            try ( KeystoreManager keyStoreManager = this.ksManager.getKeyStoreManager(ksName) ) {
                restoreValidationTrustStore(data, prefix, ksName, files, keyStoreManager);
                keystoreUser = keyStoreManager.getKeystoreUser();
                keystoreGroup = keyStoreManager.getKeystoreGroup();
            }

            Path ksBase = this.keystorePath.resolve(ksName);

            if ( !Files.exists(ksBase) ) {
                throw new BackupException("Keystore does not exist after initialization " + ksName); //$NON-NLS-1$
            }

            Service s = this.serviceSystem.getService(KEYSTORE_SERVICE, ksName);
            if ( FileSecurityUtils.isRunningAsRoot() ) {
                s.stop();
            }
            restoreKeystoreSecureData(data, files, prefix + '/' + ksName, keystoreUser, null, ksBase);
            restorePin(data, prefix, ksName, files, keystoreUser, keystoreGroup, ksBase);
            if ( FileSecurityUtils.isRunningAsRoot() ) {
                s.start();
            }
        }
        catch (
            KeystoreManagerException |
            IOException |
            ServiceException e ) {
            throw new BackupException("Failed to recreate keystore", e); //$NON-NLS-1$
        }
    }


    /**
     * @param data
     * @param prefix
     * @param ksName
     * @param files
     * @param keystoreUser
     * @param keystoreGroup
     * @param ksBase
     * @throws IOException
     */
    void restorePin ( ZipFile data, String prefix, String ksName, SortedSet<String> files, UserPrincipal keystoreUser, GroupPrincipal keystoreGroup,
            Path ksBase ) throws IOException {
        if ( files.contains(PIN_TXT) ) { // $NON-NLS-1$
            byte[] pin = readKeystoreFileBinary(data, prefix, ksName, PIN_TXT); // $NON-NLS-1$
            Path tgt = ksBase.resolve(PIN_TXT);

            Path p = FileTemporaryUtils.createRelatedTemporaryFile(tgt);
            try {
                Files.write(p, pin, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                PosixFileAttributeView attrs = Files.getFileAttributeView(p, PosixFileAttributeView.class);
                attrs.setPermissions(FileSecurityUtils.getGroupReadFilePermissions());
                if ( keystoreGroup != null ) {
                    attrs.setGroup(keystoreGroup);
                }
                if ( keystoreUser != null ) {
                    attrs.setOwner(keystoreUser);
                }
                FileUtil.safeMove(p, tgt, true);
            }
            finally {
                Files.deleteIfExists(p);
            }
        }
    }


    /**
     * @param files
     * @param keystoreUser
     * @param keystoreGroup
     * @param ksBase
     * @throws IOException
     */
    void restoreKeystoreSecureData ( ZipFile zf, SortedSet<String> files, String prefix, UserPrincipal keystoreUser, GroupPrincipal keystoreGroup,
            Path ksBase ) throws IOException {
        Path secureTarget = ksBase.resolve("secure"); //$NON-NLS-1$
        Path secureTemp = FileTemporaryUtils.createRelatedTemporaryDirectory(secureTarget);
        try {
            for ( String file : files ) {
                if ( file.startsWith("secure/") ) { //$NON-NLS-1$
                    String relname = file.substring(7);
                    Path tmpPath = secureTemp.resolve(relname);
                    ensureDirectory(tmpPath.getParent(), keystoreUser, keystoreGroup);
                    copyTo(zf, zf.getEntry(prefix + '/' + file), tmpPath);
                    ensureSecurePermissions(tmpPath, false, keystoreUser, keystoreGroup);
                }
            }
            ensureSecurePermissions(secureTemp, true, keystoreUser, keystoreGroup);
            Path secureBackup = secureTarget.resolveSibling("secure.bak"); //$NON-NLS-1$
            if ( Files.exists(secureTarget) ) {
                if ( Files.isDirectory(secureBackup) ) {
                    FileUtils.deleteDirectory(secureBackup.toFile());
                }
                FileUtil.safeMove(secureTarget, secureBackup, true);
            }
            try {
                FileUtil.safeMove(secureTemp, secureTarget, false);
            }
            catch ( IOException e ) {
                log.error("Failed to restore keystore data", e); //$NON-NLS-1$
                FileUtil.safeMove(secureBackup, secureTarget, true);
            }
        }
        finally {
            FileUtils.deleteDirectory(secureTemp.toFile());
        }
    }


    /**
     * @param zf
     * @param entry
     * @param tmpFile
     * @throws IOException
     */
    protected void copyTo ( ZipFile zf, ZipEntry entry, Path tmpFile ) throws IOException {
        try ( FileChannel fc = FileChannel.open(tmpFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
              OutputStream os = Channels.newOutputStream(fc) ) {
            IOUtils.copyLarge(zf.getInputStream(entry), os);
        }
    }


    /**
     * @param tgt
     * @param unit
     */
    private static void ensureDirectory ( Path tgt, UserPrincipal keystoreUser, GroupPrincipal keystoreGroup ) throws IOException {
        if ( Files.isDirectory(tgt) ) {
            ensureSecurePermissions(tgt, true, keystoreUser, keystoreGroup);
            return;
        }
        if ( Files.exists(tgt) ) {
            throw new IOException("Is not a directory but exists " + tgt); //$NON-NLS-1$
        }
        ensureSecurePermissions(
            Files.createDirectory(tgt, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions())),
            true,
            keystoreUser,
            keystoreGroup);
    }


    /**
     * @param path
     * @param keystoreUser
     * @param keystoreGroup
     * @throws IOException
     */
    private static void ensureSecurePermissions ( Path path, boolean dir, UserPrincipal keystoreUser, GroupPrincipal keystoreGroup )
            throws IOException {
        PosixFileAttributeView attrs = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if ( dir ) {
            attrs.setPermissions(FileSecurityUtils.getOwnerOnlyDirPermissions());
        }
        else {
            attrs.setPermissions(FileSecurityUtils.getOwnerOnlyFilePermissions());
        }

        if ( keystoreGroup != null ) {
            attrs.setGroup(keystoreGroup);
        }
        if ( keystoreUser != null ) {
            attrs.setOwner(keystoreUser);
        }
    }


    /**
     * @param data
     * @param prefix
     * @param entry
     * @param keyStoreManager
     * @throws IOException
     * @throws KeystoreManagerException
     */
    void restoreValidationTrustStore ( ZipFile data, String prefix, String ksName, SortedSet<String> files, KeystoreManager keyStoreManager )
            throws IOException, KeystoreManagerException {
        if ( files.contains(VALIDATION_TRUSTSTORE) ) {
            String validationTrustStore = readKeystoreFile(data, prefix, ksName, VALIDATION_TRUSTSTORE);
            keyStoreManager.setValidationTruststoreName(validationTrustStore);
        }
        else {
            keyStoreManager.setValidationTruststoreName(null);
        }
    }


    /**
     * @param data
     * @param prefix
     * @param entry
     * @return
     * @throws IOException
     */
    String readKeystoreFile ( ZipFile data, String prefix, String ksName, String fName ) throws IOException {
        ZipEntry e = data.getEntry(String.format("%s/%s/%s", prefix, ksName, fName)); //$NON-NLS-1$
        return readFile(data, e);
    }


    byte[] readKeystoreFileBinary ( ZipFile data, String prefix, String ksName, String fName ) throws IOException {
        ZipEntry e = data.getEntry(String.format("%s/%s/%s", prefix, ksName, fName)); //$NON-NLS-1$
        return readFileBinary(data, e);
    }


    /**
     * @param data
     * @param e
     * @return
     * @throws IOException
     */
    String readFile ( ZipFile data, ZipEntry e ) throws IOException {
        return new String(readFileBinary(data, e), StandardCharsets.UTF_8).trim();
    }


    /**
     * @param data
     * @param e
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    byte[] readFileBinary ( ZipFile data, ZipEntry e ) throws FileNotFoundException, IOException {
        if ( e == null ) {
            throw new FileNotFoundException();
        }
        byte[] buffer = new byte[1024];
        int pos = 0;
        int read = 0;
        try ( InputStream is = data.getInputStream(e) ) {
            while ( pos < buffer.length && ( read = is.read(buffer, pos, buffer.length - pos) ) >= 0 ) {
                pos += read;
            }
        }
        byte[] dt = Arrays.copyOf(buffer, pos);
        return dt;
    }

}
