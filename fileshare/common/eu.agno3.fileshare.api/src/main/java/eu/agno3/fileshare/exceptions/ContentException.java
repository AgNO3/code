/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class ContentException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = 6766905762503267159L;


    /**
     * 
     */
    public ContentException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public ContentException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ContentException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public ContentException ( Throwable cause ) {
        super(cause);
    }

}
