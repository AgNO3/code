/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class UploadException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = -7186125082882461972L;


    /**
     * 
     */
    public UploadException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public UploadException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public UploadException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public UploadException ( Throwable cause ) {
        super(cause);
    }

}
