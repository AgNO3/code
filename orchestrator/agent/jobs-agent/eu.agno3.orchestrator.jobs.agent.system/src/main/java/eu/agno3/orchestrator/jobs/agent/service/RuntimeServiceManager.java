/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service;


import java.net.URI;
import java.nio.file.Path;
import java.util.Set;

import eu.agno3.runtime.jmx.JMXClient;


/**
 * @author mbechler
 *
 */
public interface RuntimeServiceManager extends BaseServiceManager {

    /**
     * 
     * @return the path to the configuration directory
     */
    Path getConfigFilesPath ();


    /**
     * 
     * @return the system service name
     */
    String getSystemServiceName ();


    /**
     * 
     * @return a jmx connection
     * @throws ServiceManagementException
     */
    JMXClient getJMXConnection () throws ServiceManagementException;


    /**
     * @return a new jmx connection
     * @throws ServiceManagementException
     * 
     */
    JMXClient getUnpooledJMXConnection () throws ServiceManagementException;


    /**
     * @param modifiedPids
     * @return whether changes to these PIDs can be performed at runtime
     */
    boolean isOnlineReconfigurable ( Set<String> modifiedPids );


    /**
     * @param pids
     * @throws ServiceManagementException
     */
    void reconfigure ( Set<String> pids ) throws ServiceManagementException;


    /**
     * @return PIDs which should not be removed, even when not configured
     */
    Set<String> getNoRemovePIDs ();


    /**
     * @return the P2 install location
     */
    URI getP2InstallLocation ();

}