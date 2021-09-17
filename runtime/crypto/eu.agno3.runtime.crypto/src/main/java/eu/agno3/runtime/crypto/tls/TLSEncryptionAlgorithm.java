/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.util.HashMap;
import java.util.Map;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public enum TLSEncryptionAlgorithm {

    AES_256_GCM(256, TLSEncryptionMode.GCM),
    AES_128_GCM(128, TLSEncryptionMode.GCM),
    AES_256_CBC(256, TLSEncryptionMode.CBC),
    AES_128_CBC(128, TLSEncryptionMode.CBC),
    RC4_128(128, TLSEncryptionMode.STREAM),
    _3DES_EDE_CBC(112, TLSEncryptionMode.CBC),
    NULL(0, TLSEncryptionMode.NONE),
    DES_CBC(56, TLSEncryptionMode.CBC),
    RC4_40(40, TLSEncryptionMode.STREAM),
    DES40_CBC(40, TLSEncryptionMode.CBC),
    DES_CBC_40(40, TLSEncryptionMode.CBC);

    /**
     * 
     */
    private static final String _3DES_EDE_CBC_STR = "3DES_EDE_CBC"; //$NON-NLS-1$
    private static final Map<String, TLSEncryptionAlgorithm> OPENSSL_MAP = new HashMap<>();

    static {
        OPENSSL_MAP.put("AES256-GCM", AES_256_GCM); //$NON-NLS-1$
        OPENSSL_MAP.put("AES128-GCM", AES_128_GCM); //$NON-NLS-1$
        OPENSSL_MAP.put("AES256", AES_256_CBC); //$NON-NLS-1$
        OPENSSL_MAP.put("AES128", AES_128_CBC); //$NON-NLS-1$
        OPENSSL_MAP.put("RC4", RC4_128); //$NON-NLS-1$
        OPENSSL_MAP.put("DES-CBC3", _3DES_EDE_CBC); //$NON-NLS-1$
        OPENSSL_MAP.put("DES-CBC", DES_CBC); //$NON-NLS-1$
    }

    private int keySize;
    private TLSEncryptionMode mode;


    /**
     * 
     */
    private TLSEncryptionAlgorithm ( int keySize, TLSEncryptionMode mode ) {
        this.keySize = keySize;
        this.mode = mode;
    }


    /**
     * @return the keySize
     */
    public int getKeySize () {
        return this.keySize;
    }


    /**
     * @return the mode
     */
    public TLSEncryptionMode getMode () {
        return this.mode;
    }


    public static TLSEncryptionAlgorithm fromString ( String val ) {
        if ( _3DES_EDE_CBC_STR.equals(val) ) {
            return _3DES_EDE_CBC;
        }

        return valueOf(val);
    }


    public static String toString ( TLSEncryptionAlgorithm algo ) {
        if ( algo == _3DES_EDE_CBC ) {
            return _3DES_EDE_CBC_STR;
        }

        if ( algo == null ) {
            return null;
        }

        return algo.name();
    }


    /**
     * @param string
     * @return
     * @throws CryptoException
     */
    public static TLSEncryptionAlgorithm mapOpenSSL ( String string ) throws CryptoException {
        TLSEncryptionAlgorithm alg = OPENSSL_MAP.get(string);
        if ( alg == null ) {
            throw new CryptoException("Cannot map OpenSSL algo " + string); //$NON-NLS-1$
        }
        return alg;
    }
}
