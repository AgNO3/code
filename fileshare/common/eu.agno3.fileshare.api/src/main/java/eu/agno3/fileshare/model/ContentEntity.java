/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OptimisticLock;
import org.joda.time.DateTime;

import eu.agno3.runtime.util.serialization.SafeSerialization;
import eu.agno3.runtime.util.uuid.UUIDUtil;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "content_entity", uniqueConstraints = {
    @UniqueConstraint ( columnNames = {
        "parent", "localName"
        } )
})
@SafeSerialization
public abstract class ContentEntity implements VFSEntity, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4021998546535125340L;
    private UUID id;
    private long version;
    private String localName;
    private ContainerEntity parent;
    private Set<Grant> grants = new HashSet<>();
    private SecurityLabel securityLabel;
    private Subject owner;

    private DateTime created;
    private User creator;
    private Grant creatorGrant;

    private DateTime lastModified;
    private User lastModifier;
    private Grant lastModifiedGrant;

    private DateTime expires;

    private Set<User> favoriteBy = new HashSet<>();
    private Set<User> hiddenBy = new HashSet<>();

    private boolean hasParent;
    private boolean hasGrants;
    private Set<Grant> localValidGrants = new HashSet<>();
    private DateTime lastMoved;


    /**
     * 
     */
    public ContentEntity () {}


    protected ContentEntity ( ContentEntity e, boolean refs ) {
        this.id = e.id;
        this.version = e.version;
        this.localName = e.localName;
        this.securityLabel = e.securityLabel;
        this.created = e.created;
        this.lastModified = e.lastModified;
        this.expires = e.expires;
        this.lastMoved = e.lastMoved;
        if ( e.owner != null ) {
            this.owner = e.owner.cloneShallow(false);
        }

        if ( refs && e.creator != null ) {
            this.creator = e.creator.cloneShallow(false);
        }

        if ( refs && e.creatorGrant != null ) {
            this.creatorGrant = e.creatorGrant.cloneShallow(false);
        }

        if ( refs && e.lastModifier != null ) {
            this.lastModifier = e.lastModifier.cloneShallow(false);
        }

        if ( refs && e.lastModifiedGrant != null ) {
            this.lastModifiedGrant = e.lastModifiedGrant.cloneShallow(false);
        }

        this.hasParent = e.hasParent();
        this.hasGrants = e.hasGrants();

        if ( refs ) {
            this.localValidGrants.addAll(e.getLocalValidGrants());
        }
    }


    /**
     * @return whether this is a root node
     */
    @Override
    @Transient
    public boolean hasParent () {
        if ( this.parent != null ) {
            return true;
        }

        return this.hasParent;
    }


    /**
     * @return whether this nodes has grants
     */
    @Override
    @Transient
    public boolean hasGrants () {
        if ( !this.grants.isEmpty() || ( this.parent != null && this.parent.hasGrants() ) ) {
            return true;
        }

        return this.hasGrants;
    }


    /**
     * @return whether this nodes has grants
     */
    @Override
    @Transient
    public boolean hasLocalValidGrants () {
        return this.getNumLocalValidGrants() > 0;
    }


    /**
     * @return the number of local grants
     */
    @Transient
    public int getNumLocalValidGrants () {
        return this.getLocalValidGrants().size();
    }


    /**
     * @return the valid local grants
     */
    @Override
    @Transient
    public Set<Grant> getLocalValidGrants () {
        if ( this.grants != null && !this.grants.isEmpty() ) {
            Set<Grant> valid = new HashSet<>();
            for ( Grant g : this.grants ) {
                if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                    continue;
                }
                valid.add(g.cloneShallow());
            }
            return valid;
        }

        return this.localValidGrants;
    }


    /**
     * @return the singleGrant
     */
    @Transient
    public Grant getSingleValidGrant () {
        if ( !this.hasLocalValidGrants() ) {
            return null;
        }
        Set<Grant> g = getLocalValidGrants();
        if ( g != null && g.size() == 1 ) {
            return g.iterator().next();
        }
        return null;
    }


    /**
     * 
     * @param refs
     * @return cloned entity
     */
    @Override
    public abstract VFSEntity cloneShallow ( boolean refs );


    /**
     * 
     * @return cloned entity
     */
    @Override
    public VFSEntity cloneShallow () {
        return cloneShallow(true);
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
     * @return the entity type
     */
    @Override
    @Transient
    public EntityType getEntityType () {
        return EntityType.UNKNOWN;
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
     * @return the localName
     */
    @Override
    @Basic
    public String getLocalName () {
        return this.localName;
    }


    /**
     * @param localName
     *            the localName to set
     */
    @Override
    public void setLocalName ( String localName ) {
        this.localName = localName;
    }


    /**
     * @return the parent
     */
    @ManyToOne ( optional = true )
    @JoinColumn ( name = "parent" )
    public ContainerEntity getParent () {
        return this.parent;
    }


    /**
     * @param parent
     *            the parent to set
     */
    public void setParent ( ContainerEntity parent ) {
        this.parent = parent;
    }


    /**
     * @return the grants
     */
    @ManyToMany ( mappedBy = "entity", fetch = FetchType.LAZY, cascade = {
        CascadeType.REMOVE
    } )
    public Set<Grant> getGrants () {
        return this.grants;
    }


    /**
     * @param grants
     *            the grants to set
     */
    public void setGrants ( Set<Grant> grants ) {
        this.grants = grants;
    }


    /**
     * @return the securityLevel
     */
    @Override
    @ManyToOne
    public SecurityLabel getSecurityLabel () {
        return this.securityLabel;
    }


    /**
     * @param securityLabel
     *            the securityLevel to set
     */
    @Override
    public void setSecurityLabel ( SecurityLabel securityLabel ) {
        this.securityLabel = securityLabel;
    }


    /**
     * @return the owner
     */
    @Override
    @ManyToOne
    public Subject getOwner () {
        return this.owner;
    }


    /**
     * @param owner
     *            the owner to set
     */
    @Override
    public void setOwner ( Subject owner ) {
        this.owner = owner;
    }


    /**
     * @return the created
     */
    @Override
    public DateTime getCreated () {
        return this.created;
    }


    /**
     * @param created
     *            the created to set
     */
    @Override
    public void setCreated ( DateTime created ) {
        this.created = created;
    }


    /**
     * @return the creator
     */
    @Override
    @ManyToOne ( optional = true )
    public User getCreator () {
        return this.creator;
    }


    /**
     * @param creator
     *            the creator to set
     */
    @Override
    public void setCreator ( User creator ) {
        this.creator = creator;
    }


    /**
     * @return the creatorGrant
     */
    @Override
    @ManyToOne ( optional = true )
    public Grant getCreatorGrant () {
        return this.creatorGrant;
    }


    /**
     * @param creatorGrant
     *            the creatorGrant to set
     */
    @Override
    public void setCreatorGrant ( Grant creatorGrant ) {
        this.creatorGrant = creatorGrant;
    }


    /**
     * @return the lastModifier
     */
    @Override
    @ManyToOne ( optional = true )
    @OptimisticLock ( excluded = true )
    public User getLastModifier () {
        return this.lastModifier;
    }


    /**
     * @param lastModifier
     *            the lastModifier to set
     */
    @Override
    public void setLastModifier ( User lastModifier ) {
        this.lastModifier = lastModifier;
    }


    /**
     * @return the lastModifiedGrant
     */
    @Override
    @ManyToOne ( optional = true )
    @OptimisticLock ( excluded = true )
    public Grant getLastModifiedGrant () {
        return this.lastModifiedGrant;
    }


    /**
     * @param lastModifiedGrant
     *            the lastModifiedGrant to set
     */
    @Override
    public void setLastModifiedGrant ( Grant lastModifiedGrant ) {
        this.lastModifiedGrant = lastModifiedGrant;
    }


    /**
     * @return the lastModified
     */
    @Override
    @OptimisticLock ( excluded = true )
    public DateTime getLastModified () {
        return this.lastModified;
    }


    /**
     * @param lastModified
     *            the lastModified to set
     */
    @Override
    public void setLastModified ( DateTime lastModified ) {
        this.lastModified = lastModified;
    }


    /**
     * @return the lastMoved
     */
    @OptimisticLock ( excluded = true )
    public DateTime getLastMoved () {
        return this.lastMoved;
    }


    /**
     * @param lastMoved
     *            the lastMoved to set
     */
    public void setLastMoved ( DateTime lastMoved ) {
        this.lastMoved = lastMoved;
    }


    /**
     * @return the expires
     */
    @Override
    public DateTime getExpires () {
        return this.expires;
    }


    /**
     * @param expires
     *            the expires to set
     */
    @Override
    public void setExpires ( DateTime expires ) {
        this.expires = expires;
    }


    /**
     * @return the favoriteBy
     */
    @ManyToMany ( fetch = FetchType.LAZY, mappedBy = "favoriteEntities" )
    public Set<User> getFavoriteBy () {
        return this.favoriteBy;
    }


    /**
     * @param favoriteBy
     *            the favoriteBy to set
     */
    public void setFavoriteBy ( Set<User> favoriteBy ) {
        this.favoriteBy = favoriteBy;
    }


    /**
     * @return the hiddenBy
     */
    @ManyToMany ( fetch = FetchType.LAZY, mappedBy = "hiddenEntities" )
    public Set<User> getHiddenBy () {
        return this.hiddenBy;
    }


    /**
     * @param hiddenBy
     *            the hiddenBy to set
     */
    public void setHiddenBy ( Set<User> hiddenBy ) {
        this.hiddenBy = hiddenBy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getEntityKey()
     */
    @Override
    @Transient
    public EntityKey getEntityKey () {
        if ( this.id == null ) {
            return null;
        }
        return new NativeEntityKey(this.id);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getInode()
     */
    @Override
    @Transient
    public byte[] getInode () {
        return UUIDUtil.toBytes(this.id);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#isStaticReadOnly()
     */
    @Override
    @Transient
    public boolean isStaticReadOnly () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "entity %s (id: %s, securityLabel: %s, owner: %s)", //$NON-NLS-1$
            this.getLocalName(),
            this.getId(),
            this.getSecurityLabel(),
            this.getOwner());
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        return result;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ContentEntity other = (ContentEntity) obj;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        return true;
    }

}
