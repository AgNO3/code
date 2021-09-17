/**

 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.04.2016 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.List;
import java.util.Set;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.CollectionResult;
import eu.agno3.fileshare.service.BrowseService;


/**
 * @author mbechler
 *
 */
public interface BrowseServiceInternal extends BrowseService {

    /**
     * @param tx
     * @param roots
     * @param violations
     * @param clone
     * @return the number of groups hidden by policy
     * @throws FileshareException
     */
    int getVisibleGroupsInternal ( List<VFSContainerEntity> roots, Set<PolicyViolation> violations, boolean clone ) throws FileshareException;


    /**
     * @param tx
     * @param entityId
     * @param clone
     * @return the node children
     * @throws FileshareException
     * @throws AccessDeniedException
     * @throws GrantAuthenticationRequiredException
     * @throws PolicyNotFulfilledException
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws EntityNotFoundException
     */
    CollectionResult<VFSEntity> getChildrenInternal ( EntityKey entityId, boolean clone )
            throws FileshareException, AccessDeniedException, GrantAuthenticationRequiredException, PolicyNotFulfilledException,
            AuthenticationException, UserNotFoundException, EntityNotFoundException;

}
