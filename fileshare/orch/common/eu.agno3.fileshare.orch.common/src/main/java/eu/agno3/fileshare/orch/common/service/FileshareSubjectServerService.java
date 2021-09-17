/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.service;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.RemoteCallErrorException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = FileshareSubjectServerServiceDescriptor.NAMESPACE )
public interface FileshareSubjectServerService extends SOAPWebService {

    /**
     * @param context
     * @param id
     * @return the subject
     * @throws FileshareException
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "subject" )
    Subject getSubject ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @return subjet info
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "queryResult" )
    SubjectQueryResult getSubjectInfo ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter (
        value = UUIDAdapter.class ) UUID id ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param principal
     * @return user info
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "queryResult" )
    SubjectQueryResult getUserInfo ( @WebParam ( name = "context" ) ServiceStructuralObject context, UserPrincipal principal )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;


    /**
     * @param context
     * @param query
     * @param i
     * @return query results
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "queryResult" )
    @XmlElementWrapper
    List<SubjectQueryResult> querySubjects ( @WebParam ( name = "context" ) ServiceStructuralObject context, String query, int i )
            throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException,
            RemoteCallErrorException;


    /**
     * @param context
     * @param query
     * @param groupId
     * @param i
     * @return query results
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "queryResult" )
    @XmlElementWrapper
    List<SubjectQueryResult> querySubjectsExcludingMembers ( @WebParam ( name = "context" ) ServiceStructuralObject context, String query,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID groupId, int i ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @param role
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void addRole ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id,
            String role ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @param roles
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void setRoles ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id,
            Set<String> roles ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @param role
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    void removeRole ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id,
            String role ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException, ModelServiceException,
            AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param id
     * @return the subjects effective roles
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "effectiveRoles" )
    @XmlElementWrapper
    Set<String> getEffectiveRoles ( @WebParam ( name = "context" ) ServiceStructuralObject context,
            @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID id ) throws FileshareException, ModelObjectNotFoundException,
            AgentDetachedException, ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


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
     */
    void setSubjectRootSecurityLabel ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter (
        value = UUIDAdapter.class ) UUID id, String label ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


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
     */
    void setSubjectRootSecurityLabelRecursive ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter (
        value = UUIDAdapter.class ) UUID id, String label ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;


    /**
     * @param context
     * @param subjectId
     * @return the user's subject root label
     * @throws FileshareException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws RemoteCallErrorException
     */
    @WebResult ( name = "subjectRootLabel" )
    SecurityLabel getSubjectRootLabel ( @WebParam ( name = "context" ) ServiceStructuralObject context, @XmlJavaTypeAdapter (
        value = UUIDAdapter.class ) UUID subjectId ) throws FileshareException, ModelObjectNotFoundException, AgentDetachedException,
            ModelServiceException, AgentCommunicationErrorException, RemoteCallErrorException;

}
