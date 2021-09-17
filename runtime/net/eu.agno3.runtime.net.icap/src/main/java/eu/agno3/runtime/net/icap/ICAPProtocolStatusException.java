/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2017 by mbechler
 */
package eu.agno3.runtime.net.icap;


/**
 * @author mbechler
 *
 */
public class ICAPProtocolStatusException extends ICAPProtocolException {

    /**
     * 
     */
    private static final long serialVersionUID = 775555215140737329L;

    private int status;


    /**
     * 
     */
    public ICAPProtocolStatusException () {
        super();
    }


    /**
     * @param status
     * @param message
     * @param cause
     */
    public ICAPProtocolStatusException ( int status, String message, Throwable cause ) {
        super(message, cause);
        this.status = status;
    }


    /**
     * @param status
     * @param message
     */
    public ICAPProtocolStatusException ( int status, String message ) {
        super(message);
        this.status = status;
    }


    /**
     * @return the status
     */
    public int getStatusCode () {
        return this.status;
    }

}
