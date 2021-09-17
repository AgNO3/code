/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.02.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import eu.agno3.runtime.util.serialization.SafeSerialization;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * 
 * The users full name is also stored into a index table to allow for searching
 * 
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "users_details", indexes = {
    @Index ( columnList = "mailAddress" ), @Index ( columnList = "fullName" ), @Index ( columnList = "preferredName" )
} )
@SafeSerialization
public class UserDetails implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4797257837921008172L;

    private long version;

    private UUID id;

    private User user;
    private String fullName;
    private Set<String> fullNameIndex;
    private boolean fullNameVerified;

    private String preferredName;
    private boolean preferredNameVerified;

    private String mailAddress;
    private boolean mailAddressVerified;

    private Boolean preferTextMail;

    private String jobTitle;
    private String organization;
    private String organizationUnit;

    private String salutationName;

    private Set<String> preferredNameIndex;


    /**
     * 
     */
    public UserDetails () {}


    /**
     * @param userDetails
     * @param refs
     */
    public UserDetails ( UserDetails userDetails, boolean refs ) {
        this.id = userDetails.id;
        this.version = userDetails.version;

        if ( refs && userDetails.user != null ) {
            this.user = userDetails.user.cloneShallow(false);
        }

        setFullName(userDetails.fullName);
        this.fullNameVerified = userDetails.fullNameVerified;

        setPreferredName(userDetails.preferredName);
        this.preferredNameVerified = userDetails.preferredNameVerified;

        this.mailAddress = userDetails.mailAddress;
        this.mailAddressVerified = userDetails.mailAddressVerified;

        this.preferTextMail = userDetails.preferTextMail;

        this.jobTitle = userDetails.jobTitle;
        this.organization = userDetails.organization;
        this.organizationUnit = userDetails.organizationUnit;

        this.salutationName = userDetails.salutationName;
    }


    /**
     * @param refs
     * @return cloned object
     */
    public UserDetails cloneShallow ( boolean refs ) {
        return new UserDetails(this, refs);
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
     * @return the user
     */
    @OneToOne
    public User getUser () {
        return this.user;
    }


    /**
     * @param user
     *            the user to set
     */
    public void setUser ( User user ) {
        this.user = user;
    }


    /**
     * @return the fullName
     */
    @Column ( nullable = true )
    public String getFullName () {
        return this.fullName;
    }


    /**
     * @return the fullNameVerified
     */
    public boolean getFullNameVerified () {
        return this.fullNameVerified;
    }


    /**
     * @param fullNameVerified
     *            the fullNameVerified to set
     */
    public void setFullNameVerified ( boolean fullNameVerified ) {
        this.fullNameVerified = fullNameVerified;
    }


    /**
     * @param fullName
     *            the fullName to set
     */
    public void setFullName ( String fullName ) {
        this.fullName = fullName;
        if ( fullName == null ) {
            this.fullNameIndex = Collections.EMPTY_SET;
        }
        else {
            this.fullNameIndex = splitNameForIndex(fullName);
        }
    }


    /**
     * @return the fullNameIndex
     */
    @ElementCollection
    @CollectionTable ( name = "full_name_index", joinColumns = @JoinColumn ( name = "USER_ID" ), indexes = @Index ( columnList = "NAME_PART" ) )
    @Column ( name = "NAME_PART" )
    public Set<String> getFullNameIndex () {
        return this.fullNameIndex;
    }


    /**
     * @param fullNameIndex
     *            the fullNameIndex to set
     */

    public void setFullNameIndex ( Set<String> fullNameIndex ) {
        this.fullNameIndex = fullNameIndex;
    }


    /**
     * @param name
     * @return a tokenized name
     */
    private static Set<String> splitNameForIndex ( String name ) {
        Set<String> res = new HashSet<>();

        for ( String part : StringUtils.split(name, " -") ) { //$NON-NLS-1$
            res.add(part.toLowerCase());
        }

        return res;
    }


    /**
     * @return the preferredName
     */
    @Column ( nullable = true )
    public String getPreferredName () {
        return this.preferredName;

    }


    /**
     * @return the preferredNameVerified
     */
    public boolean getPreferredNameVerified () {
        return this.preferredNameVerified;
    }


    /**
     * @param preferredNameVerified
     *            the preferredNameVerified to set
     */
    public void setPreferredNameVerified ( boolean preferredNameVerified ) {
        this.preferredNameVerified = preferredNameVerified;
    }


    /**
     * @param preferredName
     *            the preferredName to set
     */

    public void setPreferredName ( String preferredName ) {
        this.preferredName = preferredName;
        if ( preferredName == null ) {
            this.preferredNameIndex = Collections.EMPTY_SET;
        }
        else {
            this.preferredNameIndex = splitNameForIndex(preferredName);
        }
    }


    /**
     * @return the preferredNameIndex
     */
    @ElementCollection
    @CollectionTable ( name = "pref_name_index", joinColumns = @JoinColumn ( name = "USER_ID" ), indexes = @Index ( columnList = "NAME_PART" ) )
    @Column ( name = "NAME_PART" )
    public Set<String> getPreferredNameIndex () {
        return this.preferredNameIndex;
    }


    /**
     * @param preferredNameIndex
     *            the preferredNameIndex to set
     */
    public void setPreferredNameIndex ( Set<String> preferredNameIndex ) {
        this.preferredNameIndex = preferredNameIndex;
    }


    /**
     * 
     * @return the name (including titles etc.) used in calling the user
     */
    public String getSalutationName () {
        return this.salutationName;
    }


    /**
     * @param salutationName
     *            the salutationName to set
     */
    public void setSalutationName ( String salutationName ) {
        this.salutationName = salutationName;
    }


    /**
     * @param mailAddress
     *            the mailAddress to set
     */
    public void setMailAddress ( String mailAddress ) {
        this.mailAddress = mailAddress;
    }


    /**
     * @return the mailAddress
     */
    @Column ( nullable = true )
    @ValidEmail
    public String getMailAddress () {
        return this.mailAddress;
    }


    /**
     * @return the mailAddressVerified
     */
    public boolean getMailAddressVerified () {
        return this.mailAddressVerified;
    }


    /**
     * @param mailAddressVerified
     *            the mailAddressVerified to set
     */
    public void setMailAddressVerified ( boolean mailAddressVerified ) {
        this.mailAddressVerified = mailAddressVerified;
    }


    /**
     * @return the preferTextMail
     */
    @Basic ( optional = true )
    public Boolean getPreferTextMail () {
        return this.preferTextMail;
    }


    /**
     * @param preferTextMail
     *            the preferTextMail to set
     */
    public void setPreferTextMail ( Boolean preferTextMail ) {
        this.preferTextMail = preferTextMail;
    }


    /**
     * @return the jobTitle
     */
    @Column ( nullable = true )
    public String getJobTitle () {
        return this.jobTitle;
    }


    /**
     * @param jobTitle
     *            the jobTitle to set
     */
    public void setJobTitle ( String jobTitle ) {
        this.jobTitle = jobTitle;
    }


    /**
     * @return the organization
     */
    @Column ( nullable = true )
    public String getOrganization () {
        return this.organization;
    }


    /**
     * @param organization
     *            the organization to set
     */
    public void setOrganization ( String organization ) {
        this.organization = organization;
    }


    /**
     * @return the organizationUnit
     */
    @Column ( nullable = true )
    public String getOrganizationUnit () {
        return this.organizationUnit;
    }


    /**
     * @param organizationUnit
     *            the organizationUnit to set
     */
    public void setOrganizationUnit ( String organizationUnit ) {
        this.organizationUnit = organizationUnit;
    }

}
