/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public class TLSCipherSuiteUtil {

    /**
     * 
     * @param type
     * @param cipherSuite
     * @return parsed cipher suite specification
     * @throws CryptoException
     */
    public static TLSCipherSuiteSpec parseSpec ( TLSCipherSuiteSpecType type, String cipherSuite ) throws CryptoException {
        switch ( type ) {
        case JAVA:
            return parseJavaSpec(cipherSuite);
        case OPENSSL:
            return parseOpenSSLSpec(cipherSuite);
        default:
            throw new CryptoException("Unsupported cipher suite spec type " + type); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param cipherSuite
     * @return parsed cipher suite specification
     * @throws CryptoException
     */
    public static TLSCipherSuiteSpec parseOpenSSLSpec ( String cipherSuite ) throws CryptoException {

        if ( cipherSuite.startsWith("SRP") || //$NON-NLS-1$
                cipherSuite.startsWith("PSK") ) { //$NON-NLS-1$
            throw new CryptoException("Unsupported key exchange " + cipherSuite); //$NON-NLS-1$
        }

        String[] components = StringUtils.split(cipherSuite, "-"); //$NON-NLS-1$

        if ( components.length < 2 ) {
            throw new CryptoException("Unsupported cipher format " + cipherSuite); //$NON-NLS-1$
        }

        try {
            String split[] = splitOpenSSL(cipherSuite, components);
            TLSHashAlgorithm hash = TLSHashAlgorithm.mapOpenSSL(components[ components.length - 1 ]);
            TLSKeyAlgorithm keyAlgo = TLSKeyAlgorithm.mapOpenSSL(split[ 0 ]);
            TLSEncryptionAlgorithm encAlgo = TLSEncryptionAlgorithm.mapOpenSSL(split[ 1 ]);

            if ( hash == null || keyAlgo == null || encAlgo == null ) {
                throw new CryptoException();
            }

            return new TLSCipherSuiteSpec(keyAlgo, encAlgo, hash);
        }
        catch ( IllegalArgumentException e ) {
            throw new CryptoException("Failed to parse cipher suite specification " + cipherSuite, e); //$NON-NLS-1$
        }
    }


    /**
     * @param cipherSuite
     * @param components
     * @return
     * @throws CryptoException
     */
    private static String[] splitOpenSSL ( String cipherSuite, String[] components ) throws CryptoException {

        if ( components.length >= 4 && ( cipherSuite.startsWith("ECDHE-") || //$NON-NLS-1$
                cipherSuite.startsWith("ECDH-") || //$NON-NLS-1$
                cipherSuite.startsWith("DHE-") || //$NON-NLS-1$
                cipherSuite.startsWith("EDH-") ) ) { //$NON-NLS-1$

            String algo;
            if ( components.length == 5 ) {
                algo = components[ 2 ] + "-" + components[ 3 ]; //$NON-NLS-1$
            }
            else {
                algo = components[ 2 ];
            }

            return new String[] {
                components[ 0 ] + "-" + components[ 1 ], algo //$NON-NLS-1$
            };
        }
        else if ( components.length == 3 ) {
            return new String[] {
                "RSA", //$NON-NLS-1$
                components[ 0 ] + "-" + components[ 1 ] //$NON-NLS-1$
            };
        }
        else if ( components.length == 2 ) {
            return new String[] {
                "RSA", //$NON-NLS-1$
                components[ 0 ]
            };
        }

        throw new CryptoException("Unsupported cipherSuite " + cipherSuite); //$NON-NLS-1$
    }


    /**
     * 
     * @param cipherSuite
     * @return a parsed cipher suite specification
     * @throws CryptoException
     */
    public static TLSCipherSuiteSpec parseJavaSpec ( String cipherSuite ) throws CryptoException {

        int firstSep = cipherSuite.indexOf('_');
        int lastSep = cipherSuite.lastIndexOf('_');

        if ( firstSep < 0 || lastSep < 0 ) {
            throw new CryptoException("Invalid cipher suite specification " + cipherSuite); //$NON-NLS-1$
        }

        String hash = cipherSuite.substring(lastSep + 1);
        String stripped = cipherSuite.substring(firstSep + 1, lastSep);
        int sepPos = stripped.indexOf("_WITH_"); //$NON-NLS-1$

        if ( sepPos < 0 ) {
            throw new CryptoException("Invalid cipher suite specification " + cipherSuite); //$NON-NLS-1$
        }

        String keyAlg = stripped.substring(0, sepPos);
        String encAlg = stripped.substring(sepPos + 6);

        try {
            return new TLSCipherSuiteSpec(
                TLSKeyAlgorithm.fromString(keyAlg),
                TLSEncryptionAlgorithm.fromString(encAlg),
                TLSHashAlgorithm.fromString(hash));
        }
        catch ( IllegalArgumentException e ) {
            throw new CryptoException("Failed to parse cipher suite specification " + cipherSuite, e); //$NON-NLS-1$
        }
    }
}
