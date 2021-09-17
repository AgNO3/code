/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update;


import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface UpdateTracker extends SystemService {

    /**
     * @return the current installation status
     * 
     */
    UpdateInstallation getCurrent ();


    /**
     * @return the current revert installation status
     * 
     */
    UpdateInstallation getRevert ();


    /**
     * @param inst
     * @param suggestReboot
     */
    void updated ( UpdateInstallation inst, boolean suggestReboot );


    /**
     * @return whether the system should be rebooted to complete an update
     */
    boolean isRebootIndicated ();
}
