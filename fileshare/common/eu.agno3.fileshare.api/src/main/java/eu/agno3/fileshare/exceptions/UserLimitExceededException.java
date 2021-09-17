/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2016 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class UserLimitExceededException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -7613863835326365167L;


    /**
     * 
     */
    public UserLimitExceededException () {}


    /**
     * @param msg
     * @param t
     */
    public UserLimitExceededException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public UserLimitExceededException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public UserLimitExceededException ( Throwable cause ) {
        super(cause);
    }

}
