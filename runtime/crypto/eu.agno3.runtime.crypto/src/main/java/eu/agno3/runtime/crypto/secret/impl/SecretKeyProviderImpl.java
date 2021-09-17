/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto.secret.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.CryptoKeyNotFoundException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.secret.ExportableSecretWithVersion;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyWithVersion;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SecretKeyProvider.class, configurationPid = "keys" )
public class SecretKeyProviderImpl implements SecretKeyProvider {

    private static final Logger log = Logger.getLogger(SecretKeyProviderImpl.class);
    private static final String CONFIG_DIR_PROPERTY = "config.dir"; //$NON-NLS-1$

    private SecureRandomProvider randProv;
    private SecureRandom random;
    private Map<KeyCacheKey, SecretKeyWithVersion> secretCache = new ConcurrentHashMap<>();
    private Path keyDir;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        try {
            String keyPath = ConfigUtil.parseString(ctx.getProperties(), "keyPath", null); //$NON-NLS-1$
            if ( !StringUtils.isBlank(keyPath) ) {
                Path kd = Paths.get(keyPath);
                Files.createDirectories(kd);
                this.keyDir = kd;
                return;
            }

            String configDirProperty = System.getProperty(CONFIG_DIR_PROPERTY);
            if ( configDirProperty != null ) {
                configDirProperty = configDirProperty.trim();
            }

            if ( configDirProperty != null && !configDirProperty.isEmpty() ) {
                configDirProperty = configDirProperty.replace(
                    "${user.home}", //$NON-NLS-1$
                    System.getProperty("user.home")); //$NON-NLS-1$

                Path kd = Paths.get(configDirProperty, "keys"); //$NON-NLS-1$
                Files.createDirectories(kd);
                this.keyDir = kd;
            }
            else {
                log.error("No config directory specified"); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.error("Failed to initialize key directory", e); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.randProv = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.randProv == srp ) {
            this.randProv = null;
        }
    }


    /**
     * @return the random
     */
    public SecureRandom getRandom () {
        if ( this.random == null ) {
            this.random = this.randProv.getSecureRandom();
        }
        return this.random;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getExportableSecret(java.lang.String, int)
     */
    @Override
    public SecretKeyWithVersion getExportableSecret ( String keyId, int kvno ) throws CryptoKeyNotFoundException {
        return getExporableSecretKeyInternal(keyId, kvno);
    }


    /**
     * @param keyId
     * @param kvno
     * @return
     * @throws CryptoKeyNotFoundException
     */
    private SecretKeyWithVersion getExporableSecretKeyInternal ( String keyId, int kvno ) throws CryptoKeyNotFoundException {
        if ( kvno == -1 ) {
            throw new CryptoKeyNotFoundException("Invalid kvno"); //$NON-NLS-1$
        }
        SecretKeyWithVersion secret = this.secretCache.get(new KeyCacheKey(keyId, kvno));
        if ( secret != null ) {
            return secret;
        }

        return this.loadKey(keyId, kvno);
    }


    /**
     * @param keyId
     * @return
     * @throws CryptoKeyNotFoundException
     */
    private synchronized SecretKeyWithVersion loadKey ( String keyId, int kvno ) throws CryptoKeyNotFoundException {

        SecretKeyWithVersion key;
        try {
            key = tryLoadKey(keyId);
        }
        catch ( IOException e ) {
            throw new CryptoKeyNotFoundException(keyId, e);
        }

        if ( key == null ) {
            throw new CryptoKeyNotFoundException(keyId);
        }

        this.secretCache.put(new KeyCacheKey(keyId, kvno), key);

        return key;
    }


    /**
     * @param keyId
     * @return
     * @throws CryptoKeyNotFoundException
     */
    private Path getKeyIdFile ( String keyId ) throws CryptoKeyNotFoundException {
        if ( this.keyDir == null ) {
            throw new CryptoKeyNotFoundException("Key dir does not exist"); //$NON-NLS-1$
        }
        return this.keyDir.resolve(keyId);
    }


    /**
     * @param keyId
     * @return
     * @throws IOException
     * @throws CryptoKeyNotFoundException
     */
    private ExportableSecretWithVersion tryLoadKey ( String keyId ) throws IOException, CryptoKeyNotFoundException {
        Path kf = getKeyIdFile(keyId);
        if ( !Files.isReadable(kf) ) {
            return null;
        }

        return new ExportableSecretWithVersion(Files.readAllBytes(kf), 0);
    }


    /**
     * @param keyId
     * @param keySize
     * @throws IOException
     * @throws CryptoKeyNotFoundException
     */
    private synchronized SecretKeyWithVersion loadOrCreateKey ( String keyId, int kvno, int keySize ) throws IOException, CryptoKeyNotFoundException {
        SecretKeyWithVersion secret = this.secretCache.get(new KeyCacheKey(keyId, kvno));
        if ( secret != null ) {
            return secret;
        }

        ExportableSecretWithVersion loaded = tryLoadKey(keyId);

        if ( loaded != null ) {
            if ( loaded.getEncoded().length != keySize ) {
                throw new CryptoKeyNotFoundException("keySize mismatch on " + keyId); //$NON-NLS-1$
            }
            this.secretCache.put(new KeyCacheKey(keyId, kvno), loaded);
            return loaded;
        }

        log.info("Creating new random key for " + keyId); //$NON-NLS-1$
        byte[] randKey = new byte[keySize];
        this.getRandom().nextBytes(randKey);
        Path createFile = Files.createFile(getKeyIdFile(keyId), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))); //$NON-NLS-1$
        Files.write(createFile, randKey, StandardOpenOption.WRITE);
        ExportableSecretWithVersion generated = new ExportableSecretWithVersion(randKey, kvno);
        this.secretCache.put(new KeyCacheKey(keyId, kvno), generated);
        return generated;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getSecret(java.lang.String, int, java.lang.String)
     */
    @Override
    public SecretKeyWithVersion getSecret ( String keyId, int kvno, String algo ) throws CryptoKeyNotFoundException {
        return this.getExporableSecretKeyInternal(keyId, kvno);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getOrCreateExportableSecret(java.lang.String, int, int)
     */
    @Override
    public SecretKeyWithVersion getOrCreateExportableSecret ( String keyId, int kvno, int keySize ) throws CryptoException {
        return this.getOrCreateExportableSecretInternal(keyId, kvno, keySize);
    }


    private SecretKeyWithVersion getOrCreateExportableSecretInternal ( String keyId, int kvno, int keySize ) throws CryptoKeyNotFoundException {

        int currentKvno = kvno;
        // TODO: force kvnos to 0 until a proper implementation is done
        if ( currentKvno == -1 ) {
            currentKvno = 0;
        }

        try {
            return this.loadOrCreateKey(keyId, currentKvno, keySize);
        }
        catch ( IOException e ) {
            throw new CryptoKeyNotFoundException(keyId, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getOrCreateSecret(java.lang.String, int, java.lang.String,
     *      int)
     */
    @Override
    public SecretKeyWithVersion getOrCreateSecret ( String keyId, int kvno, String algo, int keySize ) throws CryptoKeyNotFoundException {
        return this.getOrCreateExportableSecretInternal(keyId, kvno, keySize).withAlgo(algo);
    }

    private static class KeyCacheKey {

        private int kvno;
        private String keyId;


        /**
         * @param keyId
         * @param kvno
         * 
         */
        public KeyCacheKey ( String keyId, int kvno ) {
            this.keyId = keyId;
            this.kvno = kvno;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#hashCode()
         */
        // +GENERATED
        @Override
        public int hashCode () {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( this.keyId == null ) ? 0 : this.keyId.hashCode() );
            result = prime * result + this.kvno;
            return result;
        }


        // -GENERATED

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        // +GENERATED
        @Override
        public boolean equals ( Object obj ) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            KeyCacheKey other = (KeyCacheKey) obj;
            if ( this.keyId == null ) {
                if ( other.keyId != null )
                    return false;
            }
            else if ( !this.keyId.equals(other.keyId) )
                return false;
            if ( this.kvno != other.kvno )
                return false;
            return true;
        }
        // -GENERATED

    }
}
