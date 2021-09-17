/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import eu.agno3.runtime.security.ldap.LDAPUser;


/**
 * @author mbechler
 *
 */
public class LDAPUserImpl implements LDAPUser {

    /**
     * 
     */
    private static final long serialVersionUID = -4415807049391937125L;

    private String displayName;
    private String mailAddress;
    private String username;
    private String organization;
    private String organizationUnit;
    private String jobTitle;
    private String preferredLanguage;
    private String timezone;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUser#getDisplayName()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUser#getMailAddress()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUser#getUsername()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUser#getOrganization()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUser#getOrganizationUnit()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUser#getJobTitle()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPUser#getPreferredLanguage()
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
