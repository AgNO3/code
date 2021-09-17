/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_realms_adrealm" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "re_ad" )
@MapAs ( ADRealmConfig.class )
public class ADRealmConfigImpl extends AbstractRealmConfigImpl<ADRealmConfig> implements ADRealmConfig, ADRealmConfigMutable, RealmConfig {

    /**
     * 
     */
    private static final long serialVersionUID = -5172526330266997408L;

    private String overrideMachineAccount;
    private String overrideNetbiosHostname;
    private String overrideNetbiosDomainName;
    private String machineBaseDN;

    private Boolean rekeyMachineAccount;
    private Duration machineRekeyInterval;

    private Boolean updateDNS;
    private String updateDNSFromInterface;
    private Duration updateDNSTTL;

    private Boolean updateDNSForceSecure;

    private Boolean doJoin;
    private Boolean doRekey;
    private Boolean doLeave;

    private ADJoinType joinType;

    private String customMachineJoinPassword;
    private String joinUser;
    private String joinPassword;

    private Boolean allowSMB1;
    private Boolean disableSMB2;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<ADRealmConfig> getType () {
        return ADRealmConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmConfig#getRealmType()
     */
    @Override
    @Transient
    public RealmType getRealmType () {
        return RealmType.AD;
    }


    /**
     * 
     * @param type
     */
    @Override
    public void setRealmType ( RealmType type ) {
        // ignored
    }


    /**
     * @return the overrideMachineAccount
     */
    @Override
    public String getOverrideMachineAccount () {
        return this.overrideMachineAccount;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfigMutable#setOverrideMachineAccount(java.lang.String)
     */
    @Override
    public void setOverrideMachineAccount ( String overrideMachineAccount ) {
        this.overrideMachineAccount = overrideMachineAccount;
    }


    /**
     * @return the overrideNetbiosDomainName
     */
    @Override
    public String getOverrideNetbiosDomainName () {
        return this.overrideNetbiosDomainName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfigMutable#setOverrideNetbiosDomainName(java.lang.String)
     */
    @Override
    public void setOverrideNetbiosDomainName ( String overrideNetbiosDomainName ) {
        this.overrideNetbiosDomainName = overrideNetbiosDomainName;
    }


    /**
     * @return the overrideNetbiosHostname
     */
    @Override
    public String getOverrideNetbiosHostname () {
        return this.overrideNetbiosHostname;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfigMutable#setOverrideNetbiosHostname(java.lang.String)
     */
    @Override
    public void setOverrideNetbiosHostname ( String overrideNetbiosHostname ) {
        this.overrideNetbiosHostname = overrideNetbiosHostname;
    }


    /**
     * @return the machineBaseDN
     */
    @Override
    public String getMachineBaseDN () {
        return this.machineBaseDN;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfigMutable#setMachineBaseDN(java.lang.String)
     */
    @Override
    public void setMachineBaseDN ( String machineBaseDN ) {
        this.machineBaseDN = machineBaseDN;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfig#getMachineRekeyInterval()
     */
    @Override
    public Duration getMachineRekeyInterval () {
        return this.machineRekeyInterval;
    }


    /**
     * @param machineRekeyInterval
     *            the machineRekeyInterval to set
     */
    @Override
    public void setMachineRekeyInterval ( Duration machineRekeyInterval ) {
        this.machineRekeyInterval = machineRekeyInterval;
    }


    /**
     * @return the rekeyMachineAccount
     */
    @Override
    public Boolean getRekeyMachineAccount () {
        return this.rekeyMachineAccount;
    }


    /**
     * @param rekeyMachineAccount
     *            the rekeyMachineAccount to set
     */
    @Override
    public void setRekeyMachineAccount ( Boolean rekeyMachineAccount ) {
        this.rekeyMachineAccount = rekeyMachineAccount;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfig#getUpdateDns()
     */
    @Override
    public Boolean getUpdateDns () {
        return this.updateDNS;
    }


    /**
     * @param updateDNS
     *            the updateDNS to set
     */
    @Override
    public void setUpdateDns ( Boolean updateDNS ) {
        this.updateDNS = updateDNS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfig#getUpdateDnsFromInterface()
     */
    @Override
    public String getUpdateDnsFromInterface () {
        return this.updateDNSFromInterface;
    }


    /**
     * @param intf
     *            the updateDNSFromInterface to set
     */
    @Override
    public void setUpdateDnsFromInterface ( String intf ) {
        this.updateDNSFromInterface = intf;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfig#getUpdateDnsTtl()
     */
    @Override
    public Duration getUpdateDnsTtl () {
        return this.updateDNSTTL;
    }


    /**
     * @param ttl
     *            the updateDNSTTL to set
     */
    @Override
    public void setUpdateDnsTtl ( Duration ttl ) {
        this.updateDNSTTL = ttl;
    }


    /**
     * @return the updateDNSForceSecure
     */
    @Override
    public Boolean getUpdateDnsForceSecure () {
        return this.updateDNSForceSecure;
    }


    /**
     * @param forceSecure
     */
    @Override
    public void setUpdateDnsForceSecure ( Boolean forceSecure ) {
        this.updateDNSForceSecure = forceSecure;
    }


    /**
     * @return the doJoin
     */
    @Override
    public Boolean getDoJoin () {
        return this.doJoin;
    }


    /**
     * @param doJoin
     *            the doJoin to set
     */
    @Override
    public void setDoJoin ( Boolean doJoin ) {
        this.doJoin = doJoin;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.ADRealmConfig#getDoRekey()
     */
    @Override
    public Boolean getDoRekey () {
        return this.doRekey;
    }


    /**
     * @param doRekey
     *            the doRekey to set
     */
    @Override
    public void setDoRekey ( Boolean doRekey ) {
        this.doRekey = doRekey;
    }


    /**
     * @return the doLeave
     */
    @Override
    public Boolean getDoLeave () {
        return this.doLeave;
    }


    /**
     * @param doLeave
     *            the doLeave to set
     */
    @Override
    public void setDoLeave ( Boolean doLeave ) {
        this.doLeave = doLeave;
    }


    /**
     * @return the joinType
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public ADJoinType getJoinType () {
        return this.joinType;
    }


    /**
     * @param joinType
     *            the joinType to set
     */
    @Override
    public void setJoinType ( ADJoinType joinType ) {
        this.joinType = joinType;
    }


    /**
     * @return the joinUser
     */
    @Override
    public String getJoinUser () {
        return this.joinUser;
    }


    /**
     * @param joinUser
     *            the joinUser to set
     */
    @Override
    public void setJoinUser ( String joinUser ) {
        this.joinUser = joinUser;
    }


    /**
     * @return the joinPassword
     */
    @Override
    public String getJoinPassword () {
        return this.joinPassword;
    }


    /**
     * @param joinPassword
     *            the joinPassword to set
     */
    @Override
    public void setJoinPassword ( String joinPassword ) {
        this.joinPassword = joinPassword;
    }


    /**
     * @return the customMachineJoinPassword
     */
    @Override
    public String getCustomMachineJoinPassword () {
        return this.customMachineJoinPassword;
    }


    /**
     * @param customMachineJoinPassword
     *            the customMachineJoinPassword to set
     */
    @Override
    public void setCustomMachineJoinPassword ( String customMachineJoinPassword ) {
        this.customMachineJoinPassword = customMachineJoinPassword;
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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.AbstractRealmConfigImpl#doClone(eu.agno3.orchestrator.config.realms.RealmConfig)
     */
    @Override
    protected void doClone ( RealmConfig obj ) {
        if ( ! ( obj instanceof ADRealmConfig ) ) {
            throw new IllegalArgumentException();
        }

        ADRealmConfig o = (ADRealmConfig) obj;
        this.machineBaseDN = o.getMachineBaseDN();
        this.overrideMachineAccount = o.getOverrideMachineAccount();
        this.overrideNetbiosDomainName = o.getOverrideNetbiosDomainName();
        this.overrideNetbiosHostname = o.getOverrideNetbiosHostname();
        this.machineRekeyInterval = o.getMachineRekeyInterval();
        this.rekeyMachineAccount = o.getRekeyMachineAccount();
        this.updateDNS = o.getUpdateDns();
        this.updateDNSFromInterface = o.getUpdateDnsFromInterface();
        this.updateDNSTTL = o.getUpdateDnsTtl();
        this.customMachineJoinPassword = o.getCustomMachineJoinPassword();
        this.joinType = o.getJoinType();
        this.joinUser = o.getJoinUser();
        this.joinPassword = o.getJoinPassword();
        this.allowSMB1 = o.getAllowSMB1();
        this.disableSMB2 = o.getDisableSMB2();
    }

}
