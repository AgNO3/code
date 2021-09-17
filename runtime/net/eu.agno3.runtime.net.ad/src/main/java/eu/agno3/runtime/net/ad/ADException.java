/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


/**
 * @author mbechler
 *
 */
public class ADException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -935936558090358483L;


    /**
     * 
     */
    public ADException () {}


    /**
     * @param message
     */
    public ADException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ADException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public ADException ( String message, Throwable cause ) {
        super(message, cause);
    }

}
