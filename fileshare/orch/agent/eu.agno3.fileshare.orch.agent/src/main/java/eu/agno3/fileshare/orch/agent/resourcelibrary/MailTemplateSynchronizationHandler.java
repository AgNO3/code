/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.agent.resourcelibrary;


import java.io.File;
import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.orch.agent.FileshareRuntimeServiceManager;
import eu.agno3.fileshare.orch.common.config.desc.FileshareMailResourceLibraryDescriptor;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.orchestrator.system.cfgfiles.AbstractResourceLibraryConfigFilesSynchronizationHandler;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFileManager;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory;
import eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 *
 */
@Component ( service = ResourceLibrarySynchronizationHandler.class )
public class MailTemplateSynchronizationHandler extends AbstractResourceLibraryConfigFilesSynchronizationHandler {

    private ServiceManager serviceManager;
    private ConfigFilesManagerFactory configFilesManagerFactory;
    private ExecutionConfigProperties executionConfig;


    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    @Reference
    protected synchronized void setConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        this.configFilesManagerFactory = cfmf;
    }


    protected synchronized void unsetConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        if ( this.configFilesManagerFactory == cfmf ) {
            this.configFilesManagerFactory = null;
        }
    }


    @Reference
    protected synchronized void setExecutionConfig ( ExecutionConfigProperties exc ) {
        this.executionConfig = exc;
    }


    protected synchronized void unsetExecutionConfig ( ExecutionConfigProperties exc ) {
        if ( this.executionConfig == exc ) {
            this.executionConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler#getType()
     */
    @Override
    public String getType () {
        return FileshareMailResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ResourceLibraryException
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.AbstractResourceLibraryConfigFilesSynchronizationHandler#getManager(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    protected ConfigFileManager getManager ( StructuralObjectReference service ) throws ResourceLibraryException {
        try {
            FileshareRuntimeServiceManager sm = this.serviceManager.getServiceManager(service, FileshareRuntimeServiceManager.class);
            File file = PrefixUtil.resolvePrefix(this.executionConfig, sm.getConfigFilesPath()).toFile();
            ConfigFilesManager cfm = this.configFilesManagerFactory.getForPath(
                file,
                null,
                sm.getGroupPrincipal(),
                FileSecurityUtils.getGroupReadFilePermissions(),
                FileSecurityUtils.getGroupReadDirPermissions());
            return cfm.getCfgFileRoot("mail"); //$NON-NLS-1$
        }
        catch (
            IOException |
            ServiceManagementException e ) {
            throw new ResourceLibraryException("Failed to get config file storage", e); //$NON-NLS-1$
        }
    }
}
