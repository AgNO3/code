/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 * 
 */
public interface ExecutionConfig extends ExecutionConfigProperties {

    /**
     * Register a service for use by the execution units
     * 
     * @param serviceClass
     * @param service
     */
    <T extends SystemService> void registerService ( Class<T> serviceClass, T service );


    /**
     * 
     * @param serviceClass
     * @return the service object
     * @throws NoSuchServiceException
     *             if the service is not registered
     */
    <T extends SystemService> T getService ( Class<T> serviceClass ) throws NoSuchServiceException;


    /**
     * Makes sure the environment is valid
     * 
     * @throws ExecutionException
     */
    void ensureEnv () throws ExecutionException;

}
