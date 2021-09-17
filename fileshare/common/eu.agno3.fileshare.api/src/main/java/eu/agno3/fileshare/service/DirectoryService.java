/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;


/**
 * @author mbechler
 *
 */
public interface DirectoryService {

    /**
     * Create a container inside another
     * 
     * Access control:
     * - user must have UPLOAD access to the parent container
     * 
     * @param parentId
     * @param e
     * @return the created entity
     * @throws FileshareException
     */
    VFSContainerEntity create ( EntityKey parentId, VFSContainerEntity e ) throws FileshareException;


    /**
     * Get a container
     * 
     * Access control:
     * - user must have BROWSE access to the container given by id
     * 
     * @param containerId
     * @return the container with the given id
     * @throws FileshareException
     */
    VFSContainerEntity getById ( EntityKey containerId ) throws FileshareException;


    /**
     * 
     * Access control:
     * - user must be owner
     * 
     * @param id
     * @param sendNotifications
     * @throws FileshareException
     */
    void setSendNotifications ( EntityKey id, boolean sendNotifications ) throws FileshareException;


    /**
     * 
     * Access control:
     * - user must be owner
     * 
     * @param id
     * @param allowFileOverwrite
     * @throws FileshareException
     */
    void setAllowFileOverwrite ( EntityKey id, boolean allowFileOverwrite ) throws FileshareException;

}
