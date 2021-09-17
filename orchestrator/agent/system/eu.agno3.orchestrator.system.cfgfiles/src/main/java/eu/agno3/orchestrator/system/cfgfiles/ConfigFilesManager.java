/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles;


import java.io.IOException;
import java.util.Set;

import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface ConfigFilesManager extends SystemService {

    /**
     * 
     * @return the configured factories
     */
    Set<String> getPresentFactoryPIDs ();


    /**
     * 
     * @param factoryId
     * @return the configured factory instances
     */
    Set<String> getPresentFactoryInstances ( String factoryId );


    /**
     * 
     * @return the configured instance PIDs
     */
    Set<String> getInstancePIDs ();


    /**
     * 
     * @return the available cfg file roots
     */
    Set<String> listCfgFileRoots ();


    /**
     * 
     * @param pid
     * @return a manager for the given instance
     */
    ConfigManager getInstance ( String pid );


    /**
     * 
     * @param factoryId
     * @param instanceId
     * @return a manager for the given factory instance
     */
    ConfigManager getFactoryInstance ( String factoryId, String instanceId );


    /**
     * 
     * @param rootName
     * @return the manager for the given cfgfile root
     * @throws IOException
     */
    ConfigFileManager getCfgFileRoot ( String rootName ) throws IOException;

}
