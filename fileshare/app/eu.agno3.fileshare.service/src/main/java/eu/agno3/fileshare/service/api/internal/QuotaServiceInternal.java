/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import javax.persistence.EntityManager;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.QuotaService;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public interface QuotaServiceInternal extends QuotaService {

    /**
     * 
     * @param v
     * @param parent
     * @param sizeDiff
     * @param temporarySize
     * @return a reservation context
     * @throws QuotaExceededException
     * @throws InsufficentStorageSpaceException
     */
    public QuotaReservation checkAndReserve ( VFSContext v, VFSContainerEntity parent, long sizeDiff, long temporarySize )
            throws QuotaExceededException, InsufficentStorageSpaceException;


    /**
     * 
     * @param v
     * @param parent
     * @param sizeDiff
     * @throws QuotaExceededException
     * @throws FileshareException
     */
    public void commit ( VFSContext v, VFSContainerEntity parent, long sizeDiff ) throws QuotaExceededException, FileshareException;


    /**
     * 
     * @param rootId
     * @param sizeDiff
     */
    public void undoReservation ( EntityKey rootId, long sizeDiff );


    /**
     * @param v
     * @param entity
     * @return the total size of the entity and it's children
     * @throws FileshareException
     */
    public long getCombinedSize ( VFSContext v, VFSEntity entity ) throws FileshareException;


    /**
     * @param em
     * @param realSubject
     */
    void updateDirectorySizes ( EntityManager em, Subject realSubject );

}
