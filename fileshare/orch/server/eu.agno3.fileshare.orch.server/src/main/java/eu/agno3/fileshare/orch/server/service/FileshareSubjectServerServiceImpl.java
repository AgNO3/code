/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.server.service;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;
import javax.management.MalformedObjectNameException;
import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.orch.common.jmx.FileshareSubjectJMXRequest;
import eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService;
import eu.agno3.fileshare.orch.common.service.FileshareSubjectServerServiceDescriptor;
import eu.agno3.fileshare.service.admin.SubjectServiceMBean;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.RemoteCallErrorException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.runtime.jmsjmx.JMSJMXClient;
import eu.agno3.runtime.jmsjmx.JMSJMXClientFactory;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    FileshareSubjectServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService",
    targetNamespace = FileshareSubjectServerServiceDescriptor.NAMESPACE,
    serviceName = FileshareSubjectServerServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/fileshare/manage/subject" )
public class FileshareSubjectServerServiceImpl implements FileshareSubjectServerService {

    private ObjectAccessControl authz;
    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private JMSJMXClientFactory jmxClient;
    private AgentServerService agentService;


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
    protected synchronized void setAgentService ( AgentServerService ass ) {
        this.agentService = ass;
    }


    protected synchronized void unsetAgentService ( AgentServerService ass ) {
        if ( this.agentService == ass ) {
            this.agentService = null;
        }
    }


    @Reference
    protected synchronized void setJMXClient ( JMSJMXClientFactory jc ) {
        this.jmxClient = jc;
    }


    protected synchronized void unsetJMXClient ( JMSJMXClientFactory jc ) {
        if ( this.jmxClient == jc ) {
            this.jmxClient = null;
        }
    }


    /**
     * @param target
     * @return
     * @throws ModelServiceException
     */
    private SubjectServiceMBean getProxy ( ServiceStructuralObject sos, MessageTarget target ) throws ModelServiceException {
        SubjectServiceMBean proxy;
        try {
            JMSJMXClient client = this.jmxClient.getClient(getJMXRequestPrototype(sos, target));
            proxy = client.getProxyMBean(SubjectServiceMBean.class);
        }
        catch ( MalformedObjectNameException e ) {
            throw new ModelServiceException();
        }
        return proxy;
    }


    private InstanceStructuralObject getInstance ( ServiceStructuralObject service ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();

        @NonNull
        ServiceStructuralObjectImpl persistent = PersistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service.getId());
        @SuppressWarnings ( "null" )
        Optional<? extends @NonNull AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);

        if ( !parent.isPresent() || ! ( parent.get() instanceof InstanceStructuralObject ) ) {
            throw new ModelObjectNotFoundException(InstanceStructuralObject.class, service.getId());
        }

        return (InstanceStructuralObject) parent.get();
    }


    /**
     * @param e
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws FileshareException
     * @throws RemoteCallErrorException
     */
    private AgentCommunicationErrorException handleJMXException ( InstanceStructuralObject instance, UndeclaredThrowableException e )
            throws ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, RemoteCallErrorException, FileshareException {
        Throwable e2 = FileshareServiceExceptionHandler.handleJMXException(e, this.agentService, instance);
        return new AgentCommunicationErrorException("Agent/service communication failed: " + e2.getMessage(), //$NON-NLS-1$
            this.agentService.handleCommFault(e2, instance),
            e2);
    }


    private static FileshareSubjectJMXRequest getJMXRequestPrototype ( ServiceStructuralObject sos, MessageTarget target ) {
        FileshareSubjectJMXRequest req = new FileshareSubjectJMXRequest();
        req.setService(sos);
        req.setTarget(target);
        return req;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#getSubject(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public Subject getSubject ( ServiceStructuralObject context, UUID id ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getSubject(id);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#getSubjectInfo(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public SubjectQueryResult getSubjectInfo ( ServiceStructuralObject context, UUID id ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getSubjectInfo(id);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#getUserInfo(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public SubjectQueryResult getUserInfo ( ServiceStructuralObject context, UserPrincipal principal ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getUserInfo(principal);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#querySubjects(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.String, int)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<SubjectQueryResult> querySubjects ( ServiceStructuralObject context, String query, int limit ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).querySubjects(query, limit);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#querySubjectsExcludingMembers(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.String, java.util.UUID, int)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<SubjectQueryResult> querySubjectsExcludingMembers ( ServiceStructuralObject context, String query, UUID groupId, int limit )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).querySubjectsExcludingMembers(query, groupId, limit);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#addRole(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void addRole ( ServiceStructuralObject context, UUID id, String role ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).addRole(id, role);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#setRoles(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.Set)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void setRoles ( ServiceStructuralObject context, UUID id, Set<String> roles ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).setRoles(id, roles);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#removeRole(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void removeRole ( ServiceStructuralObject context, UUID id, String role ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).removeRole(id, role);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#getEffectiveRoles(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public Set<String> getEffectiveRoles ( ServiceStructuralObject context, UUID id ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getEffectiveRoles(id);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#getSubjectRootLabel(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public SecurityLabel getSubjectRootLabel ( ServiceStructuralObject context, UUID subjectId ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getSubjectRootLabel(subjectId);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#setSubjectRootSecurityLabel(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void setSubjectRootSecurityLabel ( ServiceStructuralObject context, UUID id, String label ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).setSubjectRootSecurityLabel(id, label);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService#setSubjectRootSecurityLabelRecursive(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void setSubjectRootSecurityLabelRecursive ( ServiceStructuralObject context, UUID id, String label ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).setSubjectRootSecurityLabelRecursive(id, label);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }

}
