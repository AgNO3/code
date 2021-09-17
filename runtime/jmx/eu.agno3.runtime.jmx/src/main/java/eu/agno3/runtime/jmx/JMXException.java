/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2016 by mbechler
 */
package eu.agno3.runtime.jmx;


/**
 * @author mbechler
 *
 */
public class JMXException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4148982623695890876L;


    /**
     * 
     */
    public JMXException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public JMXException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public JMXException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public JMXException ( Throwable cause ) {
        super(cause);
    }

}
