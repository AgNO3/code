/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.resourcelibrary;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8063941035301072685L;


    /**
     * 
     */
    public ResourceLibraryException () {}


    /**
     * @param message
     */
    public ResourceLibraryException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public ResourceLibraryException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public ResourceLibraryException ( String message, Throwable cause ) {
        super(message, cause);
    }

}
