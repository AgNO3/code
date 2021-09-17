/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.crypto.service;


import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.crypto.keystore.CertRequestData;
import eu.agno3.orchestrator.crypto.keystore.KeyInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyStoreInfo;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = KeystoreManagementServiceDescriptor.NAMESPACE )
public interface KeystoreManagementService extends SOAPWebService {

    /**
     * @param instance
     * @param includeInternal
     * @return a list of keystores known to the system
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws KeystoreManagerException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "getKeystores" )
    @WebResult ( name = "keystores" )
    @XmlElementWrapper ( name = "keystores", required = true )
    @XmlElement ( name = "keystore", required = false )
    List<KeyStoreInfo> getKeystores ( @WebParam ( name = "instance" ) InstanceStructuralObject instance,
            @WebParam ( name = "includeInternal" ) boolean includeInternal) throws ModelObjectNotFoundException, ModelServiceException,
                    KeystoreManagerException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * @param instance
     * @return the known keystore aliases
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws KeystoreManagerException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "getKeystoreAliases" )
    @WebResult ( name = "keystoreAliases" )
    @XmlElementWrapper ( name = "keystores", required = true )
    @XmlElement ( name = "keystore", required = false )
    List<String> getKeystoreAliases ( @WebParam ( name = "instance" ) InstanceStructuralObject instance) throws ModelObjectNotFoundException,
            ModelServiceException, KeystoreManagerException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * @param instance
     * @param keystore
     * @return the keystore info
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws KeystoreManagerException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     */
    KeyStoreInfo getKeystore ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore)
            throws ModelObjectNotFoundException, ModelServiceException, KeystoreManagerException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException;


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @return the key info for the key
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws KeystoreManagerException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "getKey" )
    @WebResult ( name = "keyInfo" )
    KeyInfo getKeyInfo ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias) throws ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
                    KeystoreManagerException, AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param keystore
     * @param keyAlias
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "deleteKey" )
    void deleteKey ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias) throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException,
                    ModelServiceException, AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param requestPassword
     * @return the generated CSR
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "generateRenewalCSR" )
    @WebResult ( name = "csr" )
    String generateRenewalCSR ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias, @WebParam ( name = "requestPassword" ) String requestPassword)
                    throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
                    AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param req
     * @return the generated CSR
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "generateCSR" )
    @WebResult ( name = "csr" )
    String generateCSR ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias, @WebParam ( name = "request" ) CertRequestData req) throws KeystoreManagerException,
                    ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param certs
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "updateChain" )
    void updateChain ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias, @WebParam ( name = "chain" ) List<String> certs) throws KeystoreManagerException,
                    ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param keyType
     * @param keyData
     * @param importChain
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "importKey" )
    void importKey ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias, @WebParam ( name = "keyType" ) String keyType, @WebParam ( name = "keyData" ) String keyData,
            @WebParam ( name = "chain" ) List<String> importChain) throws KeystoreManagerException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param keyType
     * @return job info for key generation job
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "generateKey" )
    @WebResult ( name = "keyGenerationJob" )
    JobInfo generateKey ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias, @WebParam ( name = "keyType" ) String keyType) throws KeystoreManagerException,
                    ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException;


    /**
     * @param instance
     * @param keystore
     * @param keyAlias
     * @param req
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "makeSelfSignedCert" )
    void makeSelfSignedCert ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "key" ) String keyAlias, @WebParam ( name = "req" ) CertRequestData req) throws KeystoreManagerException,
                    ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException;


    /**
     * @param instance
     * @param keystore
     * @param chain
     * @return whether the certificate is valid
     * @throws KeystoreManagerException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "validateCert" )
    @WebResult ( name = "validationError" )
    String validateCertificate ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "keystore" ) String keystore,
            @WebParam ( name = "chain" ) List<String> chain) throws KeystoreManagerException, ModelObjectNotFoundException, AgentDetachedException,
                    ModelServiceException, AgentCommunicationErrorException;

}
