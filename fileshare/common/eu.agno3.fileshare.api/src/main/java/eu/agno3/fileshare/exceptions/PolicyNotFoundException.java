/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class PolicyNotFoundException extends InvalidSecurityLabelException {

    /**
     * 
     */
    private static final long serialVersionUID = -4844877857550087424L;


    /**
     * 
     */
    public PolicyNotFoundException () {
        super();
    }


    /**
     * @param label
     * @param msg
     * @param t
     */
    public PolicyNotFoundException ( String label, String msg, Throwable t ) {
        super(label, msg, t);
    }


    /**
     * @param label
     * @param msg
     */
    public PolicyNotFoundException ( String label, String msg ) {
        super(label, msg);
    }


    /**
     * @param label
     * @param cause
     */
    public PolicyNotFoundException ( String label, Throwable cause ) {
        super(label, cause);
    }

}
