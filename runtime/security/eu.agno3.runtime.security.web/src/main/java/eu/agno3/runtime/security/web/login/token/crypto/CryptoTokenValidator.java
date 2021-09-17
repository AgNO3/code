/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login.token.crypto;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.runtime.crypto.CipherUtil;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyWithVersion;
import eu.agno3.runtime.security.principal.factors.TokenFactor;
import eu.agno3.runtime.security.token.RealmTokenToken;
import eu.agno3.runtime.security.web.login.token.TokenCreationException;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;
import eu.agno3.runtime.util.serialization.FilteredObjectInputStream;


/**
 * @author mbechler
 *
 */
public class CryptoTokenValidator implements TokenGenerator {

    /**
     * 
     */
    private static final String AES = "AES"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(CryptoTokenValidator.class);
    private static final String CIPHER_SPEC = "AES/GCM/NoPadding"; //$NON-NLS-1$
    private static final BouncyCastleProvider BCPROV = new BouncyCastleProvider();

    private SecretKeyProvider keyProvider;
    private String keyId;
    private int keySize;
    private String realm;
    private ClassLoader classLoader;
    private SecureRandom random = new SecureRandom();
    private int tagLength = 128;


    /**
     */
    public CryptoTokenValidator () {
        this(CryptoTokenValidator.class.getClassLoader(), null);

    }


    /**
     * @param random
     */
    public CryptoTokenValidator ( SecureRandom random ) {
        this(CryptoTokenValidator.class.getClassLoader(), random);

    }


    /**
     * @param classLoader
     * @param random
     */
    public CryptoTokenValidator ( ClassLoader classLoader, SecureRandom random ) {
        this.classLoader = classLoader;
        this.random = random;
    }


    /**
     * @return the realm
     */
    public String getRealm () {
        return this.realm;
    }


    /**
     * @param realm
     *            the realm to set
     */
    public void setRealm ( String realm ) {
        this.realm = realm;
    }


    /**
     * @param keyid
     *            the keyid to set
     */
    public void setKeyid ( String keyid ) {
        this.keyId = keyid;
    }


    /**
     * @param keySize
     *            the keySize to set
     */
    public void setKeySize ( int keySize ) {
        this.keySize = keySize;
    }


    /**
     * @param keyProvider
     *            the keyProvider to set
     */
    public void setKeyProvider ( SecretKeyProvider keyProvider ) {
        this.keyProvider = keyProvider;
    }


    /**
     * @param random
     *            the random to set
     */
    public void setRandom ( SecureRandom random ) {
        this.random = random;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.token.TokenGenerator#createToken(java.io.Serializable,
     *      org.joda.time.Duration)
     */
    @Override
    public String createToken ( Serializable tokenData, Duration lifetime ) throws TokenCreationException {
        return this.createToken(tokenData, DateTime.now().plus(lifetime));
    }


    @Override
    public String createToken ( Serializable tokenData, DateTime expires ) throws TokenCreationException {

        if ( this.realm == null || this.keyProvider == null || this.keyId == null ) {
            throw new TokenCreationException("Missing configuration"); //$NON-NLS-1$
        }

        Cipher cipher = getCipher();
        int bs = CipherUtil.getIVLength(cipher);
        byte[] iv = new byte[bs];
        this.random.nextBytes(iv);

        SecretKeyWithVersion key;
        try {
            key = this.keyProvider.getOrCreateSecret(this.keyId, -1, AES, this.keySize);
            cipher.init(Cipher.ENCRYPT_MODE, key, makeParameters(iv));
        }
        catch (
            InvalidKeyException |
            InvalidAlgorithmParameterException |
            CryptoException e ) {
            throw new TokenCreationException("Failed to initialize cipher", e); //$NON-NLS-1$
        }

        try {
            byte[] tokenDataBytes = this.createTokenData(tokenData, expires);
            if ( log.isDebugEnabled() ) {
                log.debug("Data size " + tokenDataBytes.length); //$NON-NLS-1$
            }

            byte[] encrypted = cipher.doFinal(tokenDataBytes);
            byte[] complete = ArrayUtils.addAll(iv, encrypted);

            StringBuilder sb = new StringBuilder();
            sb.append(this.realm);
            sb.append('-');
            sb.append(key.getVersion());
            sb.append('~');
            sb.append(Base64.encodeBase64URLSafeString(complete));
            String tok = sb.toString();
            if ( log.isDebugEnabled() ) {
                log.debug("Issued token " + tok); //$NON-NLS-1$
            }
            return tok;
        }
        catch (
            IllegalBlockSizeException |
            BadPaddingException |
            IOException e ) {
            throw new TokenCreationException("Failed to encrypt token", e); //$NON-NLS-1$
        }
    }


    private GCMParameterSpec makeParameters ( byte[] iv ) {
        return new GCMParameterSpec(this.tagLength, iv);
    }


    /**
     * @return
     * @throws IOException
     */
    private byte[] createTokenData ( Serializable data, DateTime expires ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF(this.realm);

        dos.writeBoolean(expires != null);
        if ( expires != null ) {
            dos.writeLong(expires.getMillis());
        }
        if ( data != null ) {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
        }
        return bos.toByteArray();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.token.TokenGenerator#validate(eu.agno3.runtime.security.token.RealmTokenToken)
     */
    @Override
    public PrincipalCollection validate ( RealmTokenToken tok ) throws AuthenticationException {

        if ( this.realm == null || this.keyProvider == null || this.keyId == null ) {
            throw new AuthenticationException("Missing token configuration"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Validating token " + tok); //$NON-NLS-1$
        }

        String token = tok.getCredentials();
        byte[] byteToken = Base64.decodeBase64(token);
        Cipher cipher = getCipher();
        int bs = tok.isProperIV() ? CipherUtil.getIVLength(cipher) : cipher.getBlockSize();

        if ( byteToken.length < bs ) {
            illegalToken();
            return null;
        }

        byte[] iv = Arrays.copyOfRange(byteToken, 0, bs);
        byte[] rawToken = Arrays.copyOfRange(byteToken, bs, byteToken.length);

        try {
            cipher.init(Cipher.DECRYPT_MODE, this.keyProvider.getSecret(this.keyId, tok.getKvno(), AES), makeParameters(iv));
        }
        catch (
            InvalidKeyException |
            InvalidAlgorithmParameterException |
            CryptoException e ) {
            throw new AuthenticationException("Failed to initialize token cipher", e); //$NON-NLS-1$
        }

        try {
            byte[] decrypted = cipher.doFinal(rawToken);
            return this.unmarshallToken(decrypted, tok);
        }
        catch (
            IllegalBlockSizeException |
            BadPaddingException |
            IOException |
            ClassNotFoundException e ) {
            log.debug("Failed to validate token", e); //$NON-NLS-1$
            illegalToken();
            return null;
        }
    }


    /**
     * @return
     */
    private static Cipher getCipher () {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(CIPHER_SPEC, BCPROV);
        }
        catch (
            NoSuchAlgorithmException |
            NoSuchPaddingException e ) {
            throw new AuthenticationException("Failed to get token cipher", e); //$NON-NLS-1$
        }
        return cipher;
    }


    /**
     * @param decrypted
     * @param tok
     * @return
     * @throws IOException
     * @throws StreamCorruptedException
     * @throws ClassNotFoundException
     */
    private PrincipalCollection unmarshallToken ( byte[] decrypted, RealmTokenToken tok )
            throws StreamCorruptedException, IOException, ClassNotFoundException {
        if ( log.isDebugEnabled() ) {
            log.debug("Data size " + decrypted.length); //$NON-NLS-1$
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(decrypted);
        DataInputStream dis = new DataInputStream(bis);
        String readRealm = dis.readUTF();
        if ( !this.realm.equals(readRealm) || !this.realm.equals(tok.getRealm()) ) {
            illegalToken();
            return null;
        }

        boolean doesExpire = dis.readBoolean();

        DateTime expires = null;
        if ( doesExpire ) {
            expires = new DateTime(dis.readLong());
            if ( expires.isBeforeNow() ) {
                throw new ExpiredCredentialsException();
            }
        }

        Serializable o = null;
        if ( bis.available() > 0 ) {
            try ( ObjectInputStream ois = new FilteredObjectInputStream(bis, this.classLoader) ) {
                o = (Serializable) ois.readObject();
            }
        }

        SimplePrincipalCollection princs = new SimplePrincipalCollection(new TokenPrincipal(tok.getCredentials(), expires, o), this.realm);
        princs.add(new TokenFactor(), this.realm);
        return princs;
    }


    /**
     * 
     */
    private static void illegalToken () {
        throw new IncorrectCredentialsException();
    }
}
