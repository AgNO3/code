/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2016 by mbechler
 */
package eu.agno3.runtime.crypto.ssh;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.codec.binary.Base64;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public final class OpenSSHKeyCodec {

    /**
     * 
     */
    private static final String SSH_RSA = "ssh-rsa"; //$NON-NLS-1$
    private static final int MAX_DATA_LENGTH = 8192;


    /**
     * 
     */
    private OpenSSHKeyCodec () {}


    /**
     * 
     * @param data
     * @return decoded key spec
     * @throws CryptoException
     */
    public static RSAPublicKeySpec decodeRSAPublic ( String data ) throws CryptoException {
        try {
            return decodeRSAPublic(Base64.decodeBase64(data));
        }
        catch ( IllegalArgumentException e ) {
            throw new CryptoException("Failed to decode SSH public key", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param data
     * @return decoded key spec
     * @throws CryptoException
     */
    public static RSAPublicKeySpec decodeRSAPublic ( byte[] data ) throws CryptoException {
        return decodeRSAPublic(new ByteArrayInputStream(data));
    }


    /**
     * @param in
     * @return decoded key spec
     * @throws CryptoException
     */
    public static RSAPublicKeySpec decodeRSAPublic ( ByteArrayInputStream in ) throws CryptoException {
        DataInputStream dis = new DataInputStream(in);
        try {
            String type = readString(dis);
            if ( !SSH_RSA.equals(type) ) {
                throw new CryptoException("Unsupported type " + type); //$NON-NLS-1$
            }
            BigInteger exp = readBigInteger(dis);
            BigInteger mod = readBigInteger(dis);
            if ( dis.read() >= 0 ) {
                throw new CryptoException("Excess data in SSH public key spec"); //$NON-NLS-1$
            }
            return new RSAPublicKeySpec(mod, exp);
        }
        catch (
            IOException |
            IllegalArgumentException e ) {
            throw new CryptoException("Failed to parse SSH public key"); //$NON-NLS-1$
        }
    }


    /**
     * @param pub
     * @return encoded publci key
     * @throws CryptoException
     */
    public static byte[] encodeRSAPublic ( RSAPublicKey pub ) throws CryptoException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            byte[] type = SSH_RSA.getBytes(StandardCharsets.US_ASCII);
            dos.writeInt(type.length);
            dos.write(type);
            writeBigInteger(dos, pub.getPublicExponent());
            writeBigInteger(dos, pub.getModulus());
            return bos.toByteArray();
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to produce encoded RSA public key", e); //$NON-NLS-1$
        }
    }


    /**
     * @param pk
     * @return key type string
     * @throws CryptoException
     */
    public static Object getKeyType ( PublicKey pk ) throws CryptoException {
        if ( pk instanceof RSAPublicKey ) {
            return SSH_RSA;
        }
        throw new CryptoException("Unsupported key type " + pk.getAlgorithm()); //$NON-NLS-1$
    }


    /**
     * @param pk
     * @return pubkey encoded in openssh format
     * @throws CryptoException
     */
    public static byte[] encodePublic ( PublicKey pk ) throws CryptoException {
        if ( pk instanceof RSAPublicKey ) {
            return encodeRSAPublic((RSAPublicKey) pk);
        }
        throw new CryptoException("Unsupported key type " + pk.getAlgorithm()); //$NON-NLS-1$
    }


    /**
     * @param dos
     * @param bi
     * @throws IOException
     */
    private static void writeBigInteger ( DataOutputStream dos, BigInteger bi ) throws IOException {
        byte[] data = bi.toByteArray();
        dos.writeInt(data.length);
        dos.write(data);
    }


    private static String readString ( DataInputStream dis ) throws IOException {
        int len = dis.readInt();
        if ( len < 0 || len > MAX_DATA_LENGTH ) {
            throw new IllegalArgumentException();
        }
        byte strdata[] = new byte[len];
        dis.readFully(strdata);
        return new String(strdata, StandardCharsets.US_ASCII);
    }


    private static BigInteger readBigInteger ( DataInputStream dis ) throws IOException {
        int len = dis.readInt();
        if ( len < 0 || len > MAX_DATA_LENGTH ) {
            throw new IllegalArgumentException();
        }
        byte bidata[] = new byte[len];
        dis.readFully(bidata);
        return new BigInteger(bidata);
    }

}
