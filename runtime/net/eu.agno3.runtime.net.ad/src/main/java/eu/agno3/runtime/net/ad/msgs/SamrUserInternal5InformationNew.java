/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.util.Crypto;
import jcifs.util.Encdec;


/**
 * @author mbechler
 *
 */
public class SamrUserInternal5InformationNew extends SamrUserInformation {

    private byte[] encryptedPassword;

    private byte expired;

    private byte[] salt;


    /**
     * @param encryptedPassword
     * @param salt
     * @param expired
     * 
     */
    public SamrUserInternal5InformationNew ( byte[] encryptedPassword, byte[] salt, byte expired ) {
        if ( encryptedPassword != null ) {
            this.encryptedPassword = Arrays.copyOf(encryptedPassword, encryptedPassword.length);
        }

        if ( salt != null ) {
            this.salt = Arrays.copyOf(salt, salt.length);
        }
        this.expired = expired;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.msgs.SamrUserInformation#getLevel()
     */
    @Override
    public short getLevel () {
        return 26;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer dst ) throws NdrException {
        dst.enc_ndr_short(this.getLevel());
        dst.writeOctetArray(this.encryptedPassword, 0, 516);
        dst.writeOctetArray(this.salt, 0, 16);
        dst.enc_ndr_small(this.expired);
    }


    /**
     * @param random
     * @param password
     * @param sessionKey
     * @return a password change request
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws ShortBufferException
     */
    public static SamrUserInternal5InformationNew makePasswordChangeInfo ( SecureRandom random, String password, byte[] sessionKey )
            throws UnsupportedEncodingException, NoSuchAlgorithmException, ShortBufferException {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return new SamrUserInternal5InformationNew(makeEncryptedPassword(random, password, salt, sessionKey), salt, (byte) 0);
    }


    /**
     * @param random
     * @param password
     * @param salt
     * @param sessionKey
     * @return an encrypted and obfuscated password
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws ShortBufferException
     */
    public static byte[] makeEncryptedPassword ( SecureRandom random, String password, byte[] salt, byte[] sessionKey )
            throws UnsupportedEncodingException, NoSuchAlgorithmException, ShortBufferException {
        byte[] buffer = new byte[516];
        byte[] encrypted = new byte[516];
        byte[] encodedPassword = password.getBytes("UnicodeLittleUnmarked"); //$NON-NLS-1$

        // fill buffer with contents
        System.arraycopy(encodedPassword, 0, buffer, buffer.length - 4 - encodedPassword.length, encodedPassword.length);
        Encdec.enc_uint32le(encodedPassword.length, buffer, buffer.length - 4);

        // here a truncated session key needs to be used
        byte[] truncatedSessionKey = new byte[16];
        System.arraycopy(sessionKey, 0, truncatedSessionKey, 0, truncatedSessionKey.length);

        MessageDigest md5 = Crypto.getMD5();
        md5.update(salt);
        md5.update(truncatedSessionKey);
        byte[] key = md5.digest();

        Cipher encrypt = Crypto.getArcfour(key);
        encrypt.update(buffer, 0, 516, encrypted, 0);
        return encrypted;
    }
}
