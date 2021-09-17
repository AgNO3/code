/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.crypto;


import javax.crypto.Cipher;


/**
 * @author mbechler
 *
 */
public final class CipherUtil {

    /**
     * 
     */
    private CipherUtil () {}


    /**
     * 
     * @param c
     * @return preferred IV length for the algorithm
     */
    public static int getIVLength ( Cipher c ) {
        String algo = c.getAlgorithm();
        if ( "AES/GCM/NoPadding".equals(algo) ) { //$NON-NLS-1$
            return 12; // GCM preferred IV size is 96 bit
        }

        return c.getBlockSize();
    }

}
