/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.service;


import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.RemoteCallErrorException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.LocaleAdapter;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = FileshareGroupServerServiceDescriptor.NAMESPACE )
public interface FileshareGroupServerService extends SOAPWebService {

    /**
     * @param context
     * @param off
     * @param limit
     * @return group listing
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "groups" )
    @XmlElementWrapper
    List<Group> listGroups ( @WebParam ( name = "context" ) ServiceStructuralObject context, int off, int limit ) throws FileshareException,
            ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @return the group count
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "groupCount" )
    long getGroupCount ( @WebParam ( name = "context" ) ServiceStructuralObject context ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @return the group
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "group" )
    Group getGroup ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;


    /**
     * @param context
     * @param subjectName
     * @return the group info
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "groupInfo" )
    GroupQueryResult getGroupInfo ( @WebParam ( name = "context" ) ServiceStructuralObject context, String subjectName ) throws FileshareException,
            ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param group
     * @param createRoot
     * @return the created group
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "created" )
    Group createGroup ( @WebParam ( name = "context" ) ServiceStructuralObject context, Group group, boolean createRoot ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param groupId
     * @param quota
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void updateGroupQuota ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID groupId, Long quota ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @param groupLocale
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void setGroupLocale ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id,
            @XmlJavaTypeAdapter ( value = LocaleAdapter.class ) Locale groupLocale ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @param overrideAddress
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void setNotificationOverride ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id, String overrideAddress ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @param disableNotifications
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void setNotificationDisabled ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id, boolean disableNotifications ) throws FileshareException,
            ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param groupId
     * @param subjectIds
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void removeMembers ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID groupId, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) List<UUID> subjectIds )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;


    /**
     * @param context
     * @param userId
     * @param groupIds
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void removeFromGroups ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) Set<UUID> groupIds )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;


    /**
     * @param context
     * @param groupId
     * @param subjectIds
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void addMembers ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID groupId,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) List<UUID> subjectIds ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param userId
     * @param groupId
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void removeFromGroup ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID groupId )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;


    /**
     * @param context
     * @param groupId
     * @return the group members
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "members" )
    @XmlElementWrapper
    List<Subject> getMembers ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID groupId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param userId
     * @param groupId
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void addToGroup ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID groupId ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @param groupIds
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void addToGroups ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) Set<UUID> groupIds ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param ids
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void deleteGroups ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) List<UUID> ids ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param query
     * @param userId
     * @param limit
     * @return the query results
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "result" )
    @XmlElementWrapper
    List<GroupQueryResult> queryGroupsExcludingUserGroups ( @WebParam ( name = "context" ) ServiceStructuralObject context, String query,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID userId, int limit ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param query
     * @param limit
     * @return the query results
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "result" )
    @XmlElementWrapper
    List<GroupQueryResult> queryGroups ( @WebParam ( name = "context" ) ServiceStructuralObject context, String query, int limit )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;

}
