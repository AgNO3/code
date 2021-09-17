/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "grants", indexes = {
    @Index ( columnList = "expires" )
})
@SafeSerialization
public abstract class Grant implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2293658278374082282L;
    private UUID id;
    private long version;

    private ContentEntity entity;

    private int perms;

    private User creator;
    private DateTime created;
    private DateTime expires;
    private DateTime lastModified;
    private Set<ContentEntity> lastModifierOf;
    private Set<ContentEntity> creatorOf;
    private int effectivePerms = -1;
    private int inheritedPerms;
    private int groupPerms;
    private UUID inheritedId;
    private String displayName;
    private boolean collection;


    /**
     * 
     */
    public Grant () {}


    protected Grant ( Grant g, boolean refs, boolean basic ) {
        this.id = g.id;
        this.version = g.version;
        if ( refs && !basic && g.entity != null ) {
            this.entity = (ContentEntity) g.entity.cloneShallow(false);
        }
        this.perms = g.perms;
        if ( refs && g.creator != null ) {
            this.creator = g.creator.cloneShallow(false);
        }
        this.created = g.created;
        this.expires = g.expires;
        this.lastModified = g.lastModified;
        this.displayName = g.getDisplayName();
        this.collection = g.isCollection();
    }


    /**
     * @param refs
     * @return cloned grant
     * 
     */
    public Grant cloneShallow ( boolean refs ) {
        return cloneShallow(refs, false);
    }


    /**
     * 
     * @param refs
     * @param basic
     * @return cloned grant
     */
    public abstract Grant cloneShallow ( boolean refs, boolean basic );


    /**
     * 
     * @return cloned grant
     */
    public Grant cloneShallow () {
        return cloneShallow(true);
    }


    /**
     * @return the effectivePerms
     */
    @Transient
    public int getEffectivePerms () {
        if ( this.effectivePerms < 0 ) {
            return this.perms;
        }
        return this.effectivePerms;
    }


    /**
     * @param effectivePerms
     */
    public void setEffectivePerms ( int effectivePerms ) {
        this.effectivePerms = effectivePerms;
    }


    /**
     * @return the inheritedPerms
     */
    @Transient
    public int getInheritedPerms () {
        return this.inheritedPerms;
    }


    /**
     * @param inheritedPerms
     */
    public void setInheritedPerms ( int inheritedPerms ) {
        this.inheritedPerms = inheritedPerms;
    }


    /**
     * @return the inheritedId
     */
    @Transient
    public UUID getInheritedId () {
        return this.inheritedId;
    }


    /**
     * @param inheritedId
     */
    public void setInheritedFrom ( UUID inheritedId ) {
        this.inheritedId = inheritedId;
    }


    /**
     * 
     * @return the group inherited permissions
     */
    @Transient
    public int getGroupPerms () {
        return this.groupPerms;
    }


    /**
     * @param groupPerms
     *            the groupPerms to set
     */
    public void setGroupPerms ( int groupPerms ) {
        this.groupPerms = groupPerms;
    }


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
     * @return the entity
     */
    @ManyToOne ( )
    public ContentEntity getEntity () {
        return this.entity;
    }


    /**
     * @param entity
     *            the entity to set
     */
    public void setEntity ( ContentEntity entity ) {
        this.entity = entity;
    }


    /**
     * @return the entities this grant is last modifier of
     */
    @OneToMany ( mappedBy = "lastModifiedGrant", fetch = FetchType.LAZY )
    public Set<ContentEntity> getLastModifierOf () {
        return this.lastModifierOf;
    }


    /**
     * @param lastModifierOf
     *            the lastModifierOf to set
     */
    public void setLastModifierOf ( Set<ContentEntity> lastModifierOf ) {
        this.lastModifierOf = lastModifierOf;
    }


    /**
     * 
     * @return the entities this grant is creator of
     */
    @OneToMany ( mappedBy = "creatorGrant", fetch = FetchType.LAZY )
    public Set<ContentEntity> getCreatorOf () {
        return this.creatorOf;
    }


    /**
     * @param creatorOf
     *            the creatorOf to set
     */
    public void setCreatorOf ( Set<ContentEntity> creatorOf ) {
        this.creatorOf = creatorOf;
    }


    /**
     * @return the perms
     */
    @Basic
    public int getPerms () {
        return this.perms;
    }


    /**
     * @param perms
     *            the perms to set
     */
    public void setPerms ( int perms ) {
        this.perms = perms;
    }


    /**
     * 
     * @return the permissions as a GrantPermission set
     */
    @Transient
    public Set<GrantPermission> getPermissions () {
        return GrantPermission.fromInt(getPerms());
    }


    /**
     * 
     * @param perms
     */
    public void setPermissions ( Set<GrantPermission> perms ) {
        this.setPerms(GrantPermission.toInt(perms));
    }


    /**
     * @return the creator
     */
    @ManyToOne ( optional = true )
    public User getCreator () {
        return this.creator;
    }


    /**
     * @param creator
     *            the creator to set
     */
    public void setCreator ( User creator ) {
        this.creator = creator;
    }


    /**
     * @return the created
     */
    public DateTime getCreated () {
        return this.created;
    }


    /**
     * @param created
     *            the created to set
     */
    public void setCreated ( DateTime created ) {
        this.created = created;
    }


    /**
     * @return the expires
     */
    public DateTime getExpires () {
        return this.expires;
    }


    /**
     * @param expires
     *            the expires to set
     */
    public void setExpires ( DateTime expires ) {
        this.expires = expires;
    }


    /**
     * @return the lastModified
     */
    public DateTime getLastModified () {
        return this.lastModified;
    }


    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified ( DateTime lastModified ) {
        this.lastModified = lastModified;
    }


    /**
     * 
     * @return display name (equals the share roots name)
     */
    @Transient
    public String getDisplayName () {
        if ( this.entity != null ) {
            return this.entity.getLocalName();
        }
        return this.displayName;
    }


    /**
     * @return whether the root is a collection
     */
    @Transient
    public boolean isCollection () {
        if ( this.entity != null ) {
            return this.entity instanceof VFSContainerEntity;
        }
        return this.collection;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Grant other = (Grant) obj;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        return true;
    }

    // -GENERATED

}
