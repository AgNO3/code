/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


import java.util.List;


/**
 * @author mbechler
 *
 */
public class NotificationMultiException extends NotificationException {

    /**
     * 
     */
    private static final long serialVersionUID = -1745755403809614070L;
    private List<NotificationException> failures;


    /**
     * 
     */
    public NotificationMultiException () {
        super();
    }


    /**
     * @param failures
     * @param msg
     * @param t
     */
    public NotificationMultiException ( List<NotificationException> failures, String msg, Throwable t ) {
        super(msg, t);
        this.failures = failures;
    }


    /**
     * @param failures
     * @param msg
     */
    public NotificationMultiException ( List<NotificationException> failures, String msg ) {
        super(msg);
        this.failures = failures;
    }


    /**
     * @param failures
     * @param cause
     */
    public NotificationMultiException ( List<NotificationException> failures, Throwable cause ) {
        super(cause);
        this.failures = failures;
    }


    /**
     * @return the failures
     */
    public List<NotificationException> getFailures () {
        return this.failures;
    }

}
