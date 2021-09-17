/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore;


/**
 * @author mbechler
 *
 */
public class TruststoreManagerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1343382102253289742L;


    /**
     * 
     */
    public TruststoreManagerException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public TruststoreManagerException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public TruststoreManagerException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public TruststoreManagerException ( Throwable cause ) {
        super(cause);
    }

}
