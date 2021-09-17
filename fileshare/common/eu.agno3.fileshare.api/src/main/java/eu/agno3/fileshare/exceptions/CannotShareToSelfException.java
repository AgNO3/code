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
public class CannotShareToSelfException extends ShareException {

    /**
     * 
     */
    private static final long serialVersionUID = -7524643227594498618L;


    /**
     * 
     */
    public CannotShareToSelfException () {}


    /**
     * @param msg
     * @param t
     */
    public CannotShareToSelfException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public CannotShareToSelfException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public CannotShareToSelfException ( Throwable cause ) {
        super(cause);
    }

}
