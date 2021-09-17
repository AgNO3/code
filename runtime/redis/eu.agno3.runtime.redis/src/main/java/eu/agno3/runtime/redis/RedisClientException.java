/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 28, 2017 by mbechler
 */
package eu.agno3.runtime.redis;


/**
 * @author mbechler
 *
 */
public class RedisClientException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5946849406944736775L;


    /**
     * 
     */
    public RedisClientException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public RedisClientException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public RedisClientException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public RedisClientException ( Throwable cause ) {
        super(cause);
    }

}
