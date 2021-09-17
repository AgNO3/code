/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 *
 */
public class ExtraServiceExecutionConfigWrapper implements ExecutionConfig {

    private ExecutionConfig delegate;
    private Map<Class<? extends SystemService>, SystemService> extraServices;


    /**
     * @param delegate
     * @param extraServices
     */
    public ExtraServiceExecutionConfigWrapper ( ExecutionConfig delegate, Map<Class<? extends SystemService>, SystemService> extraServices ) {
        this.delegate = delegate;
        this.extraServices = new HashMap<>(extraServices);
    }


    /**
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#isDryRun()
     */
    @Override
    public boolean isDryRun () {
        return this.delegate.isDryRun();
    }


    /**
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#getPrefix()
     */
    @Override
    public Path getPrefix () {
        return this.delegate.getPrefix();
    }


    /**
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#isAlwaysCreateTargets()
     */
    @Override
    public boolean isAlwaysCreateTargets () {
        return this.delegate.isAlwaysCreateTargets();
    }


    /**
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#isNoVerifyEnv()
     */
    @Override
    public boolean isNoVerifyEnv () {
        return this.delegate.isNoVerifyEnv();
    }


    /**
     * @param serviceClass
     * @param service
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#registerService(java.lang.Class,
     *      eu.agno3.orchestrator.system.base.SystemService)
     */
    @Override
    public <T extends SystemService> void registerService ( Class<T> serviceClass, T service ) {
        this.delegate.registerService(serviceClass, service);
    }


    /**
     * @param serviceClass
     * @throws NoSuchServiceException
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#getService(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends SystemService> T getService ( Class<T> serviceClass ) throws NoSuchServiceException {

        SystemService localService = this.extraServices.get(serviceClass);

        if ( localService != null ) {
            return (T) localService;
        }

        return this.delegate.getService(serviceClass);
    }


    /**
     * @throws ExecutionException
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#ensureEnv()
     */
    @Override
    public void ensureEnv () throws ExecutionException {
        this.delegate.ensureEnv();
    }

}
