/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class ShareNotFoundException extends ShareException {

    /**
     * 
     */
    private static final long serialVersionUID = -4287751013812921492L;


    /**
     * 
     */
    public ShareNotFoundException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public ShareNotFoundException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ShareNotFoundException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public ShareNotFoundException ( Throwable cause ) {
        super(cause);
    }

}
