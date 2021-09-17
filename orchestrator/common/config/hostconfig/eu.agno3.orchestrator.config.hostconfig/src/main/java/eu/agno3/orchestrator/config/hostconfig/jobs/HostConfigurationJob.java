/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.jobs;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;


/**
 * @author mbechler
 * 
 */
public class HostConfigurationJob extends @NonNull ConfigurationJob {

    private HostConfiguration hostConfig;


    /**
     * @param hostConfig
     *            the hostConfig to set
     */
    public void setHostConfig ( HostConfiguration hostConfig ) {
        this.hostConfig = hostConfig;
    }


    /**
     * @return the hostConfig
     */
    public HostConfiguration getHostConfig () {
        return this.hostConfig;
    }
}
