/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.audit;


import java.io.Serializable;
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
public class SingleEntityFileshareEvent extends EntityFileshareEvent implements Event, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -217289424397611615L;

    /**
     * 
     */
    public static final String TARGET_ENTITY_ID = "targetEntityId"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TARGET_ENTITY_NAME = "targetEntityName"; //$NON-NLS-1$

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
    public static final String TARGET_SECURITY_LABEL = "targetSecurityLabel"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String SINGLE_ENTITY_TYPE = "single-entity"; //$NON-NLS-1$

    private EntityKey targetEntityId;
    private String targetEntityName;
    private UUID targetEntityOwnerId;

    private EntityKey targetParentId;
    private String targetParentName;

    private long targetSize;

    private String targetContentType;
    private String targetEntityType;

    private String targetSecurityLabel;

    private Long targetLastModified;

    private Long targetCreated;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return SINGLE_ENTITY_TYPE;
    }


    /**
     * @return the targetEntityId
     */
    @JsonSerialize ( using = ToStringSerializer.class )
    public EntityKey getTargetEntityId () {
        return this.targetEntityId;
    }


    /**
     * @param targetEntityId
     *            the targetEntityId to set
     */
    public void setTargetEntityId ( EntityKey targetEntityId ) {
        this.targetEntityId = targetEntityId;
    }


    /**
     * @return the targetEntityName
     */
    public String getTargetEntityName () {
        return this.targetEntityName;
    }


    /**
     * @param targetEntityName
     *            the targetEntityName to set
     */
    public void setTargetEntityName ( String targetEntityName ) {
        this.targetEntityName = targetEntityName;
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


    /**
     * @param fileSize
     */
    public void setTargetSize ( long fileSize ) {
        this.targetSize = fileSize;
    }


    /**
     * @return the targetSize
     */
    public long getTargetSize () {
        return this.targetSize;
    }


    /**
     * @param contentType
     */
    public void setTargetContentType ( String contentType ) {
        this.targetContentType = contentType;
    }


    /**
     * @return the targetType
     */
    public String getTargetContentType () {
        return this.targetContentType;
    }


    /**
     * @param id
     */
    public void setTargetEntityOwnerId ( UUID id ) {
        this.targetEntityOwnerId = id;
    }


    /**
     * @return the targetEntityOwnerId
     */
    public UUID getTargetEntityOwnerId () {
        return this.targetEntityOwnerId;
    }


    /**
     * @param targetEntityType
     */
    public void setTargetEntityType ( String targetEntityType ) {
        this.targetEntityType = targetEntityType;
    }


    /**
     * @return the targetEntityType
     */
    public String getTargetEntityType () {
        return this.targetEntityType;
    }


    /**
     * @param label
     */
    public void setTargetSecurityLabel ( String label ) {
        this.targetSecurityLabel = label;
    }


    /**
     * @return the targetSecurityLabel
     */
    public String getTargetSecurityLabel () {
        return this.targetSecurityLabel;
    }


    /**
     * @param targetLastModified
     */
    public void setTargetLastModified ( Long targetLastModified ) {
        this.targetLastModified = targetLastModified;
    }


    /**
     * @return the targetLastModified
     */
    public Long getTargetLastModified () {
        return this.targetLastModified;
    }


    /**
     * @param targetCreated
     */
    public void setTargetCreated ( Long targetCreated ) {
        this.targetCreated = targetCreated;
    }


    /**
     * @return the targetCreated
     */
    public Long getTargetCreated () {
        return this.targetCreated;
    }
}
