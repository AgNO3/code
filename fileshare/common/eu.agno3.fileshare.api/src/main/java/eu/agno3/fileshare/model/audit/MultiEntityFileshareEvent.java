/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.audit;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.runtime.eventlog.Event;


/**
 * @author mbechler
 *
 */
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class MultiEntityFileshareEvent extends EntityFileshareEvent implements Event, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -217289424397611615L;

    /**
     * 
     */
    public static final String TARGET_ENTITY_IDS = "targetEntityIds"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_ENTITY_NAMES = "targetEntityNames"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_ENTITY_OWNERS = "targetEntityOwner"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_PARENT_ID = "targetParentId"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_PARENT_NAME = "targetParentName"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String MULTI_ENTITY_TYPE = "multi-entity"; //$NON-NLS-1$

    private List<EntityKey> targetEntityIds = new ArrayList<>();
    private List<String> targetEntityNames = new ArrayList<>();
    private List<UUID> targetEntityOwners = new ArrayList<>();

    private EntityKey targetParentId;
    private String targetParentName;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return MULTI_ENTITY_TYPE;
    }


    /**
     * @return the targetEntityIds
     */
    @JsonSerialize ( contentUsing = ToStringSerializer.class )
    public List<EntityKey> getTargetEntityIds () {
        return this.targetEntityIds;
    }


    /**
     * @return the targetEntityNames
     */
    public List<String> getTargetEntityNames () {
        return this.targetEntityNames;
    }


    /**
     * @return the targetEntityOwners
     */
    public List<UUID> getTargetEntityOwners () {
        return this.targetEntityOwners;
    }


    /**
     * @param targetEntityOwners
     *            the targetEntityOwners to set
     */
    public void setTargetEntityOwners ( List<UUID> targetEntityOwners ) {
        this.targetEntityOwners = targetEntityOwners;
    }


    /**
     * @return the targetParentId
     */
    @JsonSerialize ( using = ToStringSerializer.class )
    public EntityKey getTargetParentId () {
        return this.targetParentId;
    }


    /**
     * @param targetParentId
     *            the targetParentId to set
     */
    public void setTargetParentId ( EntityKey targetParentId ) {
        this.targetParentId = targetParentId;
    }


    /**
     * @return the targetParentName
     */
    public String getTargetParentName () {
        return this.targetParentName;
    }


    /**
     * @param targetParentName
     *            the targetParentName to set
     */
    public void setTargetParentName ( String targetParentName ) {
        this.targetParentName = targetParentName;
    }

}
