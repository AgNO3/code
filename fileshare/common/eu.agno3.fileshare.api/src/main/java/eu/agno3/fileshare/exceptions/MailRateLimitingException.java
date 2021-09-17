/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.06.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class MailRateLimitingException extends FileshareException {

    /**
     * 
     */
    private static final long serialVersionUID = -5113040888168096713L;

    private int delay;


    /**
     * 
     */
    public MailRateLimitingException () {}


    /**
     * @param delay
     * 
     */
    public MailRateLimitingException ( int delay ) {
        this.delay = delay;
    }


    /**
     * @param delay
     * @param msg
     * @param t
     */
    public MailRateLimitingException ( int delay, String msg, Throwable t ) {
        super(msg, t);
        this.delay = delay;
    }


    /**
     * @param delay
     * @param msg
     */
    public MailRateLimitingException ( int delay, String msg ) {
        super(msg);
        this.delay = delay;
    }


    /**
     * @param delay
     * @param cause
     */
    public MailRateLimitingException ( int delay, Throwable cause ) {
        super(cause);
        this.delay = delay;
    }


    /**
     * @return the delay
     */
    public int getDelay () {
        return this.delay;
    }
}
