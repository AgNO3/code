/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto;


/**
 * @author mbechler
 *
 */
public class CryptoException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4517287911061816777L;


    /**
     * 
     */
    public CryptoException () {}


    /**
     * @param m
     */
    public CryptoException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public CryptoException ( Throwable t ) {
        super(t);
    }


    /**
     * @param m
     * @param t
     */
    public CryptoException ( String m, Throwable t ) {
        super(m, t);
    }

}
