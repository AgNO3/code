/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.CollectionResult;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SearchResult;
import eu.agno3.fileshare.model.query.SubjectQueryResult;


/**
 * @author mbechler
 *
 */
public interface BrowseService {

    /**
     * Get the user's own root container
     * 
     * Access control check:
     * - none as the current user's root is implicitly accessible to himself
     * 
     * @return the users root directory
     * @throws FileshareException
     * @throws AuthenticationException
     */
    ContainerEntity getUserRoot () throws FileshareException;


    /**
     * Enumerate child entities
     * 
     * Access control check:
     * - BROWSE access on the parent entity
     * OR
     * - only returns entries of which the user is the creator
     * 
     * @param entityId
     * 
     * @param e
     * @return the element's children
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getChildren ( EntityKey entityId ) throws FileshareException;


    /**
     * @param entityId
     * @param relativeSegments
     * @return the resolved entity or null if it does not exist
     * @throws FileshareException
     */
    VFSEntity getRelativePath ( EntityKey entityId, String[] relativeSegments ) throws FileshareException;


    /**
     * Get the current user's groups which have a root container
     * 
     * Access control check:
     * - only groups that the user is member of are returned, therefor implicitly accessible
     * 
     * @return the visible group roots for the current user
     * @throws FileshareException
     */
    CollectionResult<VFSContainerEntity> getVisibleGroupRoots () throws FileshareException;


    /**
     * Fetch a groups root container
     * 
     * If the root container for the given group does not exist, a new one will be created,
     * effectivly enabling file sharing in the group.
     * 
     * Access control check:
     * - the current user must be a member of the given group
     * 
     * @param groupId
     * @return the group root for the given group id
     * @throws FileshareException
     */
    VFSContainerEntity getOrCreateGroupRoot ( UUID groupId ) throws FileshareException;


    /**
     * Get all subjects that have shared entities with the current user
     * 
     * Access control check:
     * - only sharing subjects are returned
     * 
     * @return the subjects that shared something to this user
     * @throws FileshareException
     */
    Set<SubjectQueryResult> getSharingSubjects () throws FileshareException;


    /**
     * Get all share sources/targets for the current user
     * 
     * Access control check:
     * - only related information is returned
     * 
     * @return the users peers
     * @throws FileshareException
     */
    Set<PeerInfo> getPeers () throws FileshareException;


    /**
     * Fetch a subject that shared something with the current users
     * 
     * Access control check:
     * - subject is only returned if it shared something with the user
     * - OR have subjects:query (optimized: in that case grants will not be searched and a subject returned in any case)
     * 
     * @param subjId
     * @return the subject
     * @throws FileshareException
     */
    SubjectQueryResult getSharingSubject ( EntityKey subjId ) throws FileshareException;


    /**
     * Get all grants to the current user by the given subject
     * 
     * If multiple grants for the same resource exist only the one with the most permissions
     * will be returned.
     * 
     * Grants of entities owned by the user himself will be excluded (i.e. a user shares to one of his groups).
     * 
     * @param subjectId
     * @return the shares to the current user by the given subject
     * @throws FileshareException
     */
    CollectionResult<SubjectGrant> getSubjectShareGrants ( UUID subjectId ) throws FileshareException;


    /**
     * @param subjectId
     * @param id
     * @return the entity shared by the current user to the given subject
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getSubjectSharedToEntities ( UUID subjectId ) throws FileshareException;


    /**
     * Get all grants to the current user
     * 
     * If multiple grants for the same resource exist only the one with the most permissions
     * will be returned.
     * 
     * Grants of entities owned by the user himself will be excluded (i.e. a user shares to one of his groups).
     * 
     * @return the shares to the current user
     * @throws FileshareException
     */
    CollectionResult<SubjectGrant> getSharedToUserGrants () throws FileshareException;


    /**
     * Get all share root entities by the current user
     * 
     * @return the entities shared by the current user
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getSharedByUserGrants () throws FileshareException;


    /**
     * @param mailAddr
     * @return the entities shared by mail to the given address
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getMailSharedToEntities ( String mailAddr ) throws FileshareException;


    /**
     * @return the entiteis shared by link
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getTokenSharedToEntities () throws FileshareException;


    /**
     * @param subjectId
     * @param sqi
     * @return the last creation/expiration of a subject share to the current user
     */
    DateTime getSubjectSharesLastModified ( UUID subjectId );


    /**
     * @param subjectId
     * @return the last creation/expiration/content modification of a subject share to the current user
     */
    DateTime getSubjectSharesRecursiveLastModified ( UUID subjectId );


    /**
     * @return the last modification of a share for the current user
     */
    DateTime getSharesLastModified ();


    /**
     * @return the last modification of a share or it's contents for the current user
     */
    DateTime getSharesRecursiveLastModified ();


    /**
     * @param entityKey
     * @return the recursive last modification time of the entity
     */
    DateTime getRecursiveLastModified ( EntityKey entityKey );


    /**
     * 
     * Access control:
     * - user is grant target
     * 
     * @param subjectId
     * @param rootName
     * @return the grant
     * @throws AuthenticationException
     * @throws FileshareException
     */
    Grant getGrantWithName ( UUID subjectId, String rootName ) throws FileshareException;


    /**
     * 
     * Access control:
     * - user is member of group
     * 
     * @param groupName
     * @param realm
     * @return the group root
     * @throws FileshareException
     */
    VFSContainerEntity getGroupRootByName ( String groupName, String realm ) throws FileshareException;


    /**
     * 
     * Access control:
     * - only returns entities that the user has access to
     * 
     * @param query
     * @param limit
     * @param offset
     * @return search results
     * @throws FileshareException
     */
    SearchResult searchEntities ( String query, int limit, int offset ) throws FileshareException;


    /**
     * @param query
     * @return the user's peer mail addresses filtered by query
     * @throws FileshareException
     */
    Set<String> getPeerMailAddresses ( String query ) throws FileshareException;

}
