/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update;


import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.Context;


/**
 * @author mbechler
 *
 */
public interface ServiceReconfigurator extends SystemService {

    /**
     * @param context
     * @param allowRestart
     * @param b
     * @param serviceType
     * @throws ModelServiceException
     * @throws ConfigRepositoryException
     * @throws JobBuilderException
     */
    void reconfigureAll ( Context context, boolean allowRestart, String serviceType )
            throws ModelServiceException, ConfigRepositoryException, JobBuilderException;


    /**
     * @param b
     * @param context
     * @param allowRestart
     * @param service
     * @throws ConfigRepositoryException
     * @throws ModelServiceException
     * @throws JobBuilderException
     */
    void reconfigure ( Context context, boolean allowRestart, StructuralObjectReference service )
            throws ConfigRepositoryException, ModelServiceException, JobBuilderException;


    /**
     * @param serviceType
     * @return the classload to use for loading the jobs units
     */
    ClassLoader getUnitClassLoader ( String serviceType );

}
