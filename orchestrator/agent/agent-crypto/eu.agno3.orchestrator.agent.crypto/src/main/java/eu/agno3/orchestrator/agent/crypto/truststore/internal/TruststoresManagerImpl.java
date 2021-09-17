/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.crypto.truststore.TruststoresConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    TruststoresManager.class, SystemService.class
} )
@SystemServiceType ( TruststoresManager.class )
public class TruststoresManagerImpl implements TruststoresManager {

    private static final String MARKER_FILE = ".truststore"; //$NON-NLS-1$
    private TruststoresConfig tsConfig;
    private RevocationConfigReader revConfigReader;
    private PKIXParameterFactory pkixParameterFactory;


    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory pf ) {
        this.pkixParameterFactory = pf;
    }


    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory pf ) {
        if ( this.pkixParameterFactory == pf ) {
            this.pkixParameterFactory = null;
        }
    }


    @Reference
    protected synchronized void setTruststoresConfig ( TruststoresConfig tsc ) {
        this.tsConfig = tsc;
    }


    protected synchronized void unsetTruststoresConfig ( TruststoresConfig tsc ) {
        if ( this.tsConfig == tsc ) {
            this.tsConfig = null;
        }
    }


    @Reference
    protected synchronized void setRevocationConfigReader ( RevocationConfigReader rcr ) {
        this.revConfigReader = rcr;
    }


    protected synchronized void unsetRevocationConfigReader ( RevocationConfigReader rcr ) {
        if ( this.revConfigReader == rcr ) {
            this.revConfigReader = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#getKeyStores()
     */
    @Override
    public List<String> getTrustStores () {
        List<String> trustStoreNames = new LinkedList<>();
        for ( File ksDir : this.tsConfig.getTruststoreBaseDirectory().listFiles(new FileFilter() {

            @Override
            public boolean accept ( File pathname ) {
                File trustStore = new File(pathname, "jks/trust.jks"); //$NON-NLS-1$
                return pathname.isDirectory() && trustStore.isFile() && trustStore.canRead();
            }
        }) ) {
            trustStoreNames.add(ksDir.getName());
        }

        return trustStoreNames;
    }


    @Override
    public File getTrustStorePath ( String name ) {
        return new File(this.tsConfig.getTruststoreBaseDirectory(), name);
    }


    @Override
    public boolean isReadOnly ( String name ) {
        File roFile = new File(this.getTrustStorePath(name), ".readonly"); //$NON-NLS-1$
        return roFile.exists();

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager#createTrustStore(java.lang.String)
     */
    @Override
    public void createTrustStore ( String name ) throws TruststoreManagerException {

        if ( this.hasTrustStore(name) ) {
            throw new TruststoreManagerException("Trust store does already exist " + name); //$NON-NLS-1$
        }

        File trustStorePath = this.getTrustStorePath(name);

        try {
            if ( !trustStorePath.exists() ) {
                Files.createDirectory(trustStorePath.toPath(), PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            }
            Files.write(trustStorePath.toPath().resolve(MARKER_FILE), new byte[0], StandardOpenOption.CREATE);
            TruststoreManager manager = this.getTrustStoreManager(name);
            manager.init();

            Iterator<File> iterateFilesAndDirs = FileUtils.iterateFilesAndDirs(trustStorePath, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
            while ( iterateFilesAndDirs.hasNext() ) {
                File f = iterateFilesAndDirs.next();
                if ( f.isDirectory() ) {
                    Files.setPosixFilePermissions(f.toPath(), getTrustStoreDirPerms());
                }
                else {
                    Files.setPosixFilePermissions(f.toPath(), getTrustStoreFilePerms());
                }
            }
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to initialize truststores", e); //$NON-NLS-1$
        }
    }


    /**
     * @param name
     * @return
     */
    protected TruststoreManager[] createManagers ( File trustStorePath ) {
        return new TruststoreManager[] {
            new JKSTruststoreManager(trustStorePath), new OpenSSLTruststoreManager(trustStorePath)
        };
    }


    /**
     * @return
     */
    private static Set<PosixFilePermission> getTrustStoreDirPerms () {
        return PosixFilePermissions.fromString("rwxr-xr-x"); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private static Set<PosixFilePermission> getTrustStoreFilePerms () {
        return PosixFilePermissions.fromString("rw-r--r--"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager#deleteTrustStore(java.lang.String)
     */
    @Override
    public void deleteTrustStore ( String name ) throws TruststoreManagerException {

        if ( !this.hasTrustStore(name) ) {
            throw new TruststoreManagerException("Trust store does not exist " + name); //$NON-NLS-1$
        }

        if ( this.isReadOnly(name) ) {
            throw new TruststoreManagerException("Trust store is readOnly " + name); //$NON-NLS-1$
        }
        try {
            FileUtils.deleteDirectory(this.getTrustStorePath(name));
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to remove trust store", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager#getTrustConfig(java.lang.String, boolean)
     */
    @Override
    public TrustConfiguration getTrustConfig ( String name, boolean checkRevocation ) throws TruststoreManagerException {
        RevocationConfig revocationConfig = null;
        if ( checkRevocation ) {
            revocationConfig = getTrustStoreManager(name).getRevocationConfig();
        }

        return new TruststoreConfigImpl(name, getTrustStore(name), revocationConfig, this.pkixParameterFactory);
    }


    @Override
    public KeyStore getTrustStore ( String name ) throws TruststoreManagerException {
        File path = getTrustStorePath(name);
        JKSTruststoreManager jksTruststoreManager = new JKSTruststoreManager(path);
        return jksTruststoreManager.loadKeyStore();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager#getTrustStoreManager(java.lang.String)
     */
    @Override
    public TruststoreManager getTrustStoreManager ( String name ) throws TruststoreManagerException {
        File path = getTrustStorePath(name);
        TruststoreManager tm = new DelegatingTrustStoreManager(path, this.revConfigReader, createManagers(path));

        if ( this.isReadOnly(name) ) {
            return new ReadOnlyTrustStoreManager(tm);
        }

        return tm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager#hasTrustStore(java.lang.String)
     */
    @Override
    public boolean hasTrustStore ( String name ) {
        File trustStorePath = new File(getTrustStorePath(name), MARKER_FILE);
        return trustStorePath.exists() && trustStorePath.canRead();
    }
}
