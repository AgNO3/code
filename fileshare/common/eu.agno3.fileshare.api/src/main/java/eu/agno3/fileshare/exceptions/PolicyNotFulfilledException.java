/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


import eu.agno3.fileshare.model.PolicyViolation;


/**
 * @author mbechler
 *
 */
public class PolicyNotFulfilledException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 5098461301283418904L;
    private PolicyViolation violation;


    /**
     * 
     */
    public PolicyNotFulfilledException () {}


    /**
     * @param msg
     * @param t
     */
    public PolicyNotFulfilledException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public PolicyNotFulfilledException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public PolicyNotFulfilledException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param violation
     */
    public PolicyNotFulfilledException ( PolicyViolation violation ) {
        this.violation = violation;
    }


    /**
     * @return the violation
     */
    public PolicyViolation getViolation () {
        return this.violation;
    }

}
