/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2017 by mbechler
 */
package eu.agno3.runtime.net.icap;


/**
 * @author mbechler
 *
 */
public class ICAPProtocolException extends ICAPException {

    /**
     * 
     */
    private static final long serialVersionUID = -2305247763245814401L;


    /**
     * 
     */
    public ICAPProtocolException () {}


    /**
     * @param message
     * @param cause
     */
    public ICAPProtocolException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public ICAPProtocolException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ICAPProtocolException ( Throwable cause ) {
        super(cause);
    }

}
