/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 23, 2016 by mbechler
 */
package eu.agno3.fileshare.vfs;


import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public class VFSEntityChange implements VFSChange {

    private VFSEntity entity;


    /**
     * @param entity
     * 
     */
    public VFSEntityChange ( VFSEntity entity ) {
        this.entity = entity;
    }


    /**
     * @return the entity
     */
    public VFSEntity getEntity () {
        return this.entity;
    }

}
