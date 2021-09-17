/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "users", indexes = {
    @Index ( columnList = "userId", unique = true ), @Index ( columnList = "realmName,userName", unique = true )
})
public class User extends Subject implements UserInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -8513145533589307668L;
    private UserPrincipal principal;
    private Set<Grant> createdGrants = new HashSet<>();
    private Set<ContentEntity> createdEntities = new HashSet<>();
    private Set<ContentEntity> lastModifiedEntities = new HashSet<>();

    private UserDetails userDetails;
    private Map<String, String> preferences;
    private SecurityLabel securityLabel;

    private Set<ContentEntity> favoriteEntities = new HashSet<>();
    private Set<Subject> favoriteSubjects = new HashSet<>();

    private Set<ContentEntity> hiddenEntities = new HashSet<>();
    private Set<Subject> hiddenSubjects = new HashSet<>();
    private boolean noSubjectRoot;

    private boolean linksFavorite;
    private Set<String> mailFavorites = new HashSet<>();

    private boolean linksHidden;
    private Set<String> mailHidden = new HashSet<>();

    private Set<Subject> creatorOf = new HashSet<>();


    /**
     * 
     */
    public User () {}


    /**
     * 
     * @param u
     * @param refs
     */
    public User ( User u, boolean refs ) {
        super(u, refs);
        this.principal = u.principal;
        if ( u.userDetails != null ) {
            this.userDetails = u.userDetails.cloneShallow(false);
        }
        this.securityLabel = u.securityLabel;
        this.noSubjectRoot = u.noSubjectRoot;
        this.linksFavorite = u.linksFavorite;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Subject#cloneShallow(boolean)
     */
    @Override
    public User cloneShallow ( boolean refs ) {
        return new User(this, refs);
    }


    /**
     * @return the principal
     */
    @Override
    @Embedded
    public UserPrincipal getPrincipal () {
        return this.principal;
    }


    @Override
    @Transient
    public String getRealm () {
        return this.principal != null ? this.principal.getRealmName() : null;
    }


    /**
     * @param principal
     *            the principal to set
     */
    public void setPrincipal ( UserPrincipal principal ) {
        this.principal = principal;
    }


    /**
     * @return the grants created by this user
     */
    @OneToMany ( mappedBy = "creator", fetch = FetchType.LAZY )
    public Set<Grant> getCreatedGrants () {
        return this.createdGrants;
    }


    /**
     * @param createdGrants
     *            the createdGrants to set
     */
    public void setCreatedGrants ( Set<Grant> createdGrants ) {
        this.createdGrants = createdGrants;
    }


    /**
     * @return the entities created by this user
     */
    @OneToMany ( mappedBy = "creator", fetch = FetchType.LAZY )
    public Set<ContentEntity> getCreatedEntities () {
        return this.createdEntities;
    }


    /**
     * @param createdEntities
     *            the createdEntities to set
     */
    public void setCreatedEntities ( Set<ContentEntity> createdEntities ) {
        this.createdEntities = createdEntities;
    }


    /**
     * @return the lastModifiedEntities
     */
    @OneToMany ( mappedBy = "lastModifier", fetch = FetchType.LAZY )
    public Set<ContentEntity> getLastModifiedEntities () {
        return this.lastModifiedEntities;
    }


    /**
     * @param lastModifiedEntities
     *            the lastModifiedEntities to set
     */
    public void setLastModifiedEntities ( Set<ContentEntity> lastModifiedEntities ) {
        this.lastModifiedEntities = lastModifiedEntities;
    }


    /**
     * @return the userDetails
     */
    @OneToOne ( mappedBy = "user", cascade = {
        CascadeType.ALL
    }, fetch = FetchType.LAZY )
    public UserDetails getUserDetails () {
        return this.userDetails;
    }


    /**
     * @param userDetails
     *            the userDetails to set
     */
    public void setUserDetails ( UserDetails userDetails ) {
        this.userDetails = userDetails;
    }


    /**
     * @return the securityLevel
     */
    @ManyToOne
    public SecurityLabel getSecurityLabel () {
        return this.securityLabel;
    }


    /**
     * @param securityLabel
     *            the securityLevel to set
     */
    public void setSecurityLabel ( SecurityLabel securityLabel ) {
        this.securityLabel = securityLabel;
    }


    /**
     * 
     * @return the saved user preferences
     */
    @ElementCollection ( fetch = FetchType.LAZY )
    @JoinTable ( name = "user_preferences", joinColumns = @JoinColumn ( name = "id" ) )
    @MapKeyColumn ( name = "prefkey", length = 64 )
    @Column ( name = "value" )
    public Map<String, String> getPreferences () {
        return this.preferences;
    }


    /**
     * @param preferences
     *            the preferences to set
     */
    public void setPreferences ( Map<String, String> preferences ) {
        this.preferences = preferences;
    }


    /**
     * @return the favoriteEntities
     */
    @ManyToMany ( fetch = FetchType.LAZY )
    @JoinTable ( name = "user_favorite_entities" )
    public Set<ContentEntity> getFavoriteEntities () {
        return this.favoriteEntities;
    }


    /**
     * @param favoriteEntities
     *            the favoriteEntities to set
     */
    public void setFavoriteEntities ( Set<ContentEntity> favoriteEntities ) {
        this.favoriteEntities = favoriteEntities;
    }


    /**
     * @return the favoriteSubjects
     */
    @ManyToMany ( fetch = FetchType.LAZY )
    @JoinTable ( name = "user_favorite_subjects" )
    public Set<Subject> getFavoriteSubjects () {
        return this.favoriteSubjects;
    }


    /**
     * @param favoriteSubjects
     *            the favoriteSubjects to set
     */
    public void setFavoriteSubjects ( Set<Subject> favoriteSubjects ) {
        this.favoriteSubjects = favoriteSubjects;
    }


    /**
     * @return the hiddenEntities
     */
    @ManyToMany ( fetch = FetchType.LAZY )
    @JoinTable ( name = "user_hidden_entities" )
    public Set<ContentEntity> getHiddenEntities () {
        return this.hiddenEntities;
    }


    /**
     * @param hiddenEntities
     *            the hiddenEntities to set
     */
    public void setHiddenEntities ( Set<ContentEntity> hiddenEntities ) {
        this.hiddenEntities = hiddenEntities;
    }


    /**
     * @return the hiddenSubjects
     */
    @ManyToMany ( fetch = FetchType.LAZY )
    @JoinTable ( name = "user_hidden_subjects" )
    public Set<Subject> getHiddenSubjects () {
        return this.hiddenSubjects;
    }


    /**
     * @param hiddenSubjects
     *            the hiddenSubjects to set
     */
    public void setHiddenSubjects ( Set<Subject> hiddenSubjects ) {
        this.hiddenSubjects = hiddenSubjects;
    }


    /**
     * @return the mailFavorites
     */
    @ElementCollection ( fetch = FetchType.LAZY )
    @CollectionTable ( name = "user_mail_favorites" )
    public Set<String> getMailFavorites () {
        return this.mailFavorites;
    }


    /**
     * @param mailFavorites
     *            the mailFavorites to set
     */
    public void setMailFavorites ( Set<String> mailFavorites ) {
        this.mailFavorites = mailFavorites;
    }


    /**
     * @return the linksFavorite
     */
    public boolean getLinksFavorite () {
        return this.linksFavorite;
    }


    /**
     * @param linksFavorite
     *            the linksFavorite to set
     */
    public void setLinksFavorite ( boolean linksFavorite ) {
        this.linksFavorite = linksFavorite;
    }


    /**
     * @return the mailHidden
     */
    @ElementCollection ( fetch = FetchType.LAZY )
    @CollectionTable ( name = "user_mail_hidden" )
    public Set<String> getMailHidden () {
        return this.mailHidden;
    }


    /**
     * @param mailHidden
     *            the mailHidden to set
     */
    public void setMailHidden ( Set<String> mailHidden ) {
        this.mailHidden = mailHidden;
    }


    /**
     * @return the linksHidden
     */
    public boolean getLinksHidden () {
        return this.linksHidden;
    }


    /**
     * @param linksHidden
     *            the linksHidden to set
     */
    public void setLinksHidden ( boolean linksHidden ) {
        this.linksHidden = linksHidden;
    }


    /**
     * @return whether the user has no subject root
     */
    public boolean getNoSubjectRoot () {
        return this.noSubjectRoot;
    }


    /**
     * @param noSubjectRoot
     *            the noSubjectRoot to set
     */
    public void setNoSubjectRoot ( boolean noSubjectRoot ) {
        this.noSubjectRoot = noSubjectRoot;
    }


    /**
     * @return the creatorOf
     */
    @OneToMany ( mappedBy = "creator" )
    public Set<Subject> getCreatorOf () {
        return this.creatorOf;
    }


    /**
     * @param creatorOf
     *            the creatorOf to set
     */
    public void setCreatorOf ( Set<Subject> creatorOf ) {
        this.creatorOf = creatorOf;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.SubjectInfo#getNameSource()
     */
    @Override
    @Transient
    public NameSource getNameSource () {
        if ( this.userDetails == null ) {
            return NameSource.USERNAME;
        }

        if ( !StringUtils.isBlank(this.userDetails.getPreferredName()) && this.userDetails.getPreferredNameVerified() ) {
            return NameSource.FULL_NAME;
        }

        if ( !StringUtils.isBlank(this.userDetails.getFullName()) && this.userDetails.getFullNameVerified() ) {
            return NameSource.FULL_NAME;
        }

        if ( !StringUtils.isBlank(this.userDetails.getMailAddress()) && this.userDetails.getMailAddressVerified() ) {
            return NameSource.MAIL;
        }

        return NameSource.USERNAME;
    }


    @Override
    @Transient
    public String getUserDisplayName () {
        if ( this.userDetails == null ) {
            return formatPrincipal(this.getPrincipal());
        }

        if ( !StringUtils.isBlank(this.userDetails.getPreferredName()) && this.userDetails.getPreferredNameVerified() ) {
            return this.userDetails.getPreferredName();
        }

        if ( !StringUtils.isBlank(this.userDetails.getFullName()) && this.userDetails.getFullNameVerified() ) {
            return this.userDetails.getFullName();
        }

        if ( !StringUtils.isBlank(this.userDetails.getMailAddress()) && this.userDetails.getMailAddressVerified() ) {
            return this.userDetails.getMailAddress();
        }

        return formatPrincipal(this.getPrincipal());
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("user: %s (princ: %s, id: %s)", this.getUserDisplayName(), formatPrincipal(this.getPrincipal()), this.getId()); //$NON-NLS-1$
    }


    /**
     * @param princ
     * @return formatted principal name
     */
    public static String formatPrincipal ( UserPrincipal princ ) {
        if ( "LOCAL".equals(princ.getRealmName()) ) { //$NON-NLS-1$
            return princ.getUserName();
        }
        return String.format("%s (%s)", princ.getUserName(), princ.getRealmName()); //$NON-NLS-1$
    }

}
