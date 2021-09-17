/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2016 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class UnsupportedOperationException extends FileshareException {

    /**
     * 
     */
    public UnsupportedOperationException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public UnsupportedOperationException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public UnsupportedOperationException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public UnsupportedOperationException ( Throwable cause ) {
        super(cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7520157788194217578L;

}
