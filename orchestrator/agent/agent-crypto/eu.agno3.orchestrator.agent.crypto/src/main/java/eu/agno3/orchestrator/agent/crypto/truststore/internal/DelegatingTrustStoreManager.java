/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader;


/**
 * @author mbechler
 *
 */
public class DelegatingTrustStoreManager implements TruststoreManager {

    private static final Logger log = Logger.getLogger(DelegatingTrustStoreManager.class);

    /**
     * 
     */
    private static final String FAILED_TO_RELEASE_LOCK = "Failed to release lock"; //$NON-NLS-1$

    private TruststoreManager[] managers;
    private File versionFile;
    private Path lockFile;
    private File revocationConfig;

    private RevocationConfigReader revConfigReader;


    /**
     * @param baseDir
     * @param revConfigReader
     * @param managers
     */
    public DelegatingTrustStoreManager ( File baseDir, RevocationConfigReader revConfigReader, TruststoreManager[] managers ) {
        this.revConfigReader = revConfigReader;
        this.managers = Arrays.copyOf(managers, managers.length);
        this.versionFile = new File(baseDir, ".version"); //$NON-NLS-1$
        this.revocationConfig = new File(baseDir, "revocation.properties"); //$NON-NLS-1$
        this.lockFile = new File(baseDir, ".lock").toPath(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#init()
     */
    @Override
    public void init () throws TruststoreManagerException {
        try ( FileChannel wl = obtainWriteLock() ) {
            for ( TruststoreManager tm : this.managers ) {
                tm.init();
            }

            this.updateVersion(0);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#setRevocationConfig(eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig)
     */
    @Override
    public void setRevocationConfig ( RevocationConfig config ) throws TruststoreManagerException {
        try ( FileChannel wl = obtainWriteLock() ) {
            this.revConfigReader.toFile(this.revocationConfig, config);
            Files.setPosixFilePermissions(this.revocationConfig.toPath(), FileSecurityUtils.getWorldReadableFilePermissions());
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#getRevocationConfig()
     */
    @Override
    public RevocationConfig getRevocationConfig () throws TruststoreManagerException {
        try ( FileChannel rl = obtainReadLock() ) {
            return this.revConfigReader.fromFile(this.revocationConfig);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#hasCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public boolean hasCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        try ( FileChannel wl = obtainReadLock() ) {
            boolean foundInSome = false;
            boolean notFoundInSome = false;
            for ( TruststoreManager tm : this.managers ) {
                if ( !tm.hasCertificate(cert) ) {
                    notFoundInSome = true;
                }
                else {
                    foundInSome = true;
                }
            }

            if ( foundInSome && notFoundInSome ) {
                debugInconsitency(cert);
                throw new TruststoreManagerException("Inconsistent trust stores"); //$NON-NLS-1$
            }

            return foundInSome;
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * @param cert
     * @throws TruststoreManagerException
     */
    protected void debugInconsitency ( X509Certificate cert ) throws TruststoreManagerException {
        if ( log.isDebugEnabled() ) {
            log.debug("Inconsistency for " + cert.getSubjectDN().getName()); //$NON-NLS-1$
            for ( TruststoreManager tm : this.managers ) {
                if ( tm.hasCertificate(cert) ) {
                    log.debug("Certificate in trust store " + tm); //$NON-NLS-1$
                }
                else {
                    log.debug("Certificate not in trust store " + tm); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#listCertificates()
     */
    @Override
    public Set<X509Certificate> listCertificates () throws TruststoreManagerException {
        try ( FileChannel wl = obtainReadLock() ) {
            Set<X509Certificate> certs = null;

            for ( TruststoreManager tm : this.managers ) {

                Set<X509Certificate> tmCerts = tm.listCertificates();

                if ( certs == null ) {
                    certs = tmCerts;
                }
                else if ( !certs.equals(tmCerts) ) {
                    throw new TruststoreManagerException("Inconsitent trust stores"); //$NON-NLS-1$
                }
            }

            if ( certs == null ) {
                return Collections.EMPTY_SET;
            }

            return certs;
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#addCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public void addCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        try ( FileChannel wl = obtainWriteLock() ) {
            long version = getVersion();
            checkCertNotExists(cert);
            addToAllStores(cert);
            updateVersion(version + 1);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * @param cert
     * @throws TruststoreManagerException
     * @throws Exception
     */
    private void addToAllStores ( X509Certificate cert ) throws TruststoreManagerException {
        try {
            for ( TruststoreManager tm : this.managers ) {
                tm.addCertificate(cert);
            }
        }
        catch ( Exception e ) {
            for ( TruststoreManager tm : this.managers ) {
                if ( tm.hasCertificate(cert) ) {
                    tm.removeCertificate(cert);
                }
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#deleteAllCertificates()
     */
    @Override
    public void deleteAllCertificates () {
        for ( TruststoreManager tm : this.managers ) {
            tm.deleteAllCertificates();
        }
    }


    /**
     * @param cert
     * @throws TruststoreManagerException
     */
    private void checkCertNotExists ( X509Certificate cert ) throws TruststoreManagerException {
        for ( TruststoreManager tm : this.managers ) {
            if ( tm.hasCertificate(cert) ) {
                throw new TruststoreManagerException("Certificate does already exist"); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#removeCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public void removeCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        try ( FileChannel wl = obtainWriteLock() ) {
            long version = getVersion();

            for ( TruststoreManager tm : this.managers ) {
                if ( tm.hasCertificate(cert) ) {
                    tm.removeCertificate(cert);
                }
            }

            updateVersion(version + 1);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#setCertificates(java.util.Set)
     */
    @Override
    public void setCertificates ( Set<X509Certificate> trustAnchors ) throws TruststoreManagerException {
        try ( FileChannel wl = obtainWriteLock() ) {
            long version = getVersion();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Synchronizing trust store with %d anchors", trustAnchors.size())); //$NON-NLS-1$
            }
            for ( TruststoreManager tm : this.managers ) {
                tm.setCertificates(trustAnchors);
            }
            updateVersion(version + 1);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#updateCRL(java.security.cert.X509CRL)
     */
    @Override
    public void updateCRL ( X509CRL crl ) throws TruststoreManagerException {
        try ( FileChannel wl = obtainWriteLock() ) {
            for ( TruststoreManager tm : this.managers ) {
                tm.updateCRL(crl);
            }
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#listCRLs()
     */
    @Override
    public Set<X509CRL> listCRLs () throws TruststoreManagerException {
        try ( FileChannel rl = obtainReadLock() ) {
            Set<X509CRL> crls = new HashSet<>();
            for ( TruststoreManager tm : this.managers ) {
                crls.addAll(tm.listCRLs());
            }
            return crls;
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException(FAILED_TO_RELEASE_LOCK, e);
        }
    }


    /**
     * @param l
     * @throws TruststoreManagerException
     */
    protected void updateVersion ( long l ) throws TruststoreManagerException {
        try ( FileChannel ch = FileChannel.open(
            this.versionFile.toPath(),
            EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableFilePermissions())); // $NON-NLS-1$
              OutputStream fos = Channels.newOutputStream(ch);
              DataOutputStream dos = new DataOutputStream(fos) ) {
            dos.writeLong(l);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to update version file", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#getVersion()
     */
    @Override
    public long getVersion () throws TruststoreManagerException {
        if ( !this.versionFile.exists() ) {
            return 0;
        }
        try ( DataInputStream dis = new DataInputStream(new FileInputStream(this.versionFile)) ) {
            return dis.readLong();
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to read version file", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#isReadOnly()
     */
    @Override
    public boolean isReadOnly () {
        for ( TruststoreManager tm : this.managers ) {
            if ( tm.isReadOnly() ) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return
     * 
     */
    protected FileChannel obtainWriteLock () throws TruststoreManagerException {
        try {
            FileChannel l = FileChannel.open(
                this.lockFile,
                EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableFilePermissions())); // $NON-NLS-1$
            do {
                try {
                    l.lock(0L, Long.MAX_VALUE, false);
                    break;
                }
                catch ( OverlappingFileLockException e ) {
                    log.trace("Overlapping lock", e); //$NON-NLS-1$
                    continue;
                }
            }
            while ( true );
            return l;
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to get write lock", e); //$NON-NLS-1$
        }
    }


    protected FileChannel obtainReadLock () throws TruststoreManagerException {
        try {
            if ( !Files.exists(this.lockFile, LinkOption.NOFOLLOW_LINKS) ) {
                try ( FileChannel l = FileChannel.open(
                    this.lockFile,
                    EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                    PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableFilePermissions())) ) { // $NON-NLS-1$
                }
                catch ( IOException e ) {
                    log.warn("Failed to create lock file", e); //$NON-NLS-1$
                }
            }
            FileChannel l = FileChannel.open(this.lockFile, StandardOpenOption.READ);
            do {
                try {
                    l.lock(0L, Long.MAX_VALUE, true);
                    break;
                }
                catch ( OverlappingFileLockException e ) {
                    log.trace("Overlapping lock", e); //$NON-NLS-1$
                    continue;
                }
            }
            while ( true );
            return l;
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to get read lock", e); //$NON-NLS-1$
        }
    }
}
