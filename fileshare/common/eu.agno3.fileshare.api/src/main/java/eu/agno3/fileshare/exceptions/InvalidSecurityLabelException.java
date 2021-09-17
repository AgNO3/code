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
public class InvalidSecurityLabelException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -4319715776114867495L;
    private String label;


    /**
     * 
     */
    public InvalidSecurityLabelException () {}


    /**
     * @param label
     * @param msg
     * @param t
     */
    public InvalidSecurityLabelException ( String label, String msg, Throwable t ) {
        super(msg, t);
        this.label = label;
    }


    /**
     * @param label
     * @param msg
     */
    public InvalidSecurityLabelException ( String label, String msg ) {
        super(msg);
        this.label = label;
    }


    /**
     * @param label
     * @param cause
     */
    public InvalidSecurityLabelException ( String label, Throwable cause ) {
        super(cause);
        this.label = label;
    }


    /**
     * @return the label
     */
    public String getLabel () {
        return this.label;
    }
}
