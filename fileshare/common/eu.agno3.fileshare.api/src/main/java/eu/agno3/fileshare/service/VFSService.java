/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import eu.agno3.fileshare.model.EntityKey;


/**
 * @author mbechler
 *
 */
public interface VFSService {

    /**
     * 
     * @param id
     * @return an entity key
     */
    public EntityKey parseEntityKey ( String id );
}
