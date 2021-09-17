/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.12.2014 by mbechler
 */
package eu.agno3.orchestrator.realms.server.internal;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;

import javax.jws.WebService;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.orchestrator.realms.KeyInfo;
import eu.agno3.orchestrator.realms.RealmInfo;
import eu.agno3.orchestrator.realms.RealmManagementException;
import eu.agno3.orchestrator.realms.RealmManagementMXBean;
import eu.agno3.orchestrator.realms.RealmManagerJMXRequest;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.orchestrator.realms.service.RealmManagementService;
import eu.agno3.orchestrator.realms.service.RealmManagementServiceDescriptor;
import eu.agno3.runtime.jmsjmx.JMSJMXClient;
import eu.agno3.runtime.jmsjmx.JMSJMXClientFactory;
import eu.agno3.runtime.messaging.CallErrorException;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.security.credentials.WrappedCredentials;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RealmManagementService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.realms.service.RealmManagementService",
    targetNamespace = RealmManagementServiceDescriptor.NAMESPACE,
    serviceName = RealmManagementServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/crypto/realms/manage" )
public class RealmManagementServiceImpl implements RealmManagementService {

    private static final Logger log = Logger.getLogger(RealmManagementServiceImpl.class);

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
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws RealmManagementException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#getRealms(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "realms:list" )
    public List<RealmInfo> getRealms ( InstanceStructuralObject instance ) throws ModelObjectNotFoundException, ModelServiceException,
            RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.ensureAgentOnline(instance)).getRealms();
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#getRealm(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:list" )
    public RealmInfo getRealm ( InstanceStructuralObject instance, String realm ) throws ModelObjectNotFoundException, ModelServiceException,
            RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.ensureAgentOnline(instance)).getRealm(realm);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#addKeys(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, eu.agno3.orchestrator.realms.RealmType, java.lang.String, java.util.List)
     */
    @Override
    @RequirePermissions ( "realms:manage:addkey" )
    public void addKeys ( InstanceStructuralObject instance, String realm, RealmType type, String keytab, List<KeyData> kis )
            throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException {
        if ( kis == null ) {
            return;
        }
        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).addKeys(realm, type, keytab, kis);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#createKeytab(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, eu.agno3.orchestrator.realms.RealmType, java.lang.String, java.util.List)
     */
    @Override
    @RequirePermissions ( "realms:manage:createkeytab" )
    public void createKeytab ( InstanceStructuralObject instance, String realm, RealmType type, String keytab, List<KeyData> initialKeys )
            throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException {
        List<KeyData> keys = initialKeys;
        if ( keys == null ) {
            keys = Collections.EMPTY_LIST;
        }

        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).createKeytab(realm, type, keytab, keys);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#removeKeys(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, eu.agno3.orchestrator.realms.RealmType, java.lang.String, java.util.List)
     */
    @Override
    @RequirePermissions ( "realms:manage:removekey" )
    public void removeKeys ( InstanceStructuralObject instance, String realm, RealmType type, String keytab, List<KeyInfo> kis )
            throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException {
        if ( kis == null ) {
            return;
        }

        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).removeKeys(realm, type, keytab, kis);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#removeKeytab(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, eu.agno3.orchestrator.realms.RealmType, java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:manage:removekeytab" )
    public void removeKeytab ( InstanceStructuralObject instance, String realm, RealmType type, String keytab ) throws ModelObjectNotFoundException,
            ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).deleteKeytab(realm, type, keytab);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#joinAD(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, eu.agno3.runtime.security.credentials.WrappedCredentials)
     */
    @Override
    @RequirePermissions ( "realms:manage:joinad" )
    public void joinAD ( InstanceStructuralObject instance, String realm, WrappedCredentials creds ) throws ModelObjectNotFoundException,
            ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).joinAD(realm, creds);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#joinADWithMachinePassword(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:manage:joinad" )
    public void joinADWithMachinePassword ( InstanceStructuralObject instance, String realm, String machinePassword )
            throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).joinADWithMachinePassword(realm, machinePassword);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#leaveAD(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, eu.agno3.runtime.security.credentials.WrappedCredentials)
     */
    @Override
    @RequirePermissions ( "realms:manage:leavead" )
    public void leaveAD ( InstanceStructuralObject instance, String realm, WrappedCredentials creds ) throws ModelObjectNotFoundException,
            ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).leaveAD(realm, creds);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.realms.service.RealmManagementService#rekeyAD(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:manage:rekeyad" )
    public void rekeyAD ( InstanceStructuralObject instance, String realm ) throws ModelObjectNotFoundException, ModelServiceException,
            RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.ensureAgentOnline(instance)).rekeyAD(realm);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * @param target
     * @return
     * @throws ModelServiceException
     */
    private RealmManagementMXBean getProxy ( MessageTarget target ) throws ModelServiceException {
        RealmManagementMXBean proxy;
        try {
            JMSJMXClient client = this.jmxClient.getClient(getJMXRequestPrototype(target));
            proxy = client.getProxy(RealmManagementMXBean.class);
        }
        catch ( MalformedObjectNameException e ) {
            throw new ModelServiceException();
        }
        return proxy;
    }


    /**
     * @param e
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws RealmManagementException
     */
    private AgentCommunicationErrorException handleJMXException ( InstanceStructuralObject instance, UndeclaredThrowableException e )
            throws ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, RealmManagementException {
        Exception e2 = e;
        log.debug("JMX exception", e); //$NON-NLS-1$
        if ( e.getCause() instanceof InvocationTargetException ) {
            e2 = (InvocationTargetException) e.getCause();
        }
        else if ( e.getCause() instanceof IOException && e.getCause().getCause() instanceof CallErrorException ) {
            CallErrorException ce = (CallErrorException) e.getCause().getCause();

            if ( ce.getCause() instanceof MBeanException && ce.getCause().getCause() instanceof RealmManagementException ) {
                throw ( (RealmManagementException) ce.getCause().getCause() );
            }
            throw new RealmManagementException("Unknown failure", e.getCause()); //$NON-NLS-1$
        }
        return new AgentCommunicationErrorException(
            "Agent communication failed", //$NON-NLS-1$
            this.agentService.handleCommFault(e2.getCause(), instance),
            e2.getCause());
    }


    private static RealmManagerJMXRequest getJMXRequestPrototype ( MessageTarget target ) {
        RealmManagerJMXRequest req = new RealmManagerJMXRequest();
        req.setTarget(target);
        return req;
    }
}
