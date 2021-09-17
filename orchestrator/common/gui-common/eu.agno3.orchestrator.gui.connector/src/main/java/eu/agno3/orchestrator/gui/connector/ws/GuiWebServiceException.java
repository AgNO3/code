/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.ws;


/**
 * @author mbechler
 * 
 */
public class GuiWebServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4016821997070171495L;


    /**
     * 
     */
    public GuiWebServiceException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public GuiWebServiceException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public GuiWebServiceException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public GuiWebServiceException ( Throwable cause ) {
        super(cause);
    }

}
