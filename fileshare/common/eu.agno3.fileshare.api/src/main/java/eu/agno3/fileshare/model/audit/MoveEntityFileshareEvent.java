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
public class MoveEntityFileshareEvent extends EntityFileshareEvent implements Event, Serializable {

    /**
     * 
     */
    public static final String SOURCE_ENTITY_PARENT_IDS = "sourceEntityParentIds"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String SOURCE_ENTITY_IDS = "sourceEntityIds"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String SOURCE_ENTITY_NAMES = "sourceEntityNames"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String SOURCE_ENTITY_OWNERS = "sourceEntityOwners"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String SOURCE_ENTITY_RENAME = "sourceEntityRename"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_ID = "targetId"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_NAME = "targetName"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_OWNER = "targetOwner"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String MOVE_ACTION = "MOVE"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String MOVE_TYPE = "move-entity"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -217289424397611615L;

    private List<EntityKey> sourceEntityIds = new ArrayList<>();
    private List<EntityKey> sourceEntityParentIds = new ArrayList<>();
    private List<String> sourceEntityNames = new ArrayList<>();
    private List<UUID> sourceEntityOwners = new ArrayList<>();
    private List<String> sourceEntityRename = new ArrayList<>();

    private EntityKey targetId;
    private String targetName;
    private UUID targetOwner;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return MOVE_TYPE;
    }


    /**
     * @return the targetEntityIds
     */
    @JsonSerialize ( contentUsing = ToStringSerializer.class )
    public List<EntityKey> getSourceEntityIds () {
        return this.sourceEntityIds;
    }


    /**
     * @return the targetEntityNames
     */
    public List<String> getSourceEntityNames () {
        return this.sourceEntityNames;
    }


    /**
     * @return the sourceEntityOwners
     */
    public List<UUID> getSourceEntityOwners () {
        return this.sourceEntityOwners;
    }


    /**
     * @param sourceEntityOwners
     *            the sourceEntityOwners to set
     */
    public void setSourceEntityOwners ( List<UUID> sourceEntityOwners ) {
        this.sourceEntityOwners = sourceEntityOwners;
    }


    /**
     * @return the sourceEntityParentIds
     */
    @JsonSerialize ( contentUsing = ToStringSerializer.class )
    public List<EntityKey> getSourceEntityParentIds () {
        return this.sourceEntityParentIds;
    }


    /**
     * @param sourceEntityParentIds
     *            the sourceEntityParentIds to set
     */
    public void setSourceEntityParentIds ( List<EntityKey> sourceEntityParentIds ) {
        this.sourceEntityParentIds = sourceEntityParentIds;
    }


    /**
     * @return the targetParentId
     */
    @JsonSerialize ( using = ToStringSerializer.class )
    public EntityKey getTargetId () {
        return this.targetId;
    }


    /**
     * @param entityKey
     *            the targetParentId to set
     */
    public void setTargetId ( EntityKey entityKey ) {
        this.targetId = entityKey;
    }


    /**
     * @return the targetParentName
     */
    public String getTargetName () {
        return this.targetName;
    }


    /**
     * @param targetName
     *            the targetParentName to set
     */
    public void setTargetName ( String targetName ) {
        this.targetName = targetName;
    }


    /**
     * @return the targetOwner
     */
    public UUID getTargetOwner () {
        return this.targetOwner;
    }


    /**
     * @param targetOwner
     *            the targetOwner to set
     */
    public void setTargetOwner ( UUID targetOwner ) {
        this.targetOwner = targetOwner;
    }


    /**
     * @return the sourceEntityRename
     */
    public List<String> getSourceEntityRename () {
        return this.sourceEntityRename;
    }


    /**
     * @param sourceEntityRename
     *            the sourceEntityRename to set
     */
    public void setSourceEntityRename ( List<String> sourceEntityRename ) {
        this.sourceEntityRename = sourceEntityRename;
    }

}
