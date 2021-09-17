/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.scrypt;


import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.generators.SCrypt;


/**
 * @author mbechler
 *
 */
public final class SCryptUtil {

    /**
     * 
     */
    private SCryptUtil () {}

    /**
     * 
     */
    private static final int OUTPUT_SIZE = 32;
    /**
     * 
     */
    private static final String SALT_ID = "s0"; //$NON-NLS-1$

    static final java.util.Base64.Encoder B64ENC = java.util.Base64.getEncoder();
    static final java.util.Base64.Decoder B64DEC = java.util.Base64.getDecoder();


    /**
     * 
     * @param toCheck
     *            the input to check
     * @param salt
     *            the stored salt string in the format produced by generate
     * @param key
     *            the stored dervied key
     * @return whether the derived key matches
     */
    public static boolean check ( byte[] toCheck, String salt, byte[] key ) {
        String[] saltComps = StringUtils.split(salt, '$');
        if ( saltComps.length != 3 || !saltComps[ 0 ].equals(SALT_ID) ) {
            throw new IllegalArgumentException("Specified salt is invalid for this algorithm"); //$NON-NLS-1$
        }

        // extract parameters from salt
        byte[] saltBinary = B64DEC.decode(saltComps[ 2 ]);
        SCryptParams params = new SCryptParams(Long.parseLong(saltComps[ 1 ], 16));

        // generate password hash
        byte[] generated = generateInternal(toCheck, saltBinary, params);

        // constant time compare
        boolean mismatch = false;
        for ( int i = 0; i < key.length; i++ ) {
            mismatch |= ( key[ i ] ^ generated[ i ] ) != 0;
        }
        return !mismatch;
    }


    /**
     * @param salt
     * @return the scrypt paramters stored with the salt
     */
    public static SCryptParams getParamsFromSalt ( String salt ) {
        String[] saltComps = StringUtils.split(salt, '$');
        if ( saltComps.length != 3 || !saltComps[ 0 ].equals(SALT_ID) ) {
            throw new IllegalArgumentException("Specified salt is invalid for this algorithm"); //$NON-NLS-1$
        }
        return new SCryptParams(Long.parseLong(saltComps[ 1 ], 16));
    }


    /**
     * @param password
     * @param saltBinary
     * @param params
     * @return
     */
    private static byte[] generateInternal ( byte[] password, byte[] saltBinary, SCryptParams params ) {
        return SCrypt.generate(password, saltBinary, params.getN(), params.getR(), params.getP(), OUTPUT_SIZE);
    }


    /**
     * @param password
     * @param saltBinary
     * @param params
     * @return the derived key plus an structured salt string
     */
    public static SCryptResult generate ( byte[] password, byte[] saltBinary, SCryptParams params ) {
        return new SCryptResult(makeSalt(saltBinary, params), generateInternal(password, saltBinary, params));
    }


    /**
     * @param saltBinary
     * @param n
     * @param r
     * @param p
     * @return
     */
    private static String makeSalt ( byte[] saltBinary, SCryptParams params ) {
        return String.format("$s0$%s$%s", Long.toString(params.toLong(), 16), B64ENC.encodeToString(saltBinary)); //$NON-NLS-1$
    }
}
