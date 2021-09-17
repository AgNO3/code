/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.StateUtils;
import org.apache.myfaces.shared.util.serial.DefaultSerialFactory;
import org.apache.myfaces.shared.util.serial.SerialFactory;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.jcajce.io.CipherOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CipherUtil;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.secret.ConfigSecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyWithVersion;


/**
 * @author mbechler
 *
 */
@Component ( service = SerialFactory.class, configurationPid = "jsf.serial" )
public class SecureSerialFactory extends DefaultSerialFactory {

    private static final Logger log = Logger.getLogger(SecureSerialFactory.class);
    private static final String CIPHER_SPEC = "AES/GCM/NoPadding"; //$NON-NLS-1$
    private static final BouncyCastleProvider BCPROV = new BouncyCastleProvider();

    private SecureRandomProvider randomProvider;
    private SecretKeyProvider keyProvider;
    private SecretKeyProvider overrideKeyProvider;
    private int tagLength = 128;
    private int keySize = 16;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {
        this.overrideKeyProvider = ConfigSecretKeyProvider.create(ctx.getProperties(), "serialKey"); //$NON-NLS-1$
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.randomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.randomProvider == srp ) {
            this.randomProvider = null;
        }
    }


    @Reference
    protected synchronized void setSecretKeyProvider ( SecretKeyProvider skp ) {
        this.keyProvider = skp;
    }


    protected synchronized void unsetSecretKeyProvider ( SecretKeyProvider skp ) {
        if ( this.keyProvider == skp ) {
            this.keyProvider = null;
        }
    }


    /**
     * @return
     */
    protected SecretKeyProvider getKeyProvider () {
        SecretKeyProvider kp = this.overrideKeyProvider;
        if ( kp != null ) {
            return kp;
        }
        return this.keyProvider;
    }


    /**
     * @param kp2
     * @param kvno
     * @return
     * @throws CryptoException
     */
    private SecretKeyWithVersion getSecretKey ( SecretKeyProvider kp, String keyId, int kvno ) throws CryptoException {
        if ( kvno != -1 ) {
            return kp.getSecret(keyId, kvno, "AES"); //$NON-NLS-1$
        }
        return kp.getOrCreateSecret(keyId, kvno, "AES", this.keySize); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private GCMParameterSpec getParameters ( byte[] iv ) {
        return new GCMParameterSpec(this.tagLength, iv);
    }


    /**
     * @return
     */
    private byte[] generateIV ( int size ) {
        byte[] iv = new byte[size];
        this.randomProvider.getSecureRandom().nextBytes(iv);
        return iv;
    }


    /**
     * @param contextName
     * @return
     */
    private static String makeKeyId ( String contextName ) {
        return String.format("JSF.Serial.%s", contextName); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.myfaces.shared.util.serial.DefaultSerialFactory#getObjectInputStream(java.io.InputStream)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public ObjectInputStream getObjectInputStream ( InputStream inputStream ) throws IOException {

        SecretKeyProvider kp = getKeyProvider();
        if ( this.randomProvider == null || kp == null ) {
            throw new IOException("Not properly initialized"); //$NON-NLS-1$
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        InputStream is = inputStream;
        if ( StateUtils.isSecure(ec) ) {
            throw new IOException("Default encryption is enabled"); //$NON-NLS-1$
        }

        DataInputStream dis = new DataInputStream(is);
        int kvno = dis.readInt();
        if ( kvno == -1 ) {
            throw new IOException("Illegal KVNO"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Reading with KVNO " + kvno); //$NON-NLS-1$
        }

        Cipher cipher = getCipher();
        byte[] iv = new byte[CipherUtil.getIVLength(cipher)];
        dis.readFully(iv);
        initCipher(kp, makeKeyId(ec.getContextName()), Cipher.DECRYPT_MODE, cipher, iv, kvno);
        is = new CipherInputStream(dis, cipher);
        return super.getObjectInputStream(is);
    }


    /**
     * @return
     * @throws IOException
     */
    private static Cipher getCipher () throws IOException {
        try {
            return Cipher.getInstance(CIPHER_SPEC, BCPROV);
        }
        catch (
            NoSuchAlgorithmException |
            NoSuchPaddingException e ) {
            throw new IOException("Failed to create cipher", e); //$NON-NLS-1$
        }
    }


    private int initCipher ( SecretKeyProvider kp, String keyId, int mode, Cipher c, byte[] iv, int kvno ) throws IOException {
        try {
            SecretKeyWithVersion secretKey = getSecretKey(kp, keyId, kvno);
            c.init(mode, secretKey, getParameters(iv));
            return secretKey.getVersion();
        }
        catch (
            InvalidKeyException |
            InvalidAlgorithmParameterException |
            CryptoException e ) {
            throw new IOException("Failed to initialize cipher", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.myfaces.shared.util.serial.DefaultSerialFactory#getObjectOutputStream(java.io.OutputStream)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public ObjectOutputStream getObjectOutputStream ( OutputStream outputStream ) throws IOException {

        SecretKeyProvider kp = getKeyProvider();
        if ( this.randomProvider == null || kp == null ) {
            throw new IOException("Not properly initialized"); //$NON-NLS-1$
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        OutputStream os = outputStream;
        DataOutputStream dos = new DataOutputStream(os);

        if ( StateUtils.isSecure(ec) ) {
            throw new IOException("Default encryption is enabled"); //$NON-NLS-1$
        }

        Cipher cipher = getCipher();
        byte[] iv = generateIV(CipherUtil.getIVLength(cipher));
        int kvno = initCipher(kp, makeKeyId(ec.getContextName()), Cipher.ENCRYPT_MODE, cipher, iv, -1);
        if ( log.isDebugEnabled() ) {
            log.debug("Writing with KVNO " + kvno); //$NON-NLS-1$
        }
        dos.writeInt(kvno);
        dos.write(iv);
        os = new CipherOutputStream(dos, cipher);
        return new ClosingObjectOutputStreamWrapper(super.getObjectOutputStream(os));
    }
}
