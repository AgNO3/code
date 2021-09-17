/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator.jobs;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;


/**
 * @author mbechler
 * 
 */
public class OrchestratorConfigurationJob extends ConfigurationJob {

    private OrchestratorConfiguration orchConfig;
    private HostConfiguration bootstrapHostConfig;


    /**
     * @param orchConfig
     *            the hostConfig to set
     */
    public void setOrchestratorConfig ( @NonNull OrchestratorConfiguration orchConfig ) {
        this.orchConfig = orchConfig;
    }


    /**
     * @return the hostConfig
     */
    public OrchestratorConfiguration getOrchestratorConfig () {
        return this.orchConfig;
    }


    /**
     * @param bootstrapHostConfig
     */
    public void setBootstrapHostConfig ( HostConfiguration bootstrapHostConfig ) {
        this.bootstrapHostConfig = bootstrapHostConfig;
    }


    /**
     * @return the bootstrapHostConfig
     */
    public HostConfiguration getBootstrapHostConfig () {
        return this.bootstrapHostConfig;
    }
}
