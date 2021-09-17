/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.05.2015 by mbechler
 */
package eu.agno3.runtime.security.password;


import java.nio.charset.Charset;


/**
 * @author mbechler
 *
 */
public final class PasswordCompareUtil {

    private static final int MAX_LENGTH = 256;


    /**
     * Constant time string comparison
     * 
     * Limited to 256 byte length.
     * 
     * Hopefully nothing gets optimized away.
     * 
     * @param a
     * @param b
     * @return whether the two password are equal
     */
    public static boolean comparePassword ( String a, String b ) {
        byte[] aBytes = a.getBytes(Charset.forName("UTF-8")); //$NON-NLS-1$
        byte[] bBytes = b.getBytes(Charset.forName("UTF-8")); //$NON-NLS-1$
        byte[] empty = new byte[MAX_LENGTH];

        if ( aBytes.length >= MAX_LENGTH || bBytes.length >= MAX_LENGTH ) {
            throw new IllegalArgumentException();
        }

        int res = 0;
        for ( int i = 0; i < MAX_LENGTH; i++ ) {
            res |= ( i < aBytes.length ? aBytes[ i ] : empty[ i ] ) ^ ( i < bBytes.length ? bBytes[ i ] : empty[ i ] );
        }
        return ( res | aBytes.length ^ bBytes.length ) == 0;
    }
}
