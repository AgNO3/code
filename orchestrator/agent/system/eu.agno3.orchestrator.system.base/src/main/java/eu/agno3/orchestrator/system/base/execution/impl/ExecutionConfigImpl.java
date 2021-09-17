/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * Configuration determining the very fundamental behaviour of execution units
 * 
 * @author mbechler
 * 
 */
@Component ( service = ExecutionConfig.class )
public class ExecutionConfigImpl implements ExecutionConfig {

    private static final String NO_SERVICE_TYPE_ANNOTATION = "SystemService without a @SystemServiceType annotation "; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ExecutionConfigImpl.class);

    private Map<Class<?>, Object> services = new HashMap<>();

    private ExecutionConfigProperties props = new ExecutionConfigPropertiesImpl();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindService ( SystemService service ) {
        SystemServiceType t = service.getClass().getAnnotation(SystemServiceType.class);

        if ( t == null ) {
            log.warn(NO_SERVICE_TYPE_ANNOTATION + service.getClass().getName());
            return;
        }

        this.services.put(t.value(), service);
    }


    protected synchronized void unbindService ( SystemService service ) {
        SystemServiceType t = service.getClass().getAnnotation(SystemServiceType.class);

        if ( t == null ) {
            log.warn(NO_SERVICE_TYPE_ANNOTATION + service.getClass().getName());
            return;
        }

        if ( service == this.services.get(t.value()) ) {
            this.services.remove(t.value());
        }
    }


    @Reference
    protected synchronized void setExecutionConfigProperties ( ExecutionConfigProperties ecp ) {
        this.props = ecp;
    }


    protected synchronized void unsetExecutionConfigProperties ( ExecutionConfigProperties ecp ) {
        if ( this.props == ecp ) {
            this.props = null;
        }
    }


    @Override
    public boolean isDryRun () {
        return this.props.isDryRun();
    }


    @Override
    public Path getPrefix () {
        return this.props.getPrefix();
    }


    @Override
    public boolean isAlwaysCreateTargets () {
        return this.props.isAlwaysCreateTargets();
    }


    @Override
    public boolean isNoVerifyEnv () {
        return this.props.isNoVerifyEnv();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#registerService(java.lang.Class,
     *      eu.agno3.orchestrator.system.base.SystemService)
     */
    @Override
    public synchronized <T extends SystemService> void registerService ( Class<T> serviceClass, T service ) {
        this.services.put(serviceClass, service);
    }


    /**
     * @param serviceClass
     * @param service
     */
    public synchronized <T extends SystemService> void unregisterService ( Class<T> serviceClass, T service ) {
        if ( service == this.services.get(serviceClass) ) {
            this.services.remove(serviceClass);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#getService(java.lang.Class)
     */
    @Override
    public <T extends SystemService> T getService ( Class<T> serviceClass ) throws NoSuchServiceException {
        @SuppressWarnings ( "unchecked" )
        T service = (T) this.services.get(serviceClass);

        if ( service == null ) {
            throw new NoSuchServiceException("No service registered for " + serviceClass.getName()); //$NON-NLS-1$
        }

        return service;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ExecutionException
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionConfig#ensureEnv()
     */
    @Override
    public void ensureEnv () throws ExecutionException {
        boolean rootExists = Files.exists(this.getPrefix());
        if ( !rootExists && !this.isAlwaysCreateTargets() ) {
            throw new ExecutionException("Configured root does not exist and is not created"); //$NON-NLS-1$
        }
        else if ( !rootExists ) {
            try {
                Files.createDirectories(this.getPrefix(), PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            }
            catch ( IOException e ) {
                throw new ExecutionException("Configured root could not be created", e); //$NON-NLS-1$
            }
        }
    }

}
