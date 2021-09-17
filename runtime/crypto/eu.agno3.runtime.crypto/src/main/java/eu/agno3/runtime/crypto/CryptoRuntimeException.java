/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto;


/**
 * @author mbechler
 *
 */
public class CryptoRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -377023296068678298L;


    /**
     * 
     */
    public CryptoRuntimeException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public CryptoRuntimeException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public CryptoRuntimeException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public CryptoRuntimeException ( Throwable t ) {
        super(t);
    }

}
