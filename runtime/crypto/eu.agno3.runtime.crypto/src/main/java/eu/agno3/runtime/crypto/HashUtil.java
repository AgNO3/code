/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto;


/**
 * @author mbechler
 *
 */
public final class HashUtil {

    /**
     * 
     */
    private HashUtil () {}


    /**
     * @param hash
     * @param breakLines
     *            whether to break lines after 32 characters
     * @return fingerprint in dotted hexadecimal form
     */
    public static String hexToDotted ( String hash, boolean breakLines ) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for ( int i = 0; i < hash.length(); i += 2 ) {
            if ( first ) {
                first = false;
            }
            else if ( breakLines && i % 32 == 0 ) {
                sb.append(':');
                sb.append(System.lineSeparator());
            }
            else {
                sb.append(':');
            }
            sb.append(hash.substring(i, i + 2));
        }

        return sb.toString();
    }
}
