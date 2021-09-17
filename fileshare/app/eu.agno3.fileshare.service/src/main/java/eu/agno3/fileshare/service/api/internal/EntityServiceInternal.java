/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.List;
import java.util.Set;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.EntityService;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public interface EntityServiceInternal extends EntityService {

    /**
     * @param tx
     * @param v
     * @param currentUser
     * @param remove
     * @param expired
     * @throws FileshareException
     */
    void doDelete ( EntityTransactionContext tx, VFSContext v, User currentUser, Set<? extends VFSEntity> remove, boolean expired )
            throws FileshareException;


    /**
     * @param v
     * @param e
     * @param checkAccess
     *            whether to only return path segments the user has access to
     * @return the full path to the entity
     * @throws GrantAuthenticationRequiredException
     * @throws FileshareException
     */
    List<String> getFullPath ( VFSContext v, VFSEntity e, boolean checkAccess ) throws FileshareException;

}
