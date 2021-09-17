/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.service;


import java.util.List;
import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserExistsException;
import eu.agno3.fileshare.exceptions.UserLimitExceededException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserCreateData;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.RemoteCallErrorException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;
import eu.agno3.runtime.xml.binding.adapter.XmlDateTimeAdapter;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = FileshareUserServerServiceDescriptor.NAMESPACE )
public interface FileshareUserServerService extends SOAPWebService {

    /**
     * @param context
     * @param userId
     * @return the user
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "user" )
    User getUser ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId)
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void enableLocalUser ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void disableLocalUser ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @return user authentication info
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "userInfo" )
    UserInfo getLocalUserInfo ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @return direect group memberships
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "userGroups" )
    @XmlElementWrapper
    List<Group> getUserGroups ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @return transitive group memberships
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "groupClosure" )
    @XmlElementWrapper
    List<Group> getUserGroupClosure ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param off
     * @param limit
     * @return user listing
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "users" )
    @XmlElementWrapper
    List<User> listUsers ( @WebParam ( name = "context" ) ServiceStructuralObject context, int off, int limit)
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @return the user count
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "userCount" )
    long getUserCount ( @WebParam ( name = "context" ) ServiceStructuralObject context) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * 
     * @param context
     * @param userData
     * @return the created user
     * @throws FileshareException
     * @throws UserLimitExceededException
     * @throws UserExistsException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "created" )
    User createLocalUser ( @WebParam ( name = "context" ) ServiceStructuralObject context, UserCreateData userData)
            throws FileshareException, UserLimitExceededException, UserExistsException, ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userIds
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void deleteUsers ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) List<UUID> userIds) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @param newPassword
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void changePassword ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId, String newPassword)
                    throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
                    AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @return the user details
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "details" )
    UserDetails getUserDetails ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param id
     * @param label
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void updateUserLabel ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id,
            String label) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
                    AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @param data
     * @return the updated user details
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "updated" )
    UserDetails updateUserDetails ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId, UserDetails data) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param userId
     * @param quota
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void updateUserQuota ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId, Long quota) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param id
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void enableUserRoot ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id)
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param id
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void disableUserRoot ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id)
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException, AgentOfflineException;


    /**
     * @param context
     * @param id
     * @param expiration
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     * @throws AgentOfflineException
     */
    void setUserExpiry ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id,
            @XmlJavaTypeAdapter ( value = XmlDateTimeAdapter.class ) DateTime expiration) throws FileshareException, ModelObjectNotFoundException,
                    AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException, AgentOfflineException;

}
