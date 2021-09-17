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
import java.util.UUID;

import javax.jws.WebService;
import javax.management.MalformedObjectNameException;
import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserLimitExceededException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserCreateData;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.orch.common.jmx.FileshareUserJMXRequest;
import eu.agno3.fileshare.orch.common.service.FileshareUserServerService;
import eu.agno3.fileshare.orch.common.service.FileshareUserServerServiceDescriptor;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
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
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    FileshareUserServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.fileshare.orch.common.service.FileshareUserServerService",
    targetNamespace = FileshareUserServerServiceDescriptor.NAMESPACE,
    serviceName = FileshareUserServerServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/fileshare/manage/user" )
public class FileshareUserServerServiceImpl implements FileshareUserServerService {

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
    private UserServiceMBean getProxy ( ServiceStructuralObject sos, MessageTarget target ) throws ModelServiceException {
        UserServiceMBean proxy;
        try {
            JMSJMXClient client = this.jmxClient.getClient(getJMXRequestPrototype(sos, target));
            proxy = client.getProxyMBean(UserServiceMBean.class);
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
        return new AgentCommunicationErrorException(
            "Agent/service communication failed: " + e2.getMessage(), //$NON-NLS-1$
            this.agentService.handleCommFault(e2, instance),
            e2);
    }


    private static FileshareUserJMXRequest getJMXRequestPrototype ( ServiceStructuralObject sos, MessageTarget target ) {
        FileshareUserJMXRequest req = new FileshareUserJMXRequest();
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#getUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public User getUser ( ServiceStructuralObject context, UUID userId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).getUser(userId);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#enableLocalUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void enableLocalUser ( ServiceStructuralObject context, UUID userId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).enableLocalUser(userId);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#disableLocalUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void disableLocalUser ( ServiceStructuralObject context, UUID userId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).disableLocalUser(userId);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#getLocalUserInfo(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public UserInfo getLocalUserInfo ( ServiceStructuralObject context, UUID userId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).getLocalUserInfo(userId);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#getUserGroups(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<Group> getUserGroups ( ServiceStructuralObject context, UUID userId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).getUserGroups(userId);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#getUserGroupClosure(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<Group> getUserGroupClosure ( ServiceStructuralObject context, UUID userId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).getUserGroupClosure(userId);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#listUsers(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      int, int)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public List<User> listUsers ( ServiceStructuralObject context, int off, int limit ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).listUsers(off, limit);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#getUserCount(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public long getUserCount ( ServiceStructuralObject context ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).getUserCount();
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#createLocalUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.fileshare.model.UserCreateData)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public User createLocalUser ( ServiceStructuralObject context, UserCreateData userData )
            throws FileshareException, ModelObjectNotFoundException, UserLimitExceededException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).createLocalUser(userData);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#deleteUsers(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.List)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void deleteUsers ( ServiceStructuralObject context, List<UUID> userIds ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).deleteUsers(userIds);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#changePassword(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void changePassword ( ServiceStructuralObject context, UUID userId, String newPassword ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).changePassword(userId, newPassword);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#getUserDetails(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public UserDetails getUserDetails ( ServiceStructuralObject context, UUID userId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).getUserDetails(userId);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#updateUserLabel(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.String)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void updateUserLabel ( ServiceStructuralObject context, UUID id, String label ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).updateUserLabel(id, label);
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
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#updateUserDetails(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, eu.agno3.fileshare.model.UserDetails)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public UserDetails updateUserDetails ( ServiceStructuralObject context, UUID userId, UserDetails data ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            return getProxy(context, this.agentService.ensureAgentOnline(i)).updateUserDetails(userId, data);
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
     * @throws FileshareException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#updateUserQuota(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, java.lang.Long)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void updateUserQuota ( ServiceStructuralObject context, UUID userId, Long quota ) throws ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, FileshareException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).updateUserQuota(userId, quota);
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
     * @throws FileshareException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#enableUserRoot(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void enableUserRoot ( ServiceStructuralObject context, UUID id ) throws ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, FileshareException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).enableUserRoot(id);
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
     * @throws FileshareException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#disableUserRoot(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void disableUserRoot ( ServiceStructuralObject context, UUID id ) throws ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, FileshareException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).disableUserRoot(id);
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
     * @throws FileshareException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     *
     * @see eu.agno3.fileshare.orch.common.service.FileshareUserServerService#setUserExpiry(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.util.UUID, org.joda.time.DateTime)
     */
    @Override
    @RequirePermissions ( "fileshare:manage:user" )
    public void setUserExpiry ( ServiceStructuralObject context, UUID id, DateTime expiration ) throws ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, FileshareException, AgentCommunicationErrorException, AgentOfflineException {
        InstanceStructuralObject i = getInstance(context);
        try {
            getProxy(context, this.agentService.ensureAgentOnline(i)).setUserExpiry(id, expiration);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(i, e);
        }
    }

}
