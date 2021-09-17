/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.runtime.update;


/**
 * @author mbechler
 *
 */
public interface ApplicationStateProvider {

    /**
     * @return the application state
     */
    public PlatformState getStatus ();
}
