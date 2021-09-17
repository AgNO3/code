/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.realms.service;


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
import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.orchestrator.realms.KeyInfo;
import eu.agno3.orchestrator.realms.RealmInfo;
import eu.agno3.orchestrator.realms.RealmManagementException;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.runtime.security.credentials.WrappedCredentials;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = RealmManagementServiceDescriptor.NAMESPACE )
public interface RealmManagementService extends SOAPWebService {

    /**
     * @param instance
     * @return a list of realm names known to the system
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws RealmManagementException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "getRealms" )
    @WebResult ( name = "realms" )
    @XmlElementWrapper ( name = "realms", required = true )
    @XmlElement ( name = "realm", required = false )
    List<RealmInfo> getRealms ( @WebParam ( name = "instance" ) InstanceStructuralObject instance) throws ModelObjectNotFoundException,
            ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * @param selectedInstance
     * @param realm
     * @return the named realm
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "getRealm" )
    @WebResult ( name = "realm" )
    RealmInfo getRealm ( @WebParam ( name = "instance" ) InstanceStructuralObject selectedInstance, @WebParam ( name = "realm" ) String realm)
            throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException;


    /**
     * @param instance
     * @param keytab
     * @param realm
     * @param type
     * @param ki
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "removeKeys" )
    void removeKeys ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "realm" ) String realm,
            @WebParam ( name = "realmType" ) RealmType type, @WebParam ( name = "keytab" ) String keytab,
            @WebParam ( name = "keys" ) List<KeyInfo> ki) throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException,
                    AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * 
     * @param instance
     * @param realm
     * @param type
     * @param keytab
     * @param ki
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "addKeys" )
    void addKeys ( @WebParam ( name = "instance" ) InstanceStructuralObject instance, @WebParam ( name = "realm" ) String realm,
            @WebParam ( name = "realmType" ) RealmType type, @WebParam ( name = "keytab" ) String keytab,
            @WebParam ( name = "keys" ) List<KeyData> ki) throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException,
                    AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * 
     * @param selectedInstance
     * @param realm
     * @param type
     * @param keytab
     * @param initialKeys
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "createKeytab" )
    void createKeytab ( @WebParam ( name = "instance" ) InstanceStructuralObject selectedInstance, @WebParam ( name = "realm" ) String realm,
            @WebParam ( name = "realmType" ) RealmType type, @WebParam ( name = "keytab" ) String keytab, List<KeyData> initialKeys)
                    throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException,
                    AgentOfflineException, AgentCommunicationErrorException;


    /**
     * 
     * @param selectedInstance
     * @param realm
     * @param type
     * @param keytab
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "removeKeytab" )
    void removeKeytab ( @WebParam ( name = "instance" ) InstanceStructuralObject selectedInstance, @WebParam ( name = "realm" ) String realm,
            @WebParam ( name = "realmType" ) RealmType type, @WebParam ( name = "keytab" ) String keytab) throws ModelObjectNotFoundException,
                    ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * @param selectedInstance
     * @param realm
     * @param creds
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "joinADRealm" )
    void joinAD ( @WebParam ( name = "instance" ) InstanceStructuralObject selectedInstance, @WebParam ( name = "realm" ) String realm,
            @WebParam ( name = "creds" ) WrappedCredentials creds) throws ModelObjectNotFoundException, ModelServiceException,
                    RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * @param selectedInstance
     * @param realm
     * @param machinePassword
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "joinADRealmWithResetPassword" )
    void joinADWithMachinePassword ( @WebParam ( name = "instance" ) InstanceStructuralObject selectedInstance,
            @WebParam ( name = "realm" ) String realm, @WebParam ( name = "initialMachinePassword" ) String machinePassword)
                    throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException,
                    AgentOfflineException, AgentCommunicationErrorException;


    /**
     * @param selectedInstance
     * @param realm
     * @param creds
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "leaveADRealm" )
    void leaveAD ( @WebParam ( name = "instance" ) InstanceStructuralObject selectedInstance, @WebParam ( name = "realm" ) String realm,
            @WebParam ( name = "creds" ) WrappedCredentials creds) throws ModelObjectNotFoundException, ModelServiceException,
                    RealmManagementException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException;


    /**
     * 
     * @param selectedInstance
     * @param realm
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws RealmManagementException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    @WebMethod ( action = "rekeyADRealm" )
    void rekeyAD ( @WebParam ( name = "instance" ) InstanceStructuralObject selectedInstance, @WebParam ( name = "realm" ) String realm)
            throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException;

}
