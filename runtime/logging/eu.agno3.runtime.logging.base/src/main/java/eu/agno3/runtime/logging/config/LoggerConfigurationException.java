/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


/**
 * @author mbechler
 * 
 */
public class LoggerConfigurationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2353180589718639882L;


    /**
     * 
     */
    public LoggerConfigurationException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public LoggerConfigurationException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public LoggerConfigurationException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public LoggerConfigurationException ( Throwable t ) {
        super(t);
    }

}
