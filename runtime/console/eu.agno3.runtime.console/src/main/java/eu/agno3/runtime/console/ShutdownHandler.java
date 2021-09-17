/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.06.2013 by mbechler
 */
package eu.agno3.runtime.console;


/**
 * @author mbechler
 * 
 */
public interface ShutdownHandler {

    /**
     * Is called when a container shutdown is requested
     */
    void shutdown ();
}
