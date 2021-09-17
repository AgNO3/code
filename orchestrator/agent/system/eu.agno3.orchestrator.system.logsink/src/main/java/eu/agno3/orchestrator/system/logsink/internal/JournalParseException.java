/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink.internal;


/**
 * @author mbechler
 *
 */
public class JournalParseException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2526945851621508684L;


    /**
     * 
     */
    public JournalParseException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public JournalParseException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public JournalParseException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public JournalParseException ( Throwable cause ) {
        super(cause);
    }

}
