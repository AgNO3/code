/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap;


import java.io.File;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;


/**
 * @author mbechler
 *
 */
public interface BootstrapConfigurationProvider {

    /**
     * @return the bootstrapHostConfig
     */
    @NonNull
    HostConfiguration getBootstrapHostConfig ();


    /**
     * @return initial server configuration
     */
    @NonNull
    OrchestratorConfiguration getServerConfiguration ();


    /**
     * @return whether a local server should be bootstrapped
     */
    boolean isLocalServer ();


    /**
     * 
     * @return the pulicly usable hostname to use.
     */
    String getOverrideHostName ();


    /**
     * @return the initial administrator password, for local server
     */
    String getAdminPassword ();


    /**
     * @return whether the bootstrap process should be run in developer mode
     */
    boolean isDeveloperMode ();


    /**
     * @return the directory under which the local server configuration is stored
     */
    File getLocalServerConfigDirectory ();


    /**
     * @return the instance image type
     */
    String getImageType ();


    /**
     * @return whether to automatically complete the configuration using defaults
     */
    boolean isAutoRun ();

}