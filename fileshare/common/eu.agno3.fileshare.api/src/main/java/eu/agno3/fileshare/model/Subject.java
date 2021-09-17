/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.OptimisticLock;
import org.joda.time.DateTime;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "subjects", indexes = @Index ( columnList = "synchronizationHint", unique = true ) )
@SafeSerialization
public abstract class Subject implements Serializable, SubjectInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 3841019539276902591L;
    private UUID id;
    private long version;
    private Set<Group> memberships = new HashSet<>();
    private Set<SubjectGrant> grants = new HashSet<>();
    private ContainerEntity subjectRoot;
    private SubjectType type;

    private Long quota;

    private Set<String> roles = new HashSet<>();
    private String synchronizationHint;

    private Set<User> favoriteBy = new HashSet<>();
    private Set<User> hiddenBy = new HashSet<>();

    private boolean haveSubjectRoot;

    private DateTime lastModified;
    private DateTime expiration;

    private User creator;
    private DateTime created;


    /**
     * 
     */
    public Subject () {}


    protected Subject ( Subject s, boolean refs ) {
        this.id = s.id;
        this.version = s.version;
        this.type = s.type;
        this.quota = s.quota;
        this.roles = new HashSet<>(s.roles);
        this.haveSubjectRoot = s.haveSubjectRoot();
        this.expiration = s.expiration;
        if ( refs && s.creator != null ) {
            this.creator = s.creator.cloneShallow(false);
        }
        this.created = s.created;
    }


    /**
     * @return whether this subject has a root
     */
    public boolean haveSubjectRoot () {
        if ( this.subjectRoot != null ) {
            return true;
        }
        return this.haveSubjectRoot;
    }


    /**
     * 
     * @param refs
     * @return cloned subject
     */
    public abstract Subject cloneShallow ( boolean refs );


    /**
     * 
     * @return cloned subject
     */
    public Subject cloneShallow () {
        return cloneShallow(true);
    }


    /**
     * @return the id
     */
    @Override
    @Id
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
     * @return the type
     */
    @Override
    @Enumerated ( EnumType.ORDINAL )
    public SubjectType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( SubjectType type ) {
        this.type = type;
    }


    /**
     * @return the synchronization hint
     */
    public String getSynchronizationHint () {
        return this.synchronizationHint;
    }


    /**
     * @param synchronizationHint
     *            the synchronizationHint to set
     */
    public void setSynchronizationHint ( String synchronizationHint ) {
        this.synchronizationHint = synchronizationHint;
    }


    /**
     * @return the memberships
     */
    @ManyToMany ( mappedBy = "members", fetch = FetchType.LAZY )
    public Set<Group> getMemberships () {
        return this.memberships;
    }


    /**
     * @param memberships
     *            the memberships to set
     */
    public void setMemberships ( Set<Group> memberships ) {
        this.memberships = memberships;
    }


    /**
     * @return the last modified time of the group
     */
    @OptimisticLock ( excluded = true )
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
     * @return the expiration
     */
    public DateTime getExpiration () {
        return this.expiration;
    }


    /**
     * @param expiration
     *            the expiration to set
     */
    public void setExpiration ( DateTime expiration ) {
        this.expiration = expiration;
    }


    /**
     * @return the quota
     */
    @Basic
    @Column ( nullable = true )
    public Long getQuota () {
        return this.quota;
    }


    /**
     * @param quota
     *            the quota to set
     */
    public void setQuota ( Long quota ) {
        this.quota = quota;
    }


    /**
     * @return the subjectRoot
     */
    @OneToOne ( cascade = {
        CascadeType.ALL
    }, optional = true, fetch = FetchType.EAGER )
    public ContainerEntity getSubjectRoot () {
        return this.subjectRoot;
    }


    /**
     * @param subjectRoot
     *            the subjectRoot to set
     */
    public void setSubjectRoot ( ContainerEntity subjectRoot ) {
        this.subjectRoot = subjectRoot;
    }


    /**
     * @return the grants
     */
    @ManyToMany ( mappedBy = "target", cascade = {
        CascadeType.REMOVE
    } )
    public Set<SubjectGrant> getGrants () {
        return this.grants;
    }


    /**
     * @param grants
     *            the grants to set
     */
    public void setGrants ( Set<SubjectGrant> grants ) {
        this.grants = grants;
    }


    /**
     * @return the roles
     */
    @ElementCollection
    @CollectionTable ( name = "subject_roles" )
    public Set<String> getRoles () {
        return this.roles;
    }


    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles ( Set<String> roles ) {
        this.roles = roles;
    }


    /**
     * @return the creator
     */
    @ManyToOne ( fetch = FetchType.LAZY, optional = true )
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
     * @return the favoriteBy
     */
    @ManyToMany ( fetch = FetchType.LAZY, mappedBy = "favoriteSubjects" )
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
    @ManyToMany ( fetch = FetchType.LAZY, mappedBy = "hiddenSubjects" )
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    // +GENERATED
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Subject other = (Subject) obj;
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
