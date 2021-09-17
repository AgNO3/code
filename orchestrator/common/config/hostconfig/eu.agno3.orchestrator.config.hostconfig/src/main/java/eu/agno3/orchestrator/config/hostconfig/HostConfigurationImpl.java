/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfigImpl;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfigMutable;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfigImpl;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfigMutable;
import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.system.SystemConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.system.SystemConfigurationMutable;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.orchestrator.config.realms.RealmsConfigImpl;
import eu.agno3.orchestrator.config.realms.RealmsConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( HostConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_hostconfig" )
@Audited
@DiscriminatorValue ( "hc" )
public class HostConfigurationImpl extends AbstractConfigurationInstance<HostConfiguration> implements HostConfiguration, HostConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4734162889218308610L;

    private HostIdentificationImpl hostIdentification;
    private SystemConfigurationImpl systemConfiguration;
    private DateTimeConfigurationImpl dateTimeConfiguration;
    private NetworkConfigurationImpl networkConfiguration;
    private ResolverConfigurationImpl resolverConfiguration;
    private TruststoresConfigImpl trustConfiguration;
    private KeystoresConfigImpl keystoreConfiguration;
    private StorageConfigurationImpl storageConfiguration;
    private RealmsConfigImpl realmsConfiguration;
    private MailingConfigurationImpl mailingConfiguration;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<HostConfiguration> getType () {
        return HostConfiguration.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getSystemConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = HostIdentificationImpl.class )
    public HostIdentificationMutable getHostIdentification () {
        return this.hostIdentification;
    }


    @Override
    public void setHostIdentification ( HostIdentificationMutable hostIdentification ) {
        this.hostIdentification = (HostIdentificationImpl) hostIdentification;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getSystemConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = SystemConfigurationImpl.class )
    public SystemConfigurationMutable getSystemConfiguration () {
        return this.systemConfiguration;
    }


    /**
     * @param systemConfiguration
     *            the systemConfiguration to set
     */
    @Override
    public void setSystemConfiguration ( SystemConfigurationMutable systemConfiguration ) {
        this.systemConfiguration = (SystemConfigurationImpl) systemConfiguration;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getDateTimeConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = DateTimeConfigurationImpl.class )
    public DateTimeConfigurationMutable getDateTimeConfiguration () {
        return this.dateTimeConfiguration;
    }


    /**
     * @param dateTimeConfiguration
     *            the dateTimeConfiguration to set
     */
    @Override
    public void setDateTimeConfiguration ( DateTimeConfigurationMutable dateTimeConfiguration ) {
        this.dateTimeConfiguration = (DateTimeConfigurationImpl) dateTimeConfiguration;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getNetworkConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = NetworkConfigurationImpl.class )
    public NetworkConfigurationMutable getNetworkConfiguration () {
        return this.networkConfiguration;
    }


    /**
     * @param networkConfiguration
     *            the networkConfiguration to set
     */
    @Override
    public void setNetworkConfiguration ( NetworkConfigurationMutable networkConfiguration ) {
        this.networkConfiguration = (NetworkConfigurationImpl) networkConfiguration;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getResolverConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = ResolverConfigurationImpl.class )
    public ResolverConfigurationMutable getResolverConfiguration () {
        return this.resolverConfiguration;
    }


    /**
     * @param resolverConfiguration
     *            the resolverConfiguration to set
     */
    @Override
    public void setResolverConfiguration ( ResolverConfigurationMutable resolverConfiguration ) {
        this.resolverConfiguration = (ResolverConfigurationImpl) resolverConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getTrustConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = TruststoresConfigImpl.class )
    public TruststoresConfigMutable getTrustConfiguration () {
        return this.trustConfiguration;
    }


    /**
     * @param trustConfiguration
     *            the trustConfiguration to set
     */
    @Override
    public void setTrustConfiguration ( TruststoresConfigMutable trustConfiguration ) {
        this.trustConfiguration = (TruststoresConfigImpl) trustConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getKeystoreConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = KeystoresConfigImpl.class )
    public KeystoresConfigMutable getKeystoreConfiguration () {
        return this.keystoreConfiguration;
    }


    /**
     * @param keystoreConfiguration
     *            the keystoreConfiguration to set
     */
    @Override
    public void setKeystoreConfiguration ( KeystoresConfigMutable keystoreConfiguration ) {
        this.keystoreConfiguration = (KeystoresConfigImpl) keystoreConfiguration;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getStorageConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = StorageConfigurationImpl.class )
    public StorageConfigurationMutable getStorageConfiguration () {
        return this.storageConfiguration;
    }


    /**
     * @param storageConfiguration
     *            the storageConfiguration to set
     */
    @Override
    public void setStorageConfiguration ( StorageConfigurationMutable storageConfiguration ) {
        this.storageConfiguration = (StorageConfigurationImpl) storageConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getRealmsConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = RealmsConfigImpl.class )
    public RealmsConfigMutable getRealmsConfiguration () {
        return this.realmsConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfigurationMutable#setRealmsConfiguration(eu.agno3.orchestrator.config.realms.RealmsConfigMutable)
     */
    @Override
    public void setRealmsConfiguration ( RealmsConfigMutable realmsConfiguration ) {
        this.realmsConfiguration = (RealmsConfigImpl) realmsConfiguration;
    }


    /**
     * @return the mailingConfiguration
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = MailingConfigurationImpl.class )
    public MailingConfigurationMutable getMailingConfiguration () {
        return this.mailingConfiguration;
    }


    /**
     * @param mailingConfiguration
     *            the mailingConfiguration to set
     */
    @Override
    public void setMailingConfiguration ( MailingConfigurationMutable mailingConfiguration ) {
        this.mailingConfiguration = (MailingConfigurationImpl) mailingConfiguration;
    }

}
