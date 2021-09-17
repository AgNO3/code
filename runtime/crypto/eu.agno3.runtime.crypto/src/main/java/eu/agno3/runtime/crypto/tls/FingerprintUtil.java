/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.ssh.OpenSSHKeyCodec;


/**
 * @author mbechler
 *
 */
public final class FingerprintUtil {

    /**
     * 
     */
    private FingerprintUtil () {}


    /**
     * 
     * @param data
     * @return formatted string for fingerprint
     */
    public static String printFingerprintHex ( byte[] data ) {
        if ( data == null || data.length == 0 ) {
            return StringUtils.EMPTY;
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < data.length; i++ ) {
            if ( !first ) {
                sb.append(':');
            }
            else {
                first = false;
            }
            sb.append(String.format("%02x", data[ i ])); //$NON-NLS-1$
            if ( i > 0 && i % 32 == 0 ) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }


    /**
     * 
     * @param pk
     * @return md5 fingerprint
     * @throws CryptoException
     */
    public static byte[] md5 ( PublicKey pk ) throws CryptoException {
        return fingerprint("MD5", pk); //$NON-NLS-1$
    }


    /**
     * 
     * @param pk
     * @return md5 ssh style fingerprint
     * @throws CryptoException
     */
    public static byte[] md5SSH ( PublicKey pk ) throws CryptoException {
        return fingerprintSSH("MD5", pk); //$NON-NLS-1$
    }


    /**
     * 
     * @param pk
     * @return sha1 fingerprint
     * @throws CryptoException
     */
    public static byte[] sha1 ( PublicKey pk ) throws CryptoException {
        return fingerprint("SHA1", pk); //$NON-NLS-1$
    }


    /**
     * 
     * @param pk
     * @return sha256 fingerprint
     * @throws CryptoException
     */
    public static byte[] sha256 ( PublicKey pk ) throws CryptoException {
        return fingerprint("SHA-256", pk); //$NON-NLS-1$
    }


    /**
     * 
     * @param pk
     * @return sha256/ssh fingerprint
     * @throws CryptoException
     */
    public static byte[] sha256SSH ( PublicKey pk ) throws CryptoException {
        return fingerprintSSH("SHA-256", pk); //$NON-NLS-1$
    }


    /**
     * 
     * @param pk
     * @return sha512 fingerprint
     * @throws CryptoException
     */
    public static byte[] sha512 ( PublicKey pk ) throws CryptoException {
        return fingerprint("SHA-512", pk); //$NON-NLS-1$
    }


    /**
     * @param algo
     * @param pk
     * @return
     * @throws CryptoException
     */
    private static byte[] fingerprint ( String algo, PublicKey pk ) throws CryptoException {
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            return md.digest(pk.getEncoded());
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new CryptoException("Failed to produce public key fingerprint", e); //$NON-NLS-1$
        }
    }


    /**
     * @param algo
     * @param pk
     * @return
     * @throws CryptoException
     */
    private static byte[] fingerprintSSH ( String algo, PublicKey pk ) throws CryptoException {
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            return md.digest(OpenSSHKeyCodec.encodePublic(pk));
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new CryptoException("Failed to produce public key fingerprint", e); //$NON-NLS-1$
        }
    }

}
