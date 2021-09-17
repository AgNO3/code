/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 23, 2016 by mbechler
 */
package eu.agno3.fileshare.vfs;


import org.joda.time.DateTime;

import eu.agno3.fileshare.model.ChangeType;
import eu.agno3.fileshare.model.EntityType;
import eu.agno3.fileshare.model.VFSContainerEntity;


/**
 * @author mbechler
 *
 */
public class VFSContainerChange implements VFSChange {

    private VFSContainerEntity container;
    private DateTime changeTime;
    private ChangeType type;
    private String entityName;
    private EntityType entityType;


    /**
     * @param container
     * @param entityName
     * @param entityType
     * @param type
     * @param changeTime
     */
    public VFSContainerChange ( VFSContainerEntity container, String entityName, EntityType entityType, ChangeType type, DateTime changeTime ) {
        super();
        this.container = container;
        this.entityName = entityName;
        this.entityType = entityType;
        this.type = type;
        this.changeTime = changeTime;
    }


    /**
     * @return the container
     */
    public VFSContainerEntity getContainer () {
        return this.container;
    }


    /**
     * @return the changeTime
     */
    public DateTime getChangeTime () {
        return this.changeTime;
    }


    /**
     * @return the type
     */
    public ChangeType getType () {
        return this.type;
    }


    /**
     * @return the entityName
     */
    public String getEntityName () {
        return this.entityName;
    }


    /**
     * @return the entityType
     */
    public EntityType getEntityType () {
        return this.entityType;
    }

}
