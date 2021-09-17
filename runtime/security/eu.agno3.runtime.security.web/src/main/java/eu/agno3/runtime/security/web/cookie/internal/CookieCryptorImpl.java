/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.cookie.internal;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CipherUtil;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyWithVersion;
import eu.agno3.runtime.security.web.cookie.CookieCryptor;
import eu.agno3.runtime.security.web.cookie.CookieType;


/**
 * @author mbechler
 *
 */
@Component ( service = CookieCryptor.class )
public class CookieCryptorImpl implements CookieCryptor {

    private static final Logger log = Logger.getLogger(CookieCryptorImpl.class);

    private static final String AES = "AES"; //$NON-NLS-1$
    private static final String MAC = "HmacSHA256"; //$NON-NLS-1$
    private static final String CIPHER = "AES/GCM/NoPadding"; //$NON-NLS-1$

    private static final int DEFAULT_KEY_SIZE = 128;
    private static final String DEFAULT_KEY_ID = "cookie"; //$NON-NLS-1$
    private static final int DEFAULT_TAG_LENGTH = 128;

    private SecretKeyProvider keyProvider;
    private SecureRandomProvider secureRandom;

    private String keyId = DEFAULT_KEY_ID;
    private int keySize = DEFAULT_KEY_SIZE;
    private int tagLength = DEFAULT_TAG_LENGTH;

    private SecureRandom random;


    @Reference
    protected synchronized void setSecretKeyProvider ( SecretKeyProvider skp ) {
        this.keyProvider = skp;
    }


    protected synchronized void unsetSecretKeyProvider ( SecretKeyProvider skp ) {
        if ( this.keyProvider == skp ) {
            this.keyProvider = null;
        }
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandom = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandom == srp ) {
            this.secureRandom = null;
        }
    }


    /**
     * @return
     */
    private SecureRandom getRandom () {
        if ( this.random == null ) {
            this.random = this.secureRandom.getSecureRandom();
        }
        return this.random;
    }


    @Override
    public Cookie encodeCookie ( String name, String value, CookieType t ) throws CryptoException {
        SecretKeyWithVersion key = this.keyProvider.getOrCreateSecret(this.keyId, -1, AES, this.keySize);
        switch ( t ) {
        case ENCRYPT:
            return new Cookie(name, Base64.getEncoder().encodeToString(encryptCookie(key, value)));
        case SIGN:
            return new Cookie(name, value + "#" + key.getVersion() + //$NON-NLS-1$
                    "#" + Base64.getEncoder().encodeToString(signCookie(key, value))); //$NON-NLS-1$
        default:
            throw new CryptoException("Unsupported CookieType " + t); //$NON-NLS-1$
        }
    }


    /**
     * @param value
     * @return
     * @throws CryptoException
     */
    private static byte[] signCookie ( SecretKeyWithVersion key, String value ) throws CryptoException {
        try {
            Mac m = Mac.getInstance(MAC);
            m.init(key);
            return m.doFinal(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (
            NoSuchAlgorithmException |
            InvalidKeyException e ) {
            throw new CryptoException(e);
        }
    }


    /**
     * @param key
     * @param value
     * @return
     * @throws CryptoException
     */
    private byte[] encryptCookie ( SecretKeyWithVersion key, String value ) throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            int bs = CipherUtil.getIVLength(cipher);
            byte[] iv = new byte[bs];
            this.getRandom().nextBytes(iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, makeGCMParameters(iv));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            dos.writeInt(key.getVersion() | ( 1 << 31 ));
            dos.write(iv);

            byte[] enc = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            dos.write(enc);

            return bos.toByteArray();
        }
        catch (
            NoSuchAlgorithmException |
            NoSuchPaddingException |
            InvalidKeyException |
            InvalidAlgorithmParameterException |
            IOException |
            IllegalBlockSizeException |
            BadPaddingException e ) {
            throw new CryptoException("Failed to create cookie", e); //$NON-NLS-1$
        }
    }


    /**
     * @param iv
     * @return
     */
    private AlgorithmParameterSpec makeGCMParameters ( byte[] iv ) {
        return new GCMParameterSpec(this.tagLength, iv);
    }


    @Override
    public String decodeCookie ( Cookie value, CookieType t ) {
        switch ( t ) {
        case ENCRYPT:
            return decryptCookie(value);
        case SIGN:
            return checkCookie(value);
        default:
            throw new IllegalArgumentException("Unsupported CookieType " + t); //$NON-NLS-1$
        }
    }


    /**
     * @param value
     * @return
     */
    private String checkCookie ( Cookie value ) {
        String val = value.getValue();
        if ( StringUtils.isBlank(val) ) {
            return null;
        }
        int sep = val.lastIndexOf('#');
        if ( sep < 0 ) {
            return null;
        }
        int sep2 = val.lastIndexOf('#', sep - 1);
        if ( sep2 < 0 ) {
            return null;
        }
        try {
            String strval = val.substring(0, sep2);
            int kvno = Integer.parseInt(val.substring(sep2 + 1, sep));
            byte[] mac = Base64.getDecoder().decode(val.substring(sep + 1));
            SecretKeyWithVersion secret = this.keyProvider.getSecret(this.keyId, kvno, AES);
            Mac m = Mac.getInstance(MAC);
            m.init(secret);
            byte[] check = m.doFinal(strval.getBytes(StandardCharsets.UTF_8));
            if ( !MessageDigest.isEqual(mac, check) ) {
                log.debug("MAC is not valid"); //$NON-NLS-1$
                return null;
            }
            return strval;
        }
        catch (
            IllegalArgumentException |
            CryptoException |
            NoSuchAlgorithmException |
            InvalidKeyException e ) {
            log.debug("MAC verification failed", e); //$NON-NLS-1$
            return null;
        }

    }


    /**
     * @param value
     * @return
     */
    private String decryptCookie ( Cookie value ) {
        if ( StringUtils.isBlank(value.getValue()) ) {
            return null;
        }

        try {
            byte[] data = Base64.getDecoder().decode(value.getValue());
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

            Cipher cipher = Cipher.getInstance(CIPHER);
            int bs;

            int kvno = dis.readInt();

            // first bit indicates whether we use old (probably not optimal size IV)
            if ( ( kvno & ( 1 << 31 ) ) == 1 ) {
                bs = CipherUtil.getIVLength(cipher);
                kvno &= ~ ( 1 << 31 );
            }
            else {
                bs = cipher.getBlockSize();
            }

            byte[] iv = new byte[bs];
            dis.readFully(iv);

            SecretKeyWithVersion secret = this.keyProvider.getSecret(this.keyId, kvno, AES);
            cipher.init(Cipher.DECRYPT_MODE, secret, makeGCMParameters(iv));
            byte buf[] = new byte[256];
            int r = 0;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ( ( r = dis.read(buf) ) > 0 ) {
                if ( r > 0 ) {
                    bos.write(cipher.update(buf, 0, r));
                }
            }
            bos.write(cipher.doFinal());
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        }
        catch (
            IllegalArgumentException |
            IOException |
            CryptoException |
            NoSuchAlgorithmException |
            NoSuchPaddingException |
            InvalidKeyException |
            InvalidAlgorithmParameterException |
            IllegalBlockSizeException |
            BadPaddingException e ) {
            log.debug("Cookie decryption failed", e); //$NON-NLS-1$
            return null;
        }
    }
}
