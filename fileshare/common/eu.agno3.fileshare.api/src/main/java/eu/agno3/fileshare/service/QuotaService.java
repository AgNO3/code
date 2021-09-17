/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public interface QuotaService {

    /**
     * Checks whether the given size can possibly be added
     * 
     * 
     * @param parent
     * @param neededSize
     * @param temporarySize
     * @throws QuotaExceededException
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    public void checkFreeSpace ( VFSEntity parent, long neededSize, long temporarySize ) throws FileshareException;


    /**
     * @param v
     * @param e
     * @return the current used size
     * @throws FileshareException
     */
    long getUsedSize ( VFSContainerEntity e ) throws FileshareException;
}
