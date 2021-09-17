/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component;


/**
 * @author mbechler
 * 
 */
public class ComponentConfigurationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6421228753999177220L;


    /**
     * 
     */
    public ComponentConfigurationException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public ComponentConfigurationException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public ComponentConfigurationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ComponentConfigurationException ( Throwable cause ) {
        super(cause);
    }

}
