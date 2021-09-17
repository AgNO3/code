/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class FileshareException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4190517121195100315L;


    /**
     * 
     */
    public FileshareException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public FileshareException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public FileshareException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public FileshareException ( Throwable cause ) {
        super(cause);
    }

}
