/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.system;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationImpl;
import eu.agno3.orchestrator.config.web.RuntimeConfigurationMutable;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( SystemConfiguration.class )
@Entity
@Table ( name = "config_hostconfig_system" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_sys" )
public class SystemConfigurationImpl extends AbstractConfigurationObject<SystemConfiguration>
        implements SystemConfiguration, SystemConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -2321523169593411188L;
    private Integer swapiness;

    private Boolean enableSshAccess;
    private Boolean sshKeyOnly;
    private Set<PublicKeyEntry> adminSshPublicKeys = new HashSet<>();

    private RuntimeConfigurationImpl agentConfig;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<SystemConfiguration> getType () {
        return SystemConfiguration.class;
    }


    /**
     * @return the enableSshAccess
     */
    @Override
    @Basic
    public Boolean getEnableSshAccess () {
        return this.enableSshAccess;
    }


    /**
     * @param enableSshAccess
     *            the enableSshAccess to set
     */
    @Override
    public void setEnableSshAccess ( Boolean enableSshAccess ) {
        this.enableSshAccess = enableSshAccess;
    }


    /**
     * @return the sshKeyOnly
     */
    @Override
    public Boolean getSshKeyOnly () {
        return this.sshKeyOnly;
    }


    /**
     * @param sshKeyOnly
     *            the sshKeyOnly to set
     */
    @Override
    public void setSshKeyOnly ( Boolean sshKeyOnly ) {
        this.sshKeyOnly = sshKeyOnly;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.system.SystemConfiguration#getAdminSshPublicKeys()
     */
    @Override
    @Audited ( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @JoinTable ( name = "config_hostconfig_system_admpub" )
    @ManyToMany ( cascade = {} )
    public Set<PublicKeyEntry> getAdminSshPublicKeys () {
        return this.adminSshPublicKeys;
    }


    /**
     * @param adminSSHPublicKeys
     *            the adminSSHPublicKeys to set
     */
    @Override
    public void setAdminSshPublicKeys ( Set<PublicKeyEntry> adminSSHPublicKeys ) {
        this.adminSshPublicKeys = adminSSHPublicKeys;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.system.SystemConfiguration#getSwapiness()
     */
    @Override
    @Column ( nullable = true )
    @Basic
    public Integer getSwapiness () {
        return this.swapiness;
    }


    /**
     * @param swapiness
     *            the swapiness to set
     */
    @Override
    public void setSwapiness ( Integer swapiness ) {
        this.swapiness = swapiness;
    }


    /**
     * @return the agentConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = RuntimeConfigurationImpl.class )
    public RuntimeConfigurationMutable getAgentConfig () {
        return this.agentConfig;
    }


    /**
     * @param agentConfig
     *            the agentConfig to set
     */
    @Override
    public void setAgentConfig ( RuntimeConfigurationMutable agentConfig ) {
        this.agentConfig = (RuntimeConfigurationImpl) agentConfig;
    }

}
