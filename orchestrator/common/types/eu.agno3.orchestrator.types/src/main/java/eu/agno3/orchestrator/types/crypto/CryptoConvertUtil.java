/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;


/**
 * @author mbechler
 *
 */
public final class CryptoConvertUtil {

    /**
     * 
     */
    private static final String RSA = "RSA"; //$NON-NLS-1$
    private static final String SUN = "SunJSSE"; //$NON-NLS-1$

    private static final int MAX_SIZE_BYTES = 1024;

    private static final int KEY_RSA = 1;

    private static final int KEY_VERSION_0 = 1 << 8;


    /**
     * 
     */
    private CryptoConvertUtil () {}


    /**
     * @param v
     * @return the encoded key
     */
    public static InputStream convertRSAPublicToStream ( RSAPublicKey v ) {
        return new ByteArrayInputStream(convertRSAPublicToBytes(v));
    }


    /**
     * @param v
     * @return an input stream with the encoded key form
     */
    public static byte[] convertRSAPublicToBytes ( RSAPublicKey v ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] m = v.getModulus().toByteArray();
        byte[] exp = v.getPublicExponent().toByteArray();
        try {
            dos.writeInt(KEY_RSA | KEY_VERSION_0);
            dos.writeInt(m.length);
            dos.write(m);
            dos.writeInt(exp.length);
            dos.write(exp);
        }
        catch ( IOException e ) {
            throw new IllegalArgumentException("Failed to write public key", e); //$NON-NLS-1$
        }

        return bos.toByteArray();
    }


    /**
     * @param v
     * @return the read public key
     */
    public static RSAPublicKey convertRSAPublicFromBytes ( byte[] v ) {
        return convertRSAPublicFromStream(new ByteArrayInputStream(v));
    }


    /**
     * @param v
     * @return the read public key
     */
    public static RSAPublicKey convertRSAPublicFromStream ( InputStream v ) {
        try ( DataInputStream dis = new DataInputStream(v) ) {

            int fmt = dis.readInt();
            if ( fmt != ( KEY_RSA | KEY_VERSION_0 ) ) {
                throw new IllegalArgumentException("Invalid key format " + fmt); //$NON-NLS-1$
            }

            byte[] modBytes = new byte[checkSize(dis.readInt())];
            dis.readFully(modBytes);
            byte[] pubExpBytes = new byte[checkSize(dis.readInt())];
            dis.readFully(pubExpBytes);

            return (RSAPublicKey) KeyFactory.getInstance(RSA, SUN)
                    .generatePublic(new RSAPublicKeySpec(new BigInteger(modBytes), new BigInteger(pubExpBytes)));
        }
        catch (
            IOException |
            InvalidKeySpecException |
            NoSuchAlgorithmException |
            NoSuchProviderException e ) {
            throw new IllegalArgumentException("Failed to read public key", e); //$NON-NLS-1$
        }
    }


    /**
     * @param v
     * @return the encoded key
     */
    public static InputStream convertRSAPrivateToStream ( RSAPrivateKey v ) {
        return new ByteArrayInputStream(convertRSAPrivateToBytes(v));
    }


    /**
     * @param v
     * @return an input stream with the encoded key form
     */
    public static byte[] convertRSAPrivateToBytes ( RSAPrivateKey v ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] m = v.getModulus().toByteArray();
        byte[] exp = v.getPrivateExponent().toByteArray();
        try {
            dos.writeInt(KEY_RSA | KEY_VERSION_0);
            dos.writeInt(m.length);
            dos.write(m);
            dos.writeInt(exp.length);
            dos.write(exp);
        }
        catch ( IOException e ) {
            throw new IllegalArgumentException("Failed to write private key", e); //$NON-NLS-1$
        }

        return bos.toByteArray();
    }


    /**
     * @param v
     * @return the read private key
     */
    public static RSAPrivateKey convertRSAPrivateFromBytes ( byte[] v ) {
        return convertRSAPrivateFromStream(new ByteArrayInputStream(v));
    }


    /**
     * @param v
     * @return the read private key
     */
    public static RSAPrivateKey convertRSAPrivateFromStream ( InputStream v ) {
        try ( DataInputStream dis = new DataInputStream(v) ) {

            int fmt = dis.readInt();
            if ( fmt != ( KEY_RSA | KEY_VERSION_0 ) ) {
                throw new IllegalArgumentException("Invalid key format " + fmt); //$NON-NLS-1$
            }

            byte[] modBytes = new byte[checkSize(dis.readInt())];
            dis.readFully(modBytes);
            byte[] privExpBytes = new byte[checkSize(dis.readInt())];
            dis.readFully(privExpBytes);

            return (RSAPrivateKey) KeyFactory.getInstance(RSA, SUN)
                    .generatePrivate(new RSAPrivateKeySpec(new BigInteger(modBytes), new BigInteger(privExpBytes)));
        }
        catch (
            IOException |
            InvalidKeySpecException |
            NoSuchAlgorithmException |
            NoSuchProviderException e ) {
            throw new IllegalArgumentException("Failed to read private key", e); //$NON-NLS-1$
        }
    }


    /**
     * @param v
     * @return the encoded key
     */
    public static InputStream convertRSAKeyPairToStream ( KeyPair v ) {
        checkKeyPair(v);
        return new ByteArrayInputStream(convertRSAKeyPairToBytes(v));
    }


    /**
     * @param v
     */
    private static void checkKeyPair ( KeyPair v ) {

        if ( ! ( v.getPrivate() instanceof RSAPrivateKey ) ) {
            throw new IllegalArgumentException("Not an RSA private key"); //$NON-NLS-1$
        }

        if ( ! ( v.getPublic() instanceof RSAPublicKey ) ) {
            throw new IllegalArgumentException("Not an RSA public key"); //$NON-NLS-1$
        }

        BigInteger privMod = ( (RSAPrivateKey) v.getPrivate() ).getModulus();
        BigInteger pubMod = ( (RSAPublicKey) v.getPublic() ).getModulus();
        if ( privMod.compareTo(pubMod) != 0 ) {
            throw new IllegalArgumentException("Private and public key modulus does not match"); //$NON-NLS-1$
        }

    }


    /**
     * @param v
     * @return an input stream with the encoded key form
     */
    public static byte[] convertRSAKeyPairToBytes ( KeyPair v ) {
        checkKeyPair(v);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] m = ( (RSAPrivateKey) v.getPrivate() ).getModulus().toByteArray();
        byte[] privateExp = ( (RSAPrivateKey) v.getPrivate() ).getPrivateExponent().toByteArray();
        byte[] publicExp = ( (RSAPublicKey) v.getPublic() ).getPublicExponent().toByteArray();
        try {
            dos.writeInt(KEY_RSA | KEY_VERSION_0);
            dos.writeInt(m.length);
            dos.write(m);
            dos.writeInt(privateExp.length);
            dos.write(privateExp);
            dos.writeInt(publicExp.length);
            dos.write(publicExp);
        }
        catch ( IOException e ) {
            throw new IllegalArgumentException("Failed to write key pair", e); //$NON-NLS-1$
        }

        return bos.toByteArray();
    }


    /**
     * @param v
     * @return the read private key
     */
    public static KeyPair convertRSAKeyPairFromBytes ( byte[] v ) {
        return convertRSAKeyPairFromStream(new ByteArrayInputStream(v));
    }


    /**
     * @param v
     * @return the read private key
     */
    public static KeyPair convertRSAKeyPairFromStream ( InputStream v ) {
        try ( DataInputStream dis = new DataInputStream(v) ) {
            int fmt = dis.readInt();
            if ( fmt != ( KEY_RSA | KEY_VERSION_0 ) ) {
                throw new IllegalArgumentException("Invalid key format " + fmt); //$NON-NLS-1$
            }
            byte[] modBytes = new byte[checkSize(dis.readInt())];
            dis.readFully(modBytes);
            byte[] privExpBytes = new byte[checkSize(dis.readInt())];
            dis.readFully(privExpBytes);
            byte[] pubExpBytes = new byte[checkSize(dis.readInt())];
            dis.readFully(pubExpBytes);

            KeyFactory kf = KeyFactory.getInstance(RSA, SUN);

            BigInteger modulus = new BigInteger(modBytes);
            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(modulus, new BigInteger(privExpBytes));
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(modulus, new BigInteger(pubExpBytes));

            return new KeyPair(kf.generatePublic(pubKeySpec), kf.generatePrivate(privKeySpec));
        }
        catch (
            IOException |
            InvalidKeySpecException |
            NoSuchAlgorithmException |
            NoSuchProviderException e ) {
            throw new IllegalArgumentException("Failed to read key pair", e); //$NON-NLS-1$
        }
    }


    /**
     * @param size
     * @return
     */
    private static int checkSize ( int size ) {
        if ( size < 0 || size > MAX_SIZE_BYTES ) {
            throw new IllegalArgumentException("Illegal size in key binary " + size); //$NON-NLS-1$
        }
        return size;
    }


    /**
     * @param v
     * @return the encoded certificate
     */
    public static byte[] convertX509CertificateToBytes ( X509Certificate v ) {
        try {
            return v.getEncoded();
        }
        catch ( CertificateEncodingException e ) {
            throw new IllegalArgumentException("Illegal certificate", e); //$NON-NLS-1$
        }
    }


    /**
     * @param v
     * @return the read certificate
     */
    public static X509Certificate convertX509CertificateFromBytes ( byte[] v ) {
        return convertX509CertificateFromStream(new ByteArrayInputStream(v));

    }


    /**
     * @param v
     * @return the read certificate
     */
    public static X509Certificate convertX509CertificateFromStream ( InputStream v ) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X509"); //$NON-NLS-1$
            return (X509Certificate) factory.generateCertificate(v);
        }
        catch ( CertificateException e ) {
            throw new IllegalArgumentException("Illegal encoded certificate", e); //$NON-NLS-1$
        }
    }


    /**
     * @param publicKey
     * @return public key
     */
    public static byte[] convertPublicToBytes ( PublicKey publicKey ) {
        if ( publicKey instanceof RSAPublicKey ) {
            return convertRSAPublicToBytes((RSAPublicKey) publicKey);
        }
        throw new IllegalArgumentException("Unsupported key type"); //$NON-NLS-1$
    }


    /**
     * @param bytes
     * @return public key
     */
    public static PublicKey convertPublicFromBytes ( byte[] bytes ) {
        return convertRSAPublicFromBytes(bytes);
    }


    /**
     * @param binaryStream
     * @return public key
     */
    public static PublicKey convertPublicFromStream ( InputStream binaryStream ) {
        return convertRSAPublicFromStream(binaryStream);
    }

}
