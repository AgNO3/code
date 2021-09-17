/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.List;
import java.util.Set;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.ShareService;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public interface ShareServiceInternal extends ShareService {

    /**
     * @param tx
     * @param grants
     * @param expire
     */
    void doRevokeGrants ( EntityTransactionContext tx, List<Grant> grants, boolean expire );


    /**
     * @param v
     * @param entity
     * @param type
     * @return the effective grants applied to the entity
     * @throws FileshareException
     */
    Set<Grant> getEffectiveGrantsInternal ( VFSContext v, VFSEntity entity, GrantType type ) throws FileshareException;

}
