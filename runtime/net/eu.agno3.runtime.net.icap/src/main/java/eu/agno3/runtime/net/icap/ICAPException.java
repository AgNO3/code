/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap;


/**
 * @author mbechler
 *
 */
public class ICAPException extends Exception {

    /**
     * 
     */
    public ICAPException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public ICAPException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public ICAPException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ICAPException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1585471289395759119L;

}
