/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.DisallowedMimeTypeException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InvalidSecurityLabelException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public interface EntityService {

    /**
     * 
     * @param id
     * @return the parsed entity key
     */
    EntityKey parseEntityKey ( String id );


    /**
     * Get an entity by id
     * 
     * Access control:
     * - user must have READ access to the entity
     * 
     * @param id
     * @return the entity
     * @throws FileshareException
     */
    VFSEntity getEntity ( EntityKey id ) throws FileshareException;


    /**
     * @param entityKey
     * @return the entities parent
     * @throws FileshareException
     */
    VFSContainerEntity getParent ( EntityKey entityKey ) throws FileshareException;


    /**
     * Access control:
     * - user must have READ access to the entities
     * 
     * @param ids
     * @return the entities specified by ids
     * @throws FileshareException
     * @throws AccessDeniedException
     */
    List<VFSEntity> getEntities ( List<EntityKey> ids ) throws FileshareException;


    /**
     * Move an entity
     * 
     * Access control:
     * - user must have EDIT access to the src
     * AND
     * - user must have UPLOAD access to the target
     * 
     * The ownership of the src will be changed to the target's owner
     * 
     * To prevent cyclic structures, entities may be not be moved to subcontainers of themselves.
     * 
     * Moving will fail if the target already contains a file with the same name.
     * 
     * @param src
     * @param target
     *            target container id
     * @return the moved entity
     * @throws FileshareException
     */
    VFSEntity move ( EntityKey src, EntityKey target ) throws FileshareException;


    /**
     * Move multiple entities
     * 
     * Access control:
     * - user must have EDIT access to all the sources
     * AND
     * - user must have UPLOAD access to the target
     * 
     * The ownership of the sources will be changed to the target's owner
     * 
     * To prevent cyclic structures, entities may be not be moved to subcontainers of themselves.
     * 
     * Moving will fail if the target already contains a file with one of the source names.
     * 
     * @param sources
     * @param newNames
     * @param target
     *            target container id
     * @return the moved entities
     * @throws FileshareException
     */
    List<VFSEntity> move ( List<EntityKey> sources, Map<EntityKey, String> newNames, EntityKey target ) throws FileshareException;


    /**
     * Move and rename an entity
     * 
     * @param entityId
     * @param targetId
     * @param newName
     * @return the moved entity
     * @throws FileshareException
     * @see this.move()
     */
    VFSEntity moveAndRename ( EntityKey entityId, EntityKey targetId, String newName ) throws FileshareException;


    /**
     * Delete an entity
     * 
     * Access control:
     * - user must have EDIT access to the entity's parent
     * - OR user has UPLOAD access and was the creator of the target file or the target directory and all of it's
     * contents
     * 
     * @param entityId
     * @throws FileshareException
     */
    void delete ( EntityKey entityId ) throws FileshareException;


    /**
     * Delete multiple entities
     * 
     * Access control:
     * - user must be the owner of all entities
     * - OR user has UPLOAD access and was the creator of the target files or the target directories and all of their
     * contents
     * 
     * @param entities
     * @throws FileshareException
     */
    void delete ( Collection<EntityKey> entities ) throws FileshareException;


    /**
     * Get the full path of an entity
     * 
     * Access control:
     * - the user must have any access to the entity
     * - path elements for parents are only returned for parents to which the user has any access
     * ( therefor the path for shared files will be relative to the topmost shared container)
     * 
     * @param id
     * @return the full (visible) path to the entity
     * @throws FileshareException
     */
    List<String> getFullPath ( EntityKey id ) throws FileshareException;


    /**
     * 
     * Access control:
     * - the user must have any access to the entity
     * - parents are only returned for parents to which the user has any access
     * ( therefor the path for shared files will be relative to the topmost shared container)
     * 
     * @param id
     * @return the parent containers of an entity
     * @throws FileshareException
     */
    List<VFSContainerEntity> getParents ( EntityKey id ) throws FileshareException;


    /**
     * 
     * Access control:
     * - the user must have EDIT access
     * - OR user has UPLOAD access and was the creator of the target file/directory
     * 
     * ABD
     * - the user must have the entity:changeMimeType permission
     * 
     * 
     * @param entityId
     * @param mimeType
     * @throws FileshareException
     * @throws DisallowedMimeTypeException
     * @throws AccessDeniedException
     */
    void setMimeType ( EntityKey entityId, String mimeType ) throws FileshareException;


    /**
     * 
     * Access control:
     * (
     * - the user must have EDIT access
     * - OR user has UPLOAD access and was the creator of the target file/directory
     * AND
     * - the user must have the entity:changeSecurityLabel permission
     * )
     * OR
     * - have have manage:subjects:subjectRootSecurityLabel
     * 
     * The label can be only set to a value at least as high as the parent container's
     * 
     * @param entityId
     * @param label
     * @param raise
     *            whether to raise lower levels below to this level
     * @throws FileshareException
     * @throws AccessDeniedException
     * @throws InvalidSecurityLabelException
     */
    void setSecurityLabel ( EntityKey entityId, String label, boolean raise ) throws FileshareException;


    /**
     * 
     * Access control:
     * - user must be owner of the entity
     * OR
     * - have have manage:subjects:subjectRootSecurityLabel
     * 
     * The label can be only set to a value at least as high as the parent container's
     * 
     * @param id
     * @param label
     * @param force
     * @param allowLower
     * @throws FileshareException
     * @throws AccessDeniedException
     * @throws InvalidSecurityLabelException
     */
    void setSecurityLabelRecursive ( EntityKey id, String label, boolean force, Set<EntityKey> allowLower ) throws FileshareException;


    /**
     * Collects child entities that have a differing security label
     * 
     * Access control:
     * - user must be owner of the entity
     * OR
     * - have have manage:subjects:subjectRootSecurityLabel
     * 
     * @param root
     * @return entities with labels that differ from their parents
     * @throws FileshareException
     */
    List<VFSEntity> getChildrenSecurityLabels ( EntityKey root ) throws FileshareException;


    /**
     * 
     * Access control:
     * - the user must have EDIT access
     * - OR user has UPLOAD access and was the creator of the target files or the target directories and all of their
     * contents
     * AND
     * - the user must have the entity:changeExpirationDate permission
     * 
     * @param id
     * @param expires
     * @throws FileshareException
     */
    void setExpirationDate ( EntityKey id, DateTime expires ) throws FileshareException;


    /**
     * 
     * Access control:
     * - the user must have BROWSE access to the container
     * 
     * @param containerId
     * @param filename
     * @return the conflicting file
     * @throws FileshareException
     */
    FileEntity checkNameConflict ( EntityKey containerId, String filename ) throws FileshareException;


    /**
     * 
     * Access control:
     * - the user must have EDIT access
     * - OR the user has UPLOAD access and was the creator of the file/directory
     * 
     * @param entityId
     * @param newName
     * @return the renamed entity
     * @throws FileshareException
     */
    VFSEntity rename ( EntityKey entityId, String newName ) throws FileshareException;


    /**
     * 
     * @param label
     * @param req
     * @return whether the policy for the given label is fulfilled
     */
    PolicyViolation getPolicyViolation ( String label, ServletRequest req );


    /**
     * @param target
     * @throws FileshareException
     */
    void checkWriteAccess ( EntityKey target ) throws FileshareException;


    /**
     * 
     * Access control:
     * - the user must be owner
     * - OR the user must be the grant target
     * 
     * @param entityId
     * @return a grant for the given entity
     * @throws FileshareException
     */
    Grant getGrant ( EntityKey entityId ) throws FileshareException;

}
