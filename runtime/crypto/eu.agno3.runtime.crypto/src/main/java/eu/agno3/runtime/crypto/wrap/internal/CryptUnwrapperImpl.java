/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.wrap.internal;


import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;
import eu.agno3.runtime.crypto.wrap.CryptBlob;
import eu.agno3.runtime.crypto.wrap.CryptUnwrapper;
import eu.agno3.runtime.crypto.wrap.NonRecpientException;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = CryptUnwrapper.class, configurationPid = "cryptUnwrap", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class CryptUnwrapperImpl implements CryptUnwrapper {

    private static final Logger log = Logger.getLogger(CryptUnwrapperImpl.class);

    private X509KeyManager keyManager;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String keyAlias;
    private int tagSize = 128;

    private InternalTLSConfiguration tlsConfig;


    /**
     * 
     */
    public CryptUnwrapperImpl () {}


    /**
     * 
     * @param kp
     */
    public CryptUnwrapperImpl ( KeyPair kp ) {
        this.privateKey = kp.getPrivate();
        this.publicKey = kp.getPublic();
    }


    /**
     * @param km
     * @param alias
     */
    public CryptUnwrapperImpl ( X509KeyManager km, String alias ) {
        this.keyManager = km;
        this.keyAlias = alias;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.keyAlias = ConfigUtil.parseString(ctx.getProperties(), "keyAlias", null); //$NON-NLS-1$
        try {
            getPublicKey();
            getPrivateKey();
        }
        catch ( CryptoException e ) {
            log.error("Key unavailable", e); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setTLSConfiguration ( InternalTLSConfiguration tc ) {
        this.tlsConfig = tc;
    }


    protected synchronized void unsetTLSConfiguration ( InternalTLSConfiguration tc ) {
        if ( this.tlsConfig == tc ) {
            this.tlsConfig = null;
        }
    }


    private X509KeyManager getKeyManager () throws CryptoException {
        if ( this.keyManager != null ) {
            return this.keyManager;
        }
        KeyManager[] kms = this.tlsConfig.getKeyManagers();

        if ( kms == null || kms.length < 1 || ! ( kms[ 0 ] instanceof X509KeyManager ) ) {
            throw new CryptoException("No key manager found"); //$NON-NLS-1$
        }
        return (X509KeyManager) kms[ 0 ];
    }


    private synchronized PublicKey getPublicKey () throws CryptoException {
        if ( this.publicKey != null ) {
            return this.publicKey;
        }
        X509KeyManager km = getKeyManager();
        if ( km == null ) {
            throw new CryptoException("No key manager available"); //$NON-NLS-1$
        }

        String ka = getKeyAlias(km);
        X509Certificate[] certs = km.getCertificateChain(ka);
        if ( certs == null || certs.length == 0 ) {
            throw new CryptoException("Certificate chain does not exist " + ka); //$NON-NLS-1$
        }
        Certificate cert = certs[ 0 ];
        this.publicKey = cert.getPublicKey();
        return this.publicKey;
    }


    /**
     * @param km
     * @return
     */
    private String getKeyAlias ( X509KeyManager km ) {
        String ka = this.keyAlias;

        if ( ka == null ) {
            ka = km.chooseClientAlias(new String[] {
                "RSA" //$NON-NLS-1$
            }, null, null);
        }
        return ka;
    }


    private synchronized PrivateKey getPrivateKey () throws CryptoException {
        if ( this.privateKey != null ) {
            return this.privateKey;
        }
        X509KeyManager km = getKeyManager();
        if ( km == null ) {
            throw new CryptoException("No key manager available"); //$NON-NLS-1$
        }
        String ka = getKeyAlias(km);
        this.privateKey = km.getPrivateKey(ka);
        if ( this.privateKey == null ) {
            throw new CryptoException("Failed to get private key " + ka); //$NON-NLS-1$
        }
        return this.privateKey;
    }


    @Override
    public byte[] unwrap ( CryptBlob blob ) throws CryptoException {
        PublicKey pk = getPublicKey();
        byte[] encpubkey = pk.getEncoded();
        byte[] encKey = null;
        for ( Entry<byte[], byte[]> entry : blob.getKeys().entrySet() ) {
            if ( Arrays.equals(entry.getKey(), encpubkey) ) {
                encKey = entry.getValue();
            }
        }

        if ( encKey == null ) {
            throw new NonRecpientException("Blob not encrypted with any available key"); //$NON-NLS-1$
        }
        try {
            Cipher rsa = Cipher.getInstance(CryptWrapperImpl.RSA_CIPHER_SPEC);
            rsa.init(Cipher.UNWRAP_MODE, getPrivateKey());
            Key unwrap = rsa.unwrap(encKey, "AES", Cipher.SECRET_KEY); //$NON-NLS-1$
            Cipher aes = Cipher.getInstance(CryptWrapperImpl.CIPHER_SPEC);
            aes.init(
                Cipher.DECRYPT_MODE,
                unwrap, // $NON-NLS-1$
                new GCMParameterSpec(this.tagSize, blob.getIv()));
            return aes.doFinal(blob.getEncrypted());
        }
        catch ( GeneralSecurityException e ) {
            throw new CryptoException("Failed to decrypt blob", e); //$NON-NLS-1$
        }
    }
}
