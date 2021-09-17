/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2016 by mbechler
 */
package eu.agno3.runtime.security.principal;


import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class UserDetailsImpl implements UserDetails {

    /**
     * 
     */
    private static final long serialVersionUID = 5956636003912411033L;

    private String displayName;
    private String mailAddress;
    private String username;
    private String organization;
    private String organizationUnit;
    private String jobTitle;
    private String preferredLanguage;
    private String timezone;


    /**
     * 
     */
    public UserDetailsImpl () {}


    /**
     * 
     * @param ud
     */
    public UserDetailsImpl ( UserDetails ud ) {
        this.displayName = ud.getDisplayName();
        this.mailAddress = ud.getMailAddress();
        this.username = ud.getUsername();
        this.organization = ud.getOrganization();
        this.organizationUnit = ud.getOrganizationUnit();
        this.jobTitle = ud.getJobTitle();
        this.preferredLanguage = ud.getPreferredLanguage();
        this.timezone = ud.getTimezone();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserDetails#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.displayName;
    }


    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserDetails#getMailAddress()
     */
    @Override
    public String getMailAddress () {
        return this.mailAddress;
    }


    /**
     * @param mailAddress
     *            the mailAddress to set
     */
    public void setMailAddress ( String mailAddress ) {
        this.mailAddress = mailAddress;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserDetails#getUsername()
     */
    @Override
    public String getUsername () {
        return this.username;
    }


    /**
     * @param username
     *            the username to set
     */
    public void setUsername ( String username ) {
        this.username = username;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserDetails#getOrganization()
     */
    @Override
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserDetails#getOrganizationUnit()
     */
    @Override
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


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserDetails#getJobTitle()
     */
    @Override
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserDetails#getPreferredLanguage()
     */
    @Override
    public String getPreferredLanguage () {
        return this.preferredLanguage;
    }


    /**
     * @param preferredLanguage
     *            the preferredLanguage to set
     */
    public void setPreferredLanguage ( String preferredLanguage ) {
        this.preferredLanguage = preferredLanguage;
    }


    /**
     * @return the timezone
     */
    @Override
    public String getTimezone () {
        return this.timezone;
    }


    /**
     * @param tz
     */
    public void setTimezone ( String tz ) {
        this.timezone = tz;
    }
}
