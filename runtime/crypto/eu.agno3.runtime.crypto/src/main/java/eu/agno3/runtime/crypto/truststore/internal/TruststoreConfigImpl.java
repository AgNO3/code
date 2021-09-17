/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertStore;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.openssl.OpenSSLCRLCertStore;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.crypto.truststore.TruststoreConfig;
import eu.agno3.runtime.crypto.truststore.TruststoresConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfigReader;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    TruststoreConfig.class, TrustConfiguration.class
}, configurationPid = TruststoreConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TruststoreConfigImpl extends TrustConfigurationImpl implements TruststoreConfig {

    private static final Logger log = Logger.getLogger(TruststoreConfigImpl.class);

    private static final String TRUSTSTORE_PASS = "changeit"; //$NON-NLS-1$
    private static final String TRUST_FILE = "jks/trust.jks"; //$NON-NLS-1$
    private static final String CRL_DIR = "opensslCRL/"; //$NON-NLS-1$

    private TruststoresConfig tsConfig;
    private RevocationConfigReader revocationConfigReader;
    private File tsDir;
    private RevocationConfig revocationConfig;
    private Path lockFile;


    /**
     * 
     */
    public TruststoreConfigImpl () {}


    /**
     * @param tsConfig
     * @param id
     */
    public TruststoreConfigImpl ( TruststoresConfig tsConfig, String id ) {
        super(id);
        this.tsConfig = tsConfig;
        this.tsDir = new File(tsConfig.getTruststoreBaseDirectory(), id);
        this.lockFile = new File(this.tsDir, ".lock").toPath(); //$NON-NLS-1$
    }


    @Reference
    protected synchronized void setTruststoresConfig ( TruststoresConfig tsconf ) {
        this.tsConfig = tsconf;
    }


    protected synchronized void unsetTruststoresConfig ( TruststoresConfig tsconf ) {
        if ( this.tsConfig == tsconf ) {
            this.tsConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig#setPKIXParameterFactory(eu.agno3.runtime.crypto.tls.PKIXParameterFactory)
     */
    @Override
    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        super.setPKIXParameterFactory(ppf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig#unsetPKIXParameterFactory(eu.agno3.runtime.crypto.tls.PKIXParameterFactory)
     */
    @Override
    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        super.unsetPKIXParameterFactory(ppf);
    }


    @Reference
    protected synchronized void setRevocationConfigReader ( RevocationConfigReader rcr ) {
        this.revocationConfigReader = rcr;
    }


    protected synchronized void unsetRevocationConfigReader ( RevocationConfigReader rcr ) {
        if ( this.revocationConfigReader == rcr ) {
            this.revocationConfigReader = null;
        }
    }


    protected FileChannel obtainReadLock () throws IOException {
        if ( !Files.exists(this.lockFile, LinkOption.NOFOLLOW_LINKS) && Files.isWritable(this.lockFile.getParent()) ) {
            try ( FileChannel l = FileChannel.open(
                this.lockFile,
                EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"))) ) { //$NON-NLS-1$
            }
            catch ( IOException e ) {
                log.warn("Failed to create lock file", e); //$NON-NLS-1$
            }
        }

        if ( !Files.exists(this.lockFile) || !Files.isReadable(this.lockFile) ) {
            return null;
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


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.crypto.truststore.internal.TrustConfigurationImpl#activate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        try {
            this.parseId(ctx);
            this.parseRevocationParams(ctx);

            this.tsDir = new File(this.tsConfig.getTruststoreBaseDirectory(), this.getId());
            this.lockFile = new File(this.tsDir, ".lock").toPath(); //$NON-NLS-1$

            if ( !this.tsDir.isDirectory() || !this.tsDir.canRead() ) {
                throw new CryptoException("Trust store does not exist " + this.getId()); //$NON-NLS-1$
            }

            loadTrustStore();
        }
        catch ( CryptoException e ) {
            log.info("Failed to load truststore " + this.getId(), e); //$NON-NLS-1$
        }
    }


    /**
     * @throws CryptoException
     */
    @Override
    protected void loadTrustStore () throws CryptoException {
        try {
            @SuppressWarnings ( "resource" )
            FileChannel rl = obtainReadLock();
            try {
                loadTrustStore(new File(this.tsDir, TRUST_FILE), TRUSTSTORE_PASS);
                loadRevocationConfig(new File(this.tsDir, "revocation.properties")); //$NON-NLS-1$
            }
            finally {
                if ( rl != null ) {
                    rl.close();
                }
            }
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to obtain lock for truststore", e); //$NON-NLS-1$
        }
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        log.info("Reloading truststore " + this.getId()); //$NON-NLS-1$
        try {
            reload();
        }
        catch ( CryptoException e ) {
            log.error("Failed to reload truststore", e); //$NON-NLS-1$
        }
    }


    /**
     * @param tsDir2
     * @throws CryptoException
     */
    private void loadRevocationConfig ( File revConfigFile ) throws CryptoException {
        if ( revConfigFile.exists() ) {
            try {
                this.revocationConfig = this.revocationConfigReader.fromFile(revConfigFile);
            }
            catch ( IOException e ) {
                throw new CryptoException("Failed to read revocation config", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.internal.TrustConfigurationImpl#isCheckRevocation()
     */
    @Override
    public boolean isCheckRevocation () {
        return this.revocationConfig != null && ( this.revocationConfig.isCheckCRL() || this.revocationConfig.isCheckOCSP() );
    }


    /**
     * @return revocation config
     */
    @Override
    public RevocationConfig getRevocationConfig () {
        return this.revocationConfig;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     * @see eu.agno3.runtime.crypto.truststore.internal.TrustConfigurationImpl#getExtraCertStores()
     */
    @Override
    public CertStore[] getExtraCertStores () throws CryptoException {
        try {
            return new CertStore[] {
                new OpenSSLCRLCertStore(new File(this.tsDir, CRL_DIR))
            };
        }
        catch ( InvalidAlgorithmParameterException e ) {
            throw new CryptoException("Failed to load CRL dir", e); //$NON-NLS-1$
        }
    }
}
