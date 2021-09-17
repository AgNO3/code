/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.runtime.update;


/**
 * @author mbechler
 *
 */
public interface PlatformStateListener {

    /**
     * 
     * @param state
     */
    public void stateChanged ( PlatformState state );
}
