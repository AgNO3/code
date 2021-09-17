/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 *
 */
@Entity
@PersistenceUnit ( unitName = "config" )
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "config_realm_base" )
@Audited
@DiscriminatorValue ( "realm_base" )
public abstract class AbstractRealmConfigImpl <T extends RealmConfig> extends AbstractConfigurationObject<T> implements RealmConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 364673834389979243L;

    private String realmName;
    private String overrideLocalHostname;
    private List<String> domainMappings = new ArrayList<>();
    private Set<CAPathEntry> caPaths = new HashSet<>();
    private Set<KeytabEntry> importKeytabs = new HashSet<>();

    private KerberosSecurityLevel securityLevel;

    private Duration maximumTicketLifetime;

    private Boolean rekeyServices;
    private Duration serviceRekeyInterval;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmConfig#getRealmName()
     */
    @Override
    public String getRealmName () {
        return this.realmName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setRealmName(java.lang.String)
     */
    @Override
    public void setRealmName ( String realmName ) {
        this.realmName = realmName;
    }


    /**
     * @return the overrideLocalHostname
     */
    @Override
    public String getOverrideLocalHostname () {
        return this.overrideLocalHostname;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setOverrideLocalHostname(java.lang.String)
     */
    @Override
    public void setOverrideLocalHostname ( String overrideLocalHostname ) {
        this.overrideLocalHostname = overrideLocalHostname;
    }


    @Override
    @Enumerated ( EnumType.STRING )
    public KerberosSecurityLevel getSecurityLevel () {
        return this.securityLevel;
    }


    @Override
    public void setSecurityLevel ( KerberosSecurityLevel secLevel ) {
        this.securityLevel = secLevel;
    }


    /**
     * @return the domainMappings
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_realms_krbrealm_dommap" )
    public List<String> getDomainMappings () {
        return this.domainMappings;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setDomainMappings(java.util.List)
     */
    @Override
    public void setDomainMappings ( List<String> domainMappings ) {
        this.domainMappings = domainMappings;
    }


    /**
     * @return the caPaths
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = CAPathEntryImpl.class )
    public Set<CAPathEntry> getCaPaths () {
        return this.caPaths;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setCaPaths(java.util.Set)
     */
    @Override
    public void setCaPaths ( Set<CAPathEntry> caPaths ) {
        this.caPaths = caPaths;
    }


    /**
     * @return the importKeytabs
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = KeytabEntryImpl.class )
    public Set<KeytabEntry> getImportKeytabs () {
        return this.importKeytabs;
    }


    /**
     * @param importKeytabs
     *            the importKeytabs to set
     */
    @Override
    public void setImportKeytabs ( Set<KeytabEntry> importKeytabs ) {
        this.importKeytabs = importKeytabs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmConfig#getMaximumTicketLifetime()
     */
    @Override
    public Duration getMaximumTicketLifetime () {
        return this.maximumTicketLifetime;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmConfigMutable#setMaximumTicketLifetime(org.joda.time.Duration)
     */
    @Override
    public void setMaximumTicketLifetime ( Duration maximumTicketLifetime ) {
        this.maximumTicketLifetime = maximumTicketLifetime;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmConfig#getServiceRekeyInterval()
     */
    @Override
    public Duration getServiceRekeyInterval () {
        return this.serviceRekeyInterval;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmConfigMutable#setServiceRekeyInterval(org.joda.time.Duration)
     */
    @Override
    public void setServiceRekeyInterval ( Duration serviceRekeyInterval ) {
        this.serviceRekeyInterval = serviceRekeyInterval;
    }


    /**
     * @return the rekeyServices
     */
    @Override
    public Boolean getRekeyServices () {
        return this.rekeyServices;
    }


    /**
     * @param rekeyServices
     *            the rekeyServices to set
     */
    @Override
    public void setRekeyServices ( Boolean rekeyServices ) {
        this.rekeyServices = rekeyServices;
    }


    /**
     * @param obj
     */
    public void clone ( RealmConfig obj ) {
        this.overrideLocalHostname = obj.getOverrideLocalHostname();
        this.realmName = obj.getOverrideLocalHostname();
        this.caPaths = CAPathEntryImpl.clone(obj.getCaPaths());
        this.domainMappings = new ArrayList<>(obj.getDomainMappings());
        this.importKeytabs = KeytabEntryImpl.clone(obj.getImportKeytabs());
        this.maximumTicketLifetime = obj.getMaximumTicketLifetime();
        this.serviceRekeyInterval = obj.getServiceRekeyInterval();
        this.rekeyServices = obj.getRekeyServices();
        doClone(obj);
    }


    /**
     * @param obj
     */
    protected abstract void doClone ( RealmConfig obj );

}
