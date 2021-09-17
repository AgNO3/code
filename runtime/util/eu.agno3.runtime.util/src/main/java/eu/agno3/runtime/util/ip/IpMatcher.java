/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.runtime.util.ip;


/**
 * @author mbechler
 *
 */
public class IpMatcher {

    /**
     * 
     * @param toMatch
     * @param spec
     * @param prefixLength
     * @return checks whether toMatch lies within spec/prefixLength
     */
    public static boolean match ( short[] toMatch, short[] spec, int prefixLength ) {

        if ( toMatch == null || spec == null ) {
            return false;
        }

        int size = toMatch.length;
        if ( size != spec.length ) {
            return false;
        }

        int remainPrefixLength = prefixLength;
        int pos = 0;
        while ( remainPrefixLength > 0 && pos < size ) {
            int r = remainPrefixLength;
            if ( r >= 8 ) {
                r = 0;
            }

            if ( r == 0 ) {
                if ( toMatch[ pos ] != spec[ pos ] ) {
                    return false;
                }
                remainPrefixLength -= 8;
                pos++;
                continue;
            }

            short mask = 0;
            for ( int p = 1; p <= r; p++ ) {
                mask |= 1 << ( 8 - p );
            }

            if ( ( toMatch[ pos ] & mask ) != ( spec[ pos ] & mask ) ) {
                return false;
            }
            remainPrefixLength -= 8;
            pos++;
        }

        return true;
    }
}
