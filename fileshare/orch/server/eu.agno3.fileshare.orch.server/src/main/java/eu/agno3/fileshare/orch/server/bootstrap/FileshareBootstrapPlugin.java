/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 28, 2016 by mbechler
 */
package eu.agno3.fileshare.orch.server.bootstrap;


import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.orch.common.bootstrap.FileshareBootstrapContext;
import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.FileshareConfigurationImpl;
import eu.agno3.fileshare.orch.common.config.FileshareStorageConfigMutable;
import eu.agno3.fileshare.orch.common.config.desc.FileshareImage;
import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
import eu.agno3.fileshare.orch.common.jobs.FileshareConfigurationJob;
import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.bootstrap.BootstrapPlugin;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.MountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.config.model.realm.server.service.DefaultRealmServicesContext;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.server.security.LocalUserServerService;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = BootstrapPlugin.class )
public class FileshareBootstrapPlugin implements BootstrapPlugin {

    private static final Logger log = Logger.getLogger(FileshareBootstrapPlugin.class);
    private DefaultRealmServicesContext rctx;
    private LocalUserServerService localUserService;


    @Reference
    protected synchronized void setDefaultRealmServicesContext ( DefaultRealmServicesContext ctx ) {
        this.rctx = ctx;
    }


    protected synchronized void unsetDefaultRealmServicesContext ( DefaultRealmServicesContext ctx ) {
        if ( this.rctx == ctx ) {
            this.rctx = null;
        }
    }


    @Reference
    protected synchronized void setLocalUserService ( LocalUserServerService lus ) {
        this.localUserService = lus;
    }


    protected synchronized void unsetLocalUserService ( LocalUserServerService lus ) {
        if ( this.localUserService == lus ) {
            this.localUserService = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.bootstrap.BootstrapPlugin#isPrimary(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public boolean isPrimary ( InstanceStructuralObject instance ) {
        return FileshareImage.IMAGE_ID.equals(instance.getImageType());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.bootstrap.BootstrapPlugin#appliesTo(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public boolean appliesTo ( InstanceStructuralObject instance ) {
        return FileshareImage.IMAGE_ID.equals(instance.getImageType());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.bootstrap.BootstrapPlugin#createContext(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public BootstrapContext createContext ( InstanceStructuralObject bootstrapInstance ) {
        log.debug("Creating context"); //$NON-NLS-1$
        return new FileshareBootstrapContext();
    }


    @Override
    public void setupServices ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance, HostConfiguration hc )
            throws ModelObjectException, ModelServiceException {
        @NonNull
        ServiceStructuralObjectImpl fcService = getBootstrapFileshareConfigService(em, instance);

        @NonNull
        FileshareConfigurationImpl fcConfig = (@NonNull FileshareConfigurationImpl) this.rctx.getServiceService()
                .getServiceConfiguration(em, fcService);
        if ( hc == null || hc.getStorageConfiguration().getMountEntries().isEmpty() ) {
            log.debug("Host does not have storage (yet)"); //$NON-NLS-1$
            return;
        }

        StorageConfiguration sc = hc.getStorageConfiguration();
        LocalMountEntry foundLocal = null;
        MountEntry foundOther = null;

        for ( MountEntry me : sc.getMountEntries() ) {
            if ( me instanceof LocalMountEntry && foundLocal == null ) {
                foundLocal = (LocalMountEntry) me;
            }
            else if ( ! ( me instanceof LocalMountEntry ) && foundOther == null ) {
                foundOther = me;
            }
        }
        FileshareStorageConfigMutable fsStorage = fcConfig.getStorageConfiguration();
        if ( foundOther != null && foundLocal == null ) {
            log.debug("Picking remote storage for files"); //$NON-NLS-1$
            fsStorage.setFileStorage(foundOther.getAlias());
        }
        else if ( foundOther != null && foundLocal != null ) {
            log.debug("Picking remote storage for files, local storage for other"); //$NON-NLS-1$
            fsStorage.setFileStorage(foundOther.getAlias());
            fsStorage.setLocalStorage(foundLocal.getAlias());
        }
        else if ( foundLocal != null ) {
            log.debug("Picking local storage for files"); //$NON-NLS-1$
            fsStorage.setFileStorage(foundLocal.getAlias());
            fsStorage.setLocalStorage(foundLocal.getAlias());
        }
        this.rctx.getServiceService().updateServiceConfiguration(em, fcService, fcConfig, null);

    }


    @Override
    public void setupContext ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl pinst, BootstrapContext ctx )
            throws ModelObjectNotFoundException, ModelServiceException {
        if ( ! ( ctx instanceof FileshareBootstrapContext ) ) {
            return;
        }
        log.debug("Setting up context"); //$NON-NLS-1$
        FileshareBootstrapContext c = (FileshareBootstrapContext) ctx;

        @NonNull
        ServiceStructuralObjectImpl service = getBootstrapFileshareConfigService(em, pinst);
        c.setFileshareService(service);
    }


    @Override
    public void completeContext ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl pinst, BootstrapContext ctx,
            ConfigUpdateInfo info )
            throws ModelObjectNotFoundException, ModelObjectValidationException, ModelObjectConflictException, ModelServiceException {

        if ( ! ( ctx instanceof FileshareBootstrapContext ) ) {
            return;
        }
        log.debug("Completing context"); //$NON-NLS-1$
        FileshareBootstrapContext c = (FileshareBootstrapContext) ctx;

        FileshareConfiguration fc = c.getFileshareConfig();
        c.setFileshareConfig(updateFileshareConfig(fc, em, pinst, info));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.bootstrap.BootstrapPlugin#contributeAutoRun(eu.agno3.orchestrator.bootstrap.BootstrapContext)
     */
    @Override
    public void contributeAutoRun ( BootstrapContext ctx ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( ! ( ctx instanceof FileshareBootstrapContext ) ) {
            return;
        }
        log.debug("Setting up context"); //$NON-NLS-1$
        FileshareBootstrapContext c = (FileshareBootstrapContext) ctx;
        c.setFileshareConfig((FileshareConfiguration) this.rctx.getServiceService().getServiceConfiguration(c.getFileshareService()));
    }


    @Override
    public void contributeJobs ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance, BootstrapContext ctx, List<Float> weights,
            List<Job> jobs ) throws ModelObjectNotFoundException, ModelServiceException {

        if ( ! ( ctx instanceof FileshareBootstrapContext ) ) {
            return;
        }
        log.debug("Contribute jobs"); //$NON-NLS-1$
        FileshareBootstrapContext c = (FileshareBootstrapContext) ctx;

        weights.add(0.4f);
        jobs.add(
            makeFileshareConfigJob(
                instance,
                em,
                getBootstrapFileshareConfigService(em, instance),
                (FileshareConfigurationImpl) c.getFileshareConfig(),
                ctx));
    }


    /**
     * @param em
     * @param bootstrapInstance
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private @NonNull ServiceStructuralObjectImpl getBootstrapFileshareConfigService ( @NonNull EntityManager em,
            @NonNull InstanceStructuralObjectImpl bootstrapInstance ) throws ModelObjectNotFoundException, ModelServiceException {
        Set<ServiceStructuralObject> servicesOfType = this.rctx.getServiceService()
                .getServicesOfType(em, bootstrapInstance, FileshareServiceTypeDescriptor.FILESHARE_SERVICE_TYPE);

        if ( servicesOfType.size() != 1 ) {
            throw new ModelServiceException("Fileshare config does not exist or is not unique"); //$NON-NLS-1$
        }

        ServiceStructuralObjectImpl first = (ServiceStructuralObjectImpl) servicesOfType.iterator().next();

        if ( first == null ) {
            throw new ModelServiceException("Fileshare config is null"); //$NON-NLS-1$
        }

        return first;
    }


    /**
     * @param fsConfig
     * @param em
     * @param persistentInstance
     * @param info
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     */
    private @NonNull FileshareConfigurationImpl updateFileshareConfig ( FileshareConfiguration fsConfig, @NonNull EntityManager em,
            @NonNull InstanceStructuralObjectImpl persistentInstance, ConfigUpdateInfo info )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException, ModelObjectConflictException {
        ServiceStructuralObjectImpl bootstrapFileshareService = getBootstrapFileshareConfigService(em, persistentInstance);
        em.refresh(bootstrapFileshareService);
        if ( fsConfig == null ) {
            return (@NonNull FileshareConfigurationImpl) this.rctx.getServiceService().getServiceConfiguration(em, bootstrapFileshareService);
        }
        return this.rctx.getServiceService().updateServiceConfiguration(em, bootstrapFileshareService, (FileshareConfigurationImpl) fsConfig, info);
    }


    /**
     * @param instance
     * @param em
     * @param newFileshareConfig
     * @param ctx
     * @return
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "null" )
    private FileshareConfigurationJob makeFileshareConfigJob ( @NonNull InstanceStructuralObject instance, @NonNull EntityManager em,
            @NonNull ServiceStructuralObject fcService, FileshareConfigurationImpl newFileshareConfig, BootstrapContext ctx )
            throws ModelServiceException {
        FileshareConfigurationJob fcjob = new FileshareConfigurationJob();
        FileshareConfiguration effective = this.rctx.getInheritanceService()
                .getEffective(em, PersistenceUtil.unproxyDeep(newFileshareConfig), newFileshareConfig.getType());
        fcjob.setAnchor(instance);
        fcjob.setFileshareConfig(effective);
        fcjob.setInstanceId(instance.getId());
        fcjob.getApplyInfo().setForce(true);
        fcjob.setNoRestart(true);
        fcjob.setService(fcService);

        try {
            setupCreateInitialUser(ctx, fcjob);
        }
        catch (
            IOException |
            SecurityManagementException e ) {
            throw new ModelServiceException("Failed to setup initial user creation", e); //$NON-NLS-1$
        }

        UUID agentId = instance.getAgentId();
        if ( agentId == null ) {
            throw new ModelServiceException("Agent id unknown"); //$NON-NLS-1$
        }
        fcjob.setTarget(new AgentTarget(agentId));
        fcjob.setOwner(getUserPrincipal());
        return fcjob;
    }


    /**
     * @param ctx
     * @param fcjob
     * @throws SecurityManagementException
     * @throws IOException
     */
    void setupCreateInitialUser ( BootstrapContext ctx, FileshareConfigurationJob fcjob ) throws IOException, SecurityManagementException {
        if ( ctx instanceof FileshareBootstrapContext ) {
            FileshareBootstrapContext fc = (FileshareBootstrapContext) ctx;
            if ( fc.getCreateUser() ) {
                if ( StringUtils.isBlank(fc.getCreateUserName()) || StringUtils.isBlank(fc.getCreateUserPassword()) ) {
                    throw new SecurityManagementException("Username or password is empty"); //$NON-NLS-1$
                }
                fcjob.setCreateInitialUser(true);
                fcjob.setCreateInitialUserName(fc.getCreateUserName());
                fcjob.setCreateInitialUserPasswordHash(generatePasswordHash(fc.getCreateUserPassword()).export());
                Set<String> roles = new HashSet<>();
                roles.add("ADMIN_CREATED_USER"); //$NON-NLS-1$
                if ( fc.getCreateUserAdmin() ) {
                    roles.add("ADMIN"); //$NON-NLS-1$
                }
                else {
                    roles.add("DEFAULT_USER"); //$NON-NLS-1$
                }
            }
        }
    }


    private SCryptResult generatePasswordHash ( String password ) throws SecurityManagementException {
        return this.localUserService.generatePasswordHash(password, false);
    }


    /**
     * @return
     * @throws ModelServiceException
     */
    private static UserPrincipal getUserPrincipal () throws ModelServiceException {
        if ( !SecurityUtils.getSubject().isAuthenticated() ) {
            return null;
        }
        Collection<UserPrincipal> ups = SecurityUtils.getSubject().getPrincipals().byType(UserPrincipal.class);

        if ( ups.size() != 1 ) {
            throw new ModelServiceException("Failed to determine user principal"); //$NON-NLS-1$
        }

        return ups.iterator().next();
    }
}
