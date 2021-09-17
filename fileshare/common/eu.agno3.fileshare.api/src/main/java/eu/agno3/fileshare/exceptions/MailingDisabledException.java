/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class MailingDisabledException extends NotificationException {

    /**
     * 
     */
    private static final long serialVersionUID = -3383959126936978756L;


    /**
     * 
     */
    public MailingDisabledException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public MailingDisabledException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public MailingDisabledException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public MailingDisabledException ( Throwable cause ) {
        super(cause);
    }

}
