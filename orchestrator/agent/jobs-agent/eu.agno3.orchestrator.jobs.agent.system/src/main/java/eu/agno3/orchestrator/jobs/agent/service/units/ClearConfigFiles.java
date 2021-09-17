/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.09.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service.units;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;


/**
 * @author mbechler
 *
 */
public class ClearConfigFiles extends AbstractExecutionUnit<StatusOnlyResult, ClearConfigFiles, ClearConfigFilesConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -410359686218966806L;

    private StructuralObjectReference service;
    private String root;

    private Path tempDir;

    private Path origPath;


    /**
     * @return the service
     */
    public StructuralObjectReference getService () {
        return this.service;
    }


    /**
     * @param service
     *            the service to set
     */
    void setService ( ServiceStructuralObject service ) {
        this.service = StructuralObjectReferenceImpl.fromObject(service);
    }


    /**
     * @return the root
     */
    public String getRoot () {
        return this.root;
    }


    /**
     * @param root
     *            the root to set
     */
    void setRoot ( String root ) {
        this.root = root;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isEmpty(this.root) ) {
            throw new InvalidUnitConfigurationException("root is required"); //$NON-NLS-1$
        }

        if ( this.service == null ) {
            throw new InvalidUnitConfigurationException("service is required"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        try {
            context.getConfig().getService(ConfigFilesManagerFactory.class);
            ServiceManager sm = context.getConfig().getService(ServiceManager.class);
            RuntimeServiceManager rsm = sm.getServiceManager(this.service, RuntimeServiceManager.class);
            File file = PrefixUtil.resolvePrefix(context, rsm.getConfigFilesPath()).toFile();

            if ( !file.exists() ) {
                return new StatusOnlyResult(Status.SKIPPED);
            }
        }
        catch (
            NoSuchServiceException |
            ServiceManagementException e ) {
            throw new ExecutionException("Failed to get services", e); //$NON-NLS-1$
        }
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        try {
            ConfigFilesManagerFactory cfmf = context.getConfig().getService(ConfigFilesManagerFactory.class);
            ServiceManager sm = context.getConfig().getService(ServiceManager.class);
            RuntimeServiceManager rsm = sm.getServiceManager(this.service, RuntimeServiceManager.class);
            File file = PrefixUtil.resolvePrefix(context, rsm.getConfigFilesPath()).toFile();

            if ( !file.exists() ) {
                return new StatusOnlyResult(Status.SKIPPED);
            }

            ConfigFilesManager cfm = cfmf.getForPath(
                file,
                null,
                rsm.getGroupPrincipal(),
                FileSecurityUtils.getGroupReadFilePermissions(),
                FileSecurityUtils.getGroupReadDirPermissions());

            Path basePath = cfm.getCfgFileRoot(this.root).getBasePath();
            this.origPath = basePath;
            this.tempDir = FileTemporaryUtils.createRelatedTemporaryDirectory(basePath);

            Files.move(basePath, this.tempDir, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (
            NoSuchServiceException |
            ServiceManagementException |
            IOException e ) {
            throw new ExecutionException("Failed to clear cfgfiles", e); //$NON-NLS-1$
        }
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#cleanup(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void cleanup ( Context context ) throws ExecutionException {
        super.cleanup(context);

        if ( this.tempDir != null && Files.exists(this.tempDir, LinkOption.NOFOLLOW_LINKS) ) {
            try {
                FileUtil.deleteRecursive(this.tempDir);
            }
            catch ( IOException e ) {
                context.getOutput().error("Failed to remove temporary directory", e); //$NON-NLS-1$
            }
            this.tempDir = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#rollback(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void rollback ( Context context ) throws ExecutionException {
        if ( this.tempDir != null && this.origPath != null ) {
            try {
                Files.move(this.tempDir, this.origPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to rollback", e); //$NON-NLS-1$
            }
            this.tempDir = null;
            this.origPath = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#canRollback()
     */
    @Override
    public boolean canRollback () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ClearConfigFilesConfigurator createConfigurator () {
        return new ClearConfigFilesConfigurator(this);
    }

}
