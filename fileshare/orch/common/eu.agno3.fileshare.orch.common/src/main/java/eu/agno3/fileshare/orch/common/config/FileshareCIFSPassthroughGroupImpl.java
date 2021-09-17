/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.hostconfig.storage.CIFSAuthType;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareCIFSPassthroughGroup.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_passthrough_cifs" )
@Audited
@DiscriminatorValue ( "filesh_pt_cifs" )
@Inheritance ( strategy = InheritanceType.JOINED )
public class FileshareCIFSPassthroughGroupImpl extends AbstractFilesharePassthroughGroupImpl<FileshareCIFSPassthroughGroup>
        implements FileshareCIFSPassthroughGroup, FileshareCIFSPassthroughGroupMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 6419676724537035623L;

    private String uncPath;

    private String username;
    private String password;
    private String domain;

    private CIFSAuthType authType;

    private String authRealm;
    private String authKeytab;

    private Boolean enableSigning;

    private Boolean allowSMB1;
    private Boolean disableSMB2;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.AbstractFilesharePassthroughGroupImpl#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareCIFSPassthroughGroup> getType () {
        return FileshareCIFSPassthroughGroup.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.CIFSMountEntry#getUncPath()
     */
    @Override
    public String getUncPath () {
        return this.uncPath;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.CIFSMountEntryMutable#setUncPath(java.lang.String)
     */
    @Override
    public void setUncPath ( String path ) {
        this.uncPath = path;
    }


    /**
     * @return the username
     */
    @Override
    public String getUsername () {
        return this.username;
    }


    /**
     * @param username
     *            the username to set
     */
    @Override
    public void setUsername ( String username ) {
        this.username = username;
    }


    /**
     * @return the password
     */
    @Override
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    @Override
    public void setPassword ( String password ) {
        this.password = password;
    }


    /**
     * @return the domain
     */
    @Override
    public String getDomain () {
        return this.domain;
    }


    /**
     * @param domain
     *            the domain to set
     */
    @Override
    public void setDomain ( String domain ) {
        this.domain = domain;
    }


    /**
     * @return the authType
     */
    @Override
    public CIFSAuthType getAuthType () {
        return this.authType;
    }


    /**
     * @param authType
     *            the authType to set
     */
    @Override

    public void setAuthType ( CIFSAuthType authType ) {
        this.authType = authType;
    }


    /**
     * @return the authRealm
     */
    @Override
    public String getAuthRealm () {
        return this.authRealm;
    }


    /**
     * @param authRealm
     *            the authRealm to set
     */
    @Override
    public void setAuthRealm ( String authRealm ) {
        this.authRealm = authRealm;
    }


    /**
     * @return the authKeytab
     */
    @Override
    public String getAuthKeytab () {
        return this.authKeytab;
    }


    /**
     * @param authKeytab
     *            the authKeytab to set
     */
    @Override
    public void setAuthKeytab ( String authKeytab ) {
        this.authKeytab = authKeytab;
    }


    /**
     * @return the enableSigning
     */
    @Override
    public Boolean getEnableSigning () {
        return this.enableSigning;
    }


    /**
     * @param enableSigning
     *            the enableSigning to set
     */
    @Override
    public void setEnableSigning ( Boolean enableSigning ) {
        this.enableSigning = enableSigning;
    }


    /**
     * @return the allowSMB1
     */
    @Override
    public Boolean getAllowSMB1 () {
        return this.allowSMB1;
    }


    /**
     * @param allowSMB1
     *            the allowSMB1 to set
     */
    @Override
    public void setAllowSMB1 ( Boolean allowSMB1 ) {
        this.allowSMB1 = allowSMB1;
    }


    /**
     * @return the disableSMB2
     */
    @Override
    public Boolean getDisableSMB2 () {
        return this.disableSMB2;
    }


    /**
     * @param disableSMB2
     *            the disableSMB2 to set
     */
    @Override
    public void setDisableSMB2 ( Boolean disableSMB2 ) {
        this.disableSMB2 = disableSMB2;
    }

}
