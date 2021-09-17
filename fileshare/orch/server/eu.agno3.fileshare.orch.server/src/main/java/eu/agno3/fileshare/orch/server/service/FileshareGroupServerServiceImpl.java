/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.server.service;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Locale;
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
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.orch.common.jmx.FileshareGroupJMXRequest;
import eu.agno3.fileshare.orch.common.service.FileshareGroupServerService;
import eu.agno3.fileshare.orch.common.service.FileshareGroupServerServiceDescriptor;
import eu.agno3.fileshare.service.admin.GroupServiceMBean;
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
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    FileshareGroupServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.fileshare.orch.common.service.FileshareGroupServerService",
    targetNamespace = FileshareGroupServerServiceDescriptor.NAMESPACE,
    serviceName = FileshareGroupServerServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/fileshare/manage/group" )
public class FileshareGroupServerServiceImpl implements FileshareGroupServerService {

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
    private GroupServiceMBean getProxy ( ServiceStructuralObject sos, MessageTarget target ) throws ModelServiceException {
        GroupServiceMBean proxy;
        try {
            JMSJMXClient client = this.jmxClient.getClient(getJMXRequestPrototype(sos, target));
            proxy = client.getProxyMBean(GroupServiceMBean.class);
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


    private static FileshareGroupJMXRequest getJMXRequestPrototype ( ServiceStructuralObject sos, MessageTarget target ) {
        FileshareGroupJMXRequest req = new FileshareGroupJMXRequest();
        req.setService(sos);
        req.setTarget(target);
        return req;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#listGroups(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      int, int)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<Group> listGroups ( ServiceStructuralObject context, int off, int limit ) throws FileshareException, ModelObjectNotFoundException,
            ModelServiceException, AgentDetachedException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).listGroups(off, limit);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#getGroupCount(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public long getGroupCount ( ServiceStructuralObject context ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getGroupCount();
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#getGroup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public Group getGroup ( ServiceStructuralObject context, UUID id ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getGroup(id);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#getGroupInfo(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public GroupQueryResult getGroupInfo ( ServiceStructuralObject context, String subjectName ) throws FileshareException,
            ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getGroupInfo(subjectName);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#createGroup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.fileshare.model.Group, boolean)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public Group createGroup ( ServiceStructuralObject context, Group group, boolean createRoot ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).createGroup(group, createRoot);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#setGroupLocale(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.Locale)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void setGroupLocale ( ServiceStructuralObject context, UUID id, Locale groupLocale ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).setGroupLocale(id, groupLocale);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#setNotificationOverride(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void setNotificationOverride ( ServiceStructuralObject context, UUID id, String overrideAddress ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).setNotificationOverride(id, overrideAddress);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#setNotificationDisabled(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, boolean)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void setNotificationDisabled ( ServiceStructuralObject context, UUID id, boolean disableNotifications ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).setNotificationDisabled(id, disableNotifications);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#updateGroupQuota(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.Long)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void updateGroupQuota ( ServiceStructuralObject context, UUID groupId, Long quota ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).updateGroupQuota(groupId, quota);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#removeMembers(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.List)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void removeMembers ( ServiceStructuralObject context, UUID groupId, List<UUID> subjectIds ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).removeMembers(groupId, subjectIds);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#removeFromGroups(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.Set)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void removeFromGroups ( ServiceStructuralObject context, UUID userId, Set<UUID> groupIds ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).removeFromGroups(userId, groupIds);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#addMembers(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.List)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void addMembers ( ServiceStructuralObject context, UUID groupId, List<UUID> subjectIds ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).addMembers(groupId, subjectIds);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#removeFromGroup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void removeFromGroup ( ServiceStructuralObject context, UUID userId, UUID groupId ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).removeFromGroup(userId, groupId);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#getMembers(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<Subject> getMembers ( ServiceStructuralObject context, UUID groupId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).getMembers(groupId);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#addToGroup(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void addToGroup ( ServiceStructuralObject context, UUID userId, UUID groupId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).addToGroup(userId, groupId);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#addToGroups(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.util.Set)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void addToGroups ( ServiceStructuralObject context, UUID id, Set<UUID> groupIds ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).addToGroups(id, groupIds);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#deleteGroups(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.List)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void deleteGroups ( ServiceStructuralObject context, List<UUID> ids ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.getMessageTarget(i)).deleteGroups(ids);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#queryGroupsExcludingUserGroups(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.String, java.util.UUID, int)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<GroupQueryResult> queryGroupsExcludingUserGroups ( ServiceStructuralObject context, String query, UUID userId, int limit )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).queryGroupsExcludingUserGroups(query, userId, limit);
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
     * @see eu.agno3.fileshare.orch.common.service.FileshareGroupServerService#queryGroups(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.String, int)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<GroupQueryResult> queryGroups ( ServiceStructuralObject context, String query, int limit ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.getMessageTarget(i)).queryGroups(query, limit);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }

}
