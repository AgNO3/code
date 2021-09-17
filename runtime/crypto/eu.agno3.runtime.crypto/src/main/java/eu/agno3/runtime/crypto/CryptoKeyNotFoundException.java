/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto;


/**
 * @author mbechler
 *
 */
public class CryptoKeyNotFoundException extends CryptoException {

    /**
     * 
     */
    private static final long serialVersionUID = -2646698972580074131L;


    /**
     * 
     */
    public CryptoKeyNotFoundException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public CryptoKeyNotFoundException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public CryptoKeyNotFoundException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public CryptoKeyNotFoundException ( Throwable t ) {
        super(t);
    }

}
