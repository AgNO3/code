/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.wrap.internal;


import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CipherUtil;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.wrap.CryptBlob;
import eu.agno3.runtime.crypto.wrap.CryptWrapper;


/**
 * @author mbechler
 *
 */
@Component ( service = CryptWrapper.class )
public class CryptWrapperImpl implements CryptWrapper {

    // would be really nice to use OAEP instead but the PKCS11 provider does not support it :(
    // we could use it if we do the padding removal on our own, but that seems a bit excessive, given that these are
    // random session keys
    // static final String RSA_CIPHER_SPEC = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"; //$NON-NLS-1$
    static final String RSA_CIPHER_SPEC = "RSA/ECB/PKCS1Padding"; //$NON-NLS-1$
    static final String CIPHER_SPEC = "AES/GCM/NoPadding"; //$NON-NLS-1$
    static final BouncyCastleProvider BCPROV = new BouncyCastleProvider();

    private SecureRandomProvider secureRandomProvider;
    private SecureRandom secureRandom;
    private int keySize = 256;
    private int tagLength = 128;


    /**
     * 
     */
    public CryptWrapperImpl () {}


    /**
     * 
     * @param sr
     */
    public CryptWrapperImpl ( SecureRandom sr ) {
        this.secureRandom = sr;
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandomProvider == srp ) {
            this.secureRandomProvider = null;
        }
    }


    /**
     * @return
     * @throws CryptoException
     */
    private static Cipher getSymmetricCipher () throws CryptoException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(CIPHER_SPEC, BCPROV);
        }
        catch (
            NoSuchAlgorithmException |
            NoSuchPaddingException e ) {
            throw new CryptoException("Failed to get token cipher", e); //$NON-NLS-1$
        }
        return cipher;
    }


    /**
     * @return the secureRandom
     */
    public synchronized SecureRandom getSecureRandom () {
        if ( this.secureRandom == null ) {
            this.secureRandom = this.secureRandomProvider.getSecureRandom();
        }
        return this.secureRandom;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.wrap.CryptWrapper#wrap(byte[], java.security.PublicKey[])
     */
    @Override
    public CryptBlob wrap ( byte[] data, PublicKey... recipients ) throws CryptoException {

        try {
            Cipher cipher = getSymmetricCipher();
            SecureRandom sr = getSecureRandom();
            byte key[] = new byte[this.keySize / 8];
            byte iv[] = new byte[CipherUtil.getIVLength(cipher)];
            sr.nextBytes(key);
            sr.nextBytes(iv);
            SecretKeySpec sk = new SecretKeySpec(key, "AES"); //$NON-NLS-1$
            cipher.init(Cipher.ENCRYPT_MODE, sk, new GCMParameterSpec(this.tagLength, iv));

            Map<byte[], byte[]> encKeys = new HashMap<>();
            byte[] encrypted = cipher.doFinal(data);
            for ( PublicKey pk : recipients ) {
                Cipher c = Cipher.getInstance(RSA_CIPHER_SPEC); // $NON-NLS-1$
                c.init(Cipher.WRAP_MODE, pk);
                byte[] encKey = c.wrap(sk);
                encKeys.put(pk.getEncoded(), encKey);
            }

            return new CryptBlob(iv, encrypted, encKeys);
        }
        catch ( GeneralSecurityException e ) {
            throw new CryptoException("Failed to produce wrapped data", e); //$NON-NLS-1$
        }
    }

}
