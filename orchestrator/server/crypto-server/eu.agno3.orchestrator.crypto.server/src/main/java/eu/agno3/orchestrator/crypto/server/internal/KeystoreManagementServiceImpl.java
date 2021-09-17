/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.12.2014 by mbechler
 */
package eu.agno3.orchestrator.crypto.server.internal;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import javax.jws.WebService;
import javax.management.MalformedObjectNameException;

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
import eu.agno3.orchestrator.crypto.jobs.GenerateKeyJob;
import eu.agno3.orchestrator.crypto.keystore.CertRequestData;
import eu.agno3.orchestrator.crypto.keystore.KeyInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyStoreInfo;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagementMXBean;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerJMXRequest;
import eu.agno3.orchestrator.crypto.service.KeystoreManagementService;
import eu.agno3.orchestrator.crypto.service.KeystoreManagementServiceDescriptor;
import eu.agno3.orchestrator.jobs.JobInfo;
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
    KeystoreManagementService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.crypto.service.KeystoreManagementService",
    targetNamespace = KeystoreManagementServiceDescriptor.NAMESPACE,
    serviceName = KeystoreManagementServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/crypto/keystore/manage" )
public class KeystoreManagementServiceImpl implements KeystoreManagementService {

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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.service.KeystoreManagementService#getKeystores(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      boolean)
     */
    @Override
    @RequirePermissions ( "keystores:list" )
    public List<KeyStoreInfo> getKeystores ( InstanceStructuralObject instance, boolean includeInternal ) throws ModelObjectNotFoundException,
            ModelServiceException, KeystoreManagerException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.ensureAgentOnline(instance)).getKeystores(includeInternal);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.service.KeystoreManagementService#getKeystoreAliases(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public List<String> getKeystoreAliases ( InstanceStructuralObject instance ) throws ModelObjectNotFoundException, ModelServiceException,
            KeystoreManagerException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.getMessageTarget(instance)).getKeystoreAliases();
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.service.KeystoreManagementService#getKeystore(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "keystores:list" )
    public KeyStoreInfo getKeystore ( InstanceStructuralObject instance, String keystore ) throws ModelObjectNotFoundException, ModelServiceException,
            KeystoreManagerException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.getMessageTarget(instance)).getKeyStoreInfo(keystore);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.service.KeystoreManagementService#getKeyInfo(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "keystores:list" )
    public KeyInfo getKeyInfo ( InstanceStructuralObject instance, String keystore, String keyAlias ) throws ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, KeystoreManagerException, AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.getMessageTarget(instance)).getKeyInfo(keystore, keyAlias);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    @Override
    @RequirePermissions ( "keystores:manage:generateKey" )
    public JobInfo generateKey ( InstanceStructuralObject instance, String keystore, String keyAlias, String keyType )
            throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException {
        try {

            GenerateKeyJob j = new GenerateKeyJob();
            j.setKeyStore(keystore);
            j.setKeyAlias(keyAlias);
            j.setKeyType(keyType);
            return this.agentService.submitJob(instance, j);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param algo
     * @param keyData
     * @param importChain
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    @Override
    @RequirePermissions ( "keystores:manage:importKey" )
    public void importKey ( InstanceStructuralObject instance, String keystore, String keyAlias, String algo, String keyData,
            List<String> importChain ) throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
                    AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.getMessageTarget(instance)).importKey(keystore, keyAlias, algo, keyData, importChain);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param certs
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    @Override
    @RequirePermissions ( "keystores:manage:updateChain" )
    public void updateChain ( InstanceStructuralObject instance, String keystore, String keyAlias, List<String> certs )
            throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException {

        try {
            getProxy(this.agentService.getMessageTarget(instance)).updateChain(keystore, keyAlias, certs);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.service.KeystoreManagementService#makeSelfSignedCert(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, java.lang.String, eu.agno3.orchestrator.crypto.keystore.CertRequestData)
     */
    @Override
    @RequirePermissions ( "keystores:manage:makeSelfSignedCert" )
    public void makeSelfSignedCert ( InstanceStructuralObject instance, String keystore, String keyAlias, CertRequestData req )
            throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.getMessageTarget(instance)).makeSelfSignedCert(keystore, keyAlias, req);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param req
     * @return the generated CSR
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    @Override
    @RequirePermissions ( "keystores:manage:generateCSR" )
    public String generateCSR ( InstanceStructuralObject instance, String keystore, String keyAlias, CertRequestData req )
            throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.getMessageTarget(instance)).generateCSR(keystore, keyAlias, req);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param requestPassword
     * @return the generated CSR
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    @Override
    @RequirePermissions ( "keystores:manage:generateRenewalCSR" )
    public String generateRenewalCSR ( InstanceStructuralObject instance, String keystore, String keyAlias, String requestPassword )
            throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.getMessageTarget(instance)).generateRenewalCSR(keystore, keyAlias, requestPassword);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    @Override
    @RequirePermissions ( "keystores:manage:deleteKey" )
    public void deleteKey ( InstanceStructuralObject instance, String keystore, String keyAlias ) throws KeystoreManagerException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        try {
            getProxy(this.agentService.getMessageTarget(instance)).deleteKey(keystore, keyAlias);
        }
        catch ( UndeclaredThrowableException e ) {
            throw handleJMXException(instance, e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.crypto.service.KeystoreManagementService#validateCertificate(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, java.util.List)
     */
    @Override
    public String validateCertificate ( InstanceStructuralObject instance, String keystore, List<String> chain ) throws KeystoreManagerException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        try {
            return getProxy(this.agentService.getMessageTarget(instance)).validateChain(keystore, false, chain);
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
    private KeystoreManagementMXBean getProxy ( MessageTarget target ) throws ModelServiceException {
        KeystoreManagementMXBean proxy;
        try {
            JMSJMXClient client = this.jmxClient.getClient(getJMXRequestPrototype(target));
            proxy = client.getProxy(KeystoreManagementMXBean.class);
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
     */
    private AgentCommunicationErrorException handleJMXException ( InstanceStructuralObject instance, UndeclaredThrowableException e )
            throws ModelObjectNotFoundException, AgentDetachedException, ModelServiceException {
        Exception e2 = e;
        if ( e.getCause() instanceof InvocationTargetException ) {
            e2 = (InvocationTargetException) e.getCause();
        }
        return new AgentCommunicationErrorException(
            "Agent communication failed", //$NON-NLS-1$
            this.agentService.handleCommFault(e2.getCause(), instance),
            e2.getCause());
    }


    private static KeystoreManagerJMXRequest getJMXRequestPrototype ( MessageTarget target ) {
        KeystoreManagerJMXRequest req = new KeystoreManagerJMXRequest();
        req.setTarget(target);
        return req;
    }
}
