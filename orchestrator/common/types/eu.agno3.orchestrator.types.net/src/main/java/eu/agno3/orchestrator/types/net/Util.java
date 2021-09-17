/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


/**
 * @author mbechler
 * 
 */
public final class Util {

    private Util () {}


    /**
     * compares two arrays lexicographically
     * 
     * @param a
     * @param b
     * @return <0 iff a < b, =0 iff a =b, >0 iff a > b
     */
    public static int lexicalCompare ( short[] a, short[] b ) {
        if ( a == null && b == null ) {
            return 0;
        }
        else if ( a == null ) {
            return -1;
        }
        else if ( b == null ) {
            return 1;
        }

        return lexicalCompareInternal(a, b);
    }


    private static int lexicalCompareInternal ( short[] a, short[] b ) {
        for ( int i = 0; i < Math.min(a.length, b.length); i++ ) {
            int cmp = Short.compare(a[ i ], b[ i ]);
            if ( cmp != 0 ) {
                return cmp;
            }
        }

        if ( a.length < b.length ) {
            return -1;
        }
        else if ( b.length < a.length ) {
            return 1;
        }

        return 0;
    }
}
