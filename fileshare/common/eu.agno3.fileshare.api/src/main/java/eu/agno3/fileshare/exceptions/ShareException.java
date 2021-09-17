/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class ShareException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = -8786049142059897675L;


    /**
     * 
     */
    public ShareException () {}


    /**
     * @param msg
     * @param t
     */
    public ShareException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ShareException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public ShareException ( Throwable cause ) {
        super(cause);
    }

}
