/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public enum TLSHashAlgorithm {

    SHA512(512), SHA384(384), SHA256(256), SHA(160), MD5(64);

    private int blockSize;


    /**
     * 
     */
    private TLSHashAlgorithm ( int blockSize ) {
        this.blockSize = blockSize;
    }


    /**
     * @return the blockSize
     */
    public int getBlockSize () {
        return this.blockSize;
    }


    public static TLSHashAlgorithm fromString ( String val ) {
        return valueOf(val);
    }


    public static String toString ( TLSHashAlgorithm algo ) {
        if ( algo == null ) {
            return null;
        }
        return algo.name();
    }


    /**
     * @param string
     * @return
     */
    public static TLSHashAlgorithm mapOpenSSL ( String val ) {
        return fromString(val);
    }
}
