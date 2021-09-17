/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationTestJob;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyBuilder;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext;
import eu.agno3.orchestrator.config.model.realm.server.util.ModelObjectValidationUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.PathUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.ConfigTestService;
import eu.agno3.orchestrator.config.model.realm.service.ConfigTestServiceDescriptor;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRegistry;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.util.log.LogWriter;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ConfigTestService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.ConfigTestService",
    targetNamespace = ConfigTestServiceDescriptor.NAMESPACE,
    serviceName = ConfigTestServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/config/test" )
public class ConfigTestServiceImpl implements ConfigTestService {

    private static final Logger log = Logger.getLogger(ConfigTestServiceImpl.class);

    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private ObjectAccessControl authz;

    private InheritanceProxyBuilder inheritanceUtil;
    private XmlMarshallingService marshallingService;
    private ModelObjectValidationUtil validationUtil;
    private ServiceServerService serviceService;

    private JobCoordinator jobCoordinator;
    private AgentServerService agentService;

    private ConfigTestPluginRegistry testPluginRegistry;

    private ConfigTestResultCache resultCache;


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
        }
    }


    @Reference
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }


    @Reference
    protected synchronized void setInheritanceUtil ( InheritanceProxyBuilder iu ) {
        this.inheritanceUtil = iu;
    }


    protected synchronized void unsetInheritanceUtil ( InheritanceProxyBuilder iu ) {
        if ( this.inheritanceUtil == iu ) {
            this.inheritanceUtil = null;
        }
    }


    @Reference
    protected synchronized void setMarshallingService ( XmlMarshallingService xms ) {
        this.marshallingService = xms;
    }


    protected synchronized void unsetMarshallingService ( XmlMarshallingService xms ) {
        if ( this.marshallingService == xms ) {
            this.marshallingService = null;
        }
    }


    @Reference
    protected synchronized void setValidationUtil ( ModelObjectValidationUtil vu ) {
        this.validationUtil = vu;
    }


    protected synchronized void unsetValidationUtil ( ModelObjectValidationUtil vu ) {
        if ( this.validationUtil == vu ) {
            this.validationUtil = null;
        }
    }


    @Reference
    protected synchronized void setConfigTestPluginRegistry ( ConfigTestPluginRegistry ctpr ) {
        this.testPluginRegistry = ctpr;
    }


    protected synchronized void unsetConfigTestPluginRegistry ( ConfigTestPluginRegistry ctpr ) {
        if ( this.testPluginRegistry == ctpr ) {
            this.testPluginRegistry = null;
        }
    }


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator jc ) {
        this.jobCoordinator = jc;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator jc ) {
        if ( this.jobCoordinator == jc ) {
            this.jobCoordinator = null;
        }
    }


    @Reference
    protected synchronized void setAgentService ( AgentServerService ass ) {
        this.agentService = ass;
    }


    protected synchronized void unsetAgentService ( AgentServerService ass ) {
        if ( this.agentService == ass ) {
            this.agentService = null;
        }
    }


    @Reference
    protected synchronized void setResultCache ( ConfigTestResultCache ctrc ) {
        this.resultCache = ctrc;
    }


    protected synchronized void unsetResultCache ( ConfigTestResultCache ctrc ) {
        if ( this.resultCache == ctrc ) {
            this.resultCache = null;
        }
    }


    @Reference
    protected synchronized void setServiceService ( ServiceServerService ss ) {
        this.serviceService = ss;
    }


    protected synchronized void unsetServiceService ( ServiceServerService ss ) {
        if ( this.serviceService == ss ) {
            this.serviceService = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws AgentDetachedException
     * @throws JobQueueException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigTestService#test(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject, java.lang.String, java.lang.String,
     *      eu.agno3.orchestrator.config.model.validation.ConfigTestParams)
     */
    @Override
    @RequirePermissions ( "config:test" )
    public ConfigTestResultImpl test ( StructuralObject object, ConfigurationObject rootConfig, String type, String path, ConfigTestParams params )
            throws ModelServiceException, ModelObjectException, AgentDetachedException, JobQueueException {
        if ( log.isDebugEnabled() ) {
            log.debug("Called test with parameters " + params); //$NON-NLS-1$
        }

        this.authz.checkAccess(object, "config:test"); //$NON-NLS-1$

        if ( rootConfig == null || object == null || type == null ) {
            throw new ModelServiceException();
        }

        @NonNull
        EntityManager em = this.sctx.createConfigEM();

        ConcreteObjectTypeDescriptor<@Nullable ? extends ConfigurationObject, @Nullable ? extends AbstractConfigurationObject<?>> typeDescriptor = this.sctx
                .getObjectTypeRegistry().getConcrete(type);

        @NonNull
        AbstractStructuralObjectImpl panchor = PersistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, object.getId());

        StructuralObject ptgt = getTestTarget(em, panchor);

        ConfigurationObject effc = getEffectiveConfig(em, rootConfig, path, panchor);
        ConfigurationObject resc = doResolv(path, typeDescriptor.getObjectType(), effc);

        if ( resc == null ) {
            throw new ModelObjectNotFoundException(new ModelObjectNotFoundFault());
        }

        @NonNull
        Map<@NonNull ServiceStructuralObject, @NonNull ConfigurationInstance> contextConfig = this.serviceService
                .getEffectiveContextConfigs(em, panchor);

        if ( log.isDebugEnabled() ) {
            log.debug("Have config " + effc); //$NON-NLS-1$
        }

        ConfigTestResultImpl r = new ConfigTestResultImpl();

        List<ViolationEntry> violations = this.validationUtil.validateEffective(rootConfig, false, effc, contextConfig);
        if ( !violations.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found violations " + violations); //$NON-NLS-1$
            }

            r.setViolations(violations);
            boolean foundError = false;

            for ( ViolationEntry ve : violations ) {
                if ( ve.getLevel() == ViolationLevel.ERROR ) {
                    foundError = true;
                }
            }
            if ( foundError ) {
                r.setState(ConfigTestState.VALIDATION);
                return r;
            }
        }

        ConfigTestPlugin<? extends ConfigurationObject> tp = this.testPluginRegistry.getTestPlugin(resc.getType());
        Set<ConfigTestPluginRunOn> runOn = EnumSet.of(ConfigTestPluginRunOn.AGENT);

        if ( tp != null ) {
            runOn = tp.getRunOn();
        }

        ConfigTestResult btr = r.withType("base"); //$NON-NLS-1$

        @NonNull
        JobTarget jobTarget;
        if ( runOn.contains(ConfigTestPluginRunOn.AGENT) && ptgt instanceof InstanceStructuralObject ) {
            InstanceStructuralObject inst = (InstanceStructuralObject) ptgt;
            if ( this.agentService.isAgentOnline(inst) ) {
                btr.info("RUNNING_ON_AGENT"); //$NON-NLS-1$
                jobTarget = new AgentTarget(this.agentService.getAgentID(inst));
            }
            else {
                jobTarget = new AnyServerTarget();
                btr.warn("FALLBACK_TO_SERVER"); //$NON-NLS-1$
            }
        }
        else if ( runOn.contains(ConfigTestPluginRunOn.SERVER) ) {
            btr.info("RUNNING_ON_SERVER"); //$NON-NLS-1$
            jobTarget = new AnyServerTarget();
        }
        else {
            btr.error("NO_TEST"); //$NON-NLS-1$
            r.setState(ConfigTestState.NO_TEST);
            return r;
        }

        ConfigurationTestJob tj = new ConfigurationTestJob();

        if ( log.isDebugEnabled() ) {
            log.debug("Job target is " + jobTarget); //$NON-NLS-1$
        }

        tj.setTarget(jobTarget);
        tj.setDeadline(DateTime.now().plusMinutes(5));
        tj.setEffectiveConfig(resc);
        tj.setInitialResult(r);
        tj.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        tj.setParameters(params);

        JobInfo ji = this.jobCoordinator.queueJob(tj);
        r.setState(ConfigTestState.QUEUED);
        r.setTestId(ji.getJobId());
        r.setDefaultObjectType(type);

        if ( log.isDebugEnabled() ) {
            log.debug("Test id is " + r.getTestId()); //$NON-NLS-1$
        }
        return r;
    }


    /**
     * @param em
     * @param panchor
     * @return
     * @throws ModelServiceException
     */
    @NonNull
    StructuralObject getTestTarget ( EntityManager em, AbstractStructuralObjectImpl panchor ) throws ModelServiceException {
        if ( panchor instanceof GroupStructuralObject || panchor instanceof InstanceStructuralObject ) {
            return panchor;
        }
        else if ( panchor instanceof ServiceStructuralObject ) {
            Optional<? extends AbstractStructuralObjectImpl> p = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, panchor);

            if ( !p.isPresent() || ! ( p.get() instanceof InstanceStructuralObject ) ) {
                throw new ModelServiceException("Invalid service parent"); //$NON-NLS-1$
            }
            AbstractStructuralObjectImpl parent = p.get();
            if ( parent != null ) {
                return parent;
            }
        }

        throw new ModelServiceException("Unknown anchor type"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigTestService#update(eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl)
     */
    @Override
    @RequirePermissions ( "config:test" )
    public ConfigTestResultImpl update ( ConfigTestResultImpl r )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException, ModelObjectException {

        if ( r.getState() == null || r.getTestId() == null || !EnumSet.of(ConfigTestState.QUEUED, ConfigTestState.RUNNING).contains(r.getState()) ) {
            log.debug("Not valid"); //$NON-NLS-1$
            return r;
        }

        JobInfo ji;
        try {
            ji = this.jobCoordinator.getJobInfo(r.getTestId());
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to find test job", e); //$NON-NLS-1$
            r.setState(ConfigTestState.UNKNOWN);
            return r;
        }

        UserPrincipal up = SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);

        if ( up == null || !up.equals(ji.getOwner()) ) {
            throw new ModelObjectNotFoundException("Is the test job of another user", new ModelObjectNotFoundFault(Job.class, r.getTestId())); //$NON-NLS-1$
        }

        JobState js = ji.getState();

        if ( log.isDebugEnabled() ) {
            log.debug("Job state is " + js); //$NON-NLS-1$
        }

        if ( EnumSet.of(JobState.QUEUED, JobState.RUNNABLE).contains(js) ) {
            r.setState(ConfigTestState.QUEUED);
        }
        else if ( JobState.RUNNING == js ) {
            r.setState(ConfigTestState.RUNNING);
            // if there is state, update from it

            ConfigTestResultImpl ctr = this.resultCache.get(r.getTestId());
            if ( ctr != null ) {
                return ctr;
            }
        }
        else if ( JobState.FINISHED == js ) {
            // update state according to result
            // fail if there is none
            ConfigTestResultImpl ctr = this.resultCache.get(r.getTestId());
            if ( ctr == null ) {
                r.setState(ConfigTestState.FAILURE);
            }
            else {
                return ctr;
            }
        }
        else {
            r.setState(ConfigTestState.FAILURE);
        }

        return r;
    }


    /**
     * @param em
     * @param rootConfig
     * @param path
     * @param panchor
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    private ConfigurationObject getEffectiveConfig ( @NonNull EntityManager em, ConfigurationObject rootConfig, String path,
            AbstractStructuralObjectImpl panchor ) throws ModelServiceException, ModelObjectException {
        @NonNull
        InheritanceProxyContext pc = this.inheritanceUtil.makeProxyContext(em, rootConfig.getType(), panchor, rootConfig.getType());

        @NonNull
        ConfigurationObject effective = pc.getInheritanceProxyBuilder().makeInheritanceProxy(pc, rootConfig.getType(), rootConfig, panchor);

        if ( log.isTraceEnabled() ) {
            try ( LogWriter logWriter = new LogWriter(log, Level.TRACE) ) {
                this.marshallingService.marshall(effective, XMLOutputFactory.newFactory().createXMLStreamWriter(logWriter));
            }
            catch (
                XMLBindingException |
                XMLStreamException |
                FactoryConfigurationError |
                IOException e ) {
                log.warn("Failed to dump config", e); //$NON-NLS-1$
            }
        }

        return effective;
    }


    /**
     * @param path
     * @param cfgClass
     * @param effective
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private <T extends ConfigurationObject> T doResolv ( String path, Class<T> cfgClass, ConfigurationObject effective )
            throws ModelObjectNotFoundException, ModelServiceException {
        ConfigurationObject resolved = PathUtil.resolvePath(effective, path);

        if ( log.isDebugEnabled() ) {
            log.debug("Resolved type is " + resolved.getType().getName()); //$NON-NLS-1$
            try ( LogWriter logWriter = new LogWriter(log, Level.DEBUG) ) {
                this.marshallingService.marshall(resolved, XMLOutputFactory.newFactory().createXMLStreamWriter(logWriter));
            }
            catch (
                XMLBindingException |
                XMLStreamException |
                FactoryConfigurationError |
                IOException e ) {
                log.warn("Failed to dump config", e); //$NON-NLS-1$
            }
        }

        if ( !resolved.getType().isAssignableFrom(cfgClass) ) {
            throw new ModelServiceException(String.format(
                "Invalid configuration type %s expected is %s", //$NON-NLS-1$
                resolved.getType().getName(),
                cfgClass.getName()));
        }

        return (T) resolved;
    }

}
