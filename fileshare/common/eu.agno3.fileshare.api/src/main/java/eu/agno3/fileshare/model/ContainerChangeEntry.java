/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 23, 2016 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Table ( name = "content_container_changes" )
public class ContainerChangeEntry {

    private ContainerEntity container;
    private UUID id;
    private long version;
    private String localName;
    private DateTime changeTime;
    private ChangeType changeType;
    private EntityType entityType;


    /**
     * @return the id
     */
    @Id
    @GeneratedValue ( generator = "system-uuid" )
    @GenericGenerator ( name = "system-uuid", strategy = "uuid2" )
    @Column ( length = 16 )
    public UUID getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( UUID id ) {
        this.id = id;
    }


    /**
     * @return the optimistic lock version
     */
    @Version
    public long getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( long version ) {
        this.version = version;
    }


    /**
     * @return the parent
     */
    @ManyToOne ( optional = false )
    @JoinColumn ( name = "container" )
    public ContainerEntity getContainer () {
        return this.container;
    }


    /**
     * @param container
     *            the container to set
     */
    public void setContainer ( ContainerEntity container ) {
        this.container = container;
    }


    /**
     * @return the localName
     */
    public String getLocalName () {
        return this.localName;
    }


    /**
     * @param localName
     *            the localName to set
     */
    public void setLocalName ( String localName ) {
        this.localName = localName;
    }


    /**
     * @return the changeTime
     */
    public DateTime getChangeTime () {
        return this.changeTime;
    }


    /**
     * @param changeTime
     *            the changeTime to set
     */
    public void setChangeTime ( DateTime changeTime ) {
        this.changeTime = changeTime;
    }


    /**
     * @return the changeType
     */
    @Enumerated ( EnumType.ORDINAL )
    public ChangeType getChangeType () {
        return this.changeType;
    }


    /**
     * @param changeType
     *            the changeType to set
     */
    public void setChangeType ( ChangeType changeType ) {
        this.changeType = changeType;
    }


    /**
     * @return the entityType
     */
    @Enumerated ( EnumType.ORDINAL )
    public EntityType getEntityType () {
        return this.entityType;
    }


    /**
     * @param entityType
     *            the entityType to set
     */
    public void setEntityType ( EntityType entityType ) {
        this.entityType = entityType;
    }
}
