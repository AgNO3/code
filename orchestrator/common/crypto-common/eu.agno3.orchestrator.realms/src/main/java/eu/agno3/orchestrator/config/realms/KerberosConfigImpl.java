/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_realms_kerberos" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "re_kc" )
@MapAs ( KerberosConfig.class )
public class KerberosConfigImpl extends AbstractConfigurationObject<KerberosConfig> implements KerberosConfig, KerberosConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 4467395902262566994L;
    private Boolean dnsLookupKDC = true;
    private Boolean dnsLookupRealm = true;

    private Boolean allowWeakCrypto = false;
    private Set<String> permittedEnctypes = new HashSet<>();
    private Set<String> defaultTicketEnctypes = new HashSet<>();
    private Set<String> defaultTGSEnctypes = new HashSet<>();

    private Boolean disableAddresses = true;

    private Boolean defaultTGTRenewable = false;
    private Boolean defaultTGTProxiable = false;
    private Boolean defaultTGTForwardable = false;

    private Duration maxClockskew;
    private Duration kdcTimeout;
    private Integer maxRetries = 3;
    private Integer udpPreferenceLimit = 1400;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<KerberosConfig> getType () {
        return KerberosConfig.class;
    }


    /**
     * @return the dnsLookupKDC
     */
    @Override
    public Boolean getDnsLookupKDC () {
        return this.dnsLookupKDC;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDnsLookupKDC(java.lang.Boolean)
     */
    @Override
    public void setDnsLookupKDC ( Boolean dnsLookupKDC ) {
        this.dnsLookupKDC = dnsLookupKDC;
    }


    /**
     * @return the dnsLookupRealm
     */
    @Override
    public Boolean getDnsLookupRealm () {
        return this.dnsLookupRealm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDnsLookupRealm(java.lang.Boolean)
     */
    @Override
    public void setDnsLookupRealm ( Boolean dnsLookupRealm ) {
        this.dnsLookupRealm = dnsLookupRealm;
    }


    /**
     * @return the disableAddresses
     */
    @Override
    public Boolean getDisableAddresses () {
        return this.disableAddresses;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDisableAddresses(java.lang.Boolean)
     */
    @Override
    public void setDisableAddresses ( Boolean disableAddresses ) {
        this.disableAddresses = disableAddresses;
    }


    /**
     * @return the defaultTGTForwardable
     */
    @Override
    public Boolean getDefaultTGTForwardable () {
        return this.defaultTGTForwardable;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDefaultTGTForwardable(java.lang.Boolean)
     */
    @Override
    public void setDefaultTGTForwardable ( Boolean defaultTGTForwardable ) {
        this.defaultTGTForwardable = defaultTGTForwardable;
    }


    /**
     * @return the defaultTGTProxiable
     */
    @Override
    public Boolean getDefaultTGTProxiable () {
        return this.defaultTGTProxiable;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDefaultTGTProxiable(java.lang.Boolean)
     */
    @Override
    public void setDefaultTGTProxiable ( Boolean defaultTGTProxiable ) {
        this.defaultTGTProxiable = defaultTGTProxiable;
    }


    /**
     * @return the defaultTGTRenewable
     */
    @Override
    public Boolean getDefaultTGTRenewable () {
        return this.defaultTGTRenewable;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDefaultTGTRenewable(java.lang.Boolean)
     */
    @Override
    public void setDefaultTGTRenewable ( Boolean defaultTGTRenewable ) {
        this.defaultTGTRenewable = defaultTGTRenewable;
    }


    /**
     * @return the allowWeakCrypto
     */
    @Override
    public Boolean getAllowWeakCrypto () {
        return this.allowWeakCrypto;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setAllowWeakCrypto(java.lang.Boolean)
     */
    @Override
    @Basic
    public void setAllowWeakCrypto ( Boolean allowWeakCrypto ) {
        this.allowWeakCrypto = allowWeakCrypto;
    }


    /**
     * @return the permittedEnctypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_realms_kerberos_permenctype" )
    public Set<String> getPermittedEnctypes () {
        return this.permittedEnctypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setPermittedEnctypes(java.util.Set)
     */
    @Override
    public void setPermittedEnctypes ( Set<String> permittedEnctypes ) {
        this.permittedEnctypes = permittedEnctypes;
    }


    /**
     * @return the defaultTGSEnctypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_realms_kerberos_tgsenctype" )
    public Set<String> getDefaultTGSEnctypes () {
        return this.defaultTGSEnctypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDefaultTGSEnctypes(java.util.Set)
     */
    @Override
    public void setDefaultTGSEnctypes ( Set<String> defaultTGSEnctypes ) {
        this.defaultTGSEnctypes = defaultTGSEnctypes;
    }


    /**
     * @return the defaultTicketEnctypes
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_realms_kerberos_tktenctype" )
    public Set<String> getDefaultTicketEnctypes () {
        return this.defaultTicketEnctypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setDefaultTicketEnctypes(java.util.Set)
     */
    @Override
    public void setDefaultTicketEnctypes ( Set<String> defaultTicketEnctypes ) {
        this.defaultTicketEnctypes = defaultTicketEnctypes;
    }


    /**
     * @return the maxClockskewSeconds
     */
    @Override
    public Duration getMaxClockskew () {
        return this.maxClockskew;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setMaxClockskew(org.joda.time.Duration)
     */
    @Override
    public void setMaxClockskew ( Duration maxClockskew ) {
        this.maxClockskew = maxClockskew;
    }


    /**
     * @return the kdcTimeout
     */
    @Override
    public Duration getKdcTimeout () {
        return this.kdcTimeout;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setKdcTimeout(org.joda.time.Duration)
     */
    @Override
    public void setKdcTimeout ( Duration kdcTimeout ) {
        this.kdcTimeout = kdcTimeout;
    }


    /**
     * @return the maxRetries
     */
    @Override
    @Basic
    public Integer getMaxRetries () {
        return this.maxRetries;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setMaxRetries(java.lang.Integer)
     */
    @Override
    public void setMaxRetries ( Integer maxRetries ) {
        this.maxRetries = maxRetries;
    }


    /**
     * @return the udpPreferenceLimit
     */
    @Override
    public Integer getUdpPreferenceLimit () {
        return this.udpPreferenceLimit;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KerberosConfigMutable#setUdpPreferenceLimit(java.lang.Integer)
     */
    @Override
    public void setUdpPreferenceLimit ( Integer udpPreferenceLimit ) {
        this.udpPreferenceLimit = udpPreferenceLimit;
    }

}
