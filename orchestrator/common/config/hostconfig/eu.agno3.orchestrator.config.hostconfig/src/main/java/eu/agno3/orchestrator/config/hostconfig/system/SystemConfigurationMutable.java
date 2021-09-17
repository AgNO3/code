/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.system;


import java.util.Set;

import eu.agno3.orchestrator.config.web.RuntimeConfigurationMutable;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;


/**
 * @author mbechler
 * 
 */
public interface SystemConfigurationMutable extends SystemConfiguration {

    /**
     * @param swapiness
     */
    void setSwapiness ( Integer swapiness );


    /**
     * @param enableSshAccess
     */
    void setEnableSshAccess ( Boolean enableSshAccess );


    /**
     * @param agentConfig
     */
    void setAgentConfig ( RuntimeConfigurationMutable agentConfig );


    /**
     * @param adminSSHPublicKeys
     */
    void setAdminSshPublicKeys ( Set<PublicKeyEntry> adminSSHPublicKeys );


    /**
     * @param sshKeyOnly
     */
    void setSshKeyOnly ( Boolean sshKeyOnly );

}
