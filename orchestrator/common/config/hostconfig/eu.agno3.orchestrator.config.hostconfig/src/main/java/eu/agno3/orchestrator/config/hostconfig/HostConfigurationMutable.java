/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfigMutable;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfigMutable;
import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.system.SystemConfigurationMutable;
import eu.agno3.orchestrator.config.realms.RealmsConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( HostConfiguration.class )
public interface HostConfigurationMutable extends HostConfiguration {

    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getHostIdentification()
     */
    @Override
    HostIdentificationMutable getHostIdentification ();


    /**
     * @param hostIdentification
     */
    void setHostIdentification ( HostIdentificationMutable hostIdentification );


    /**
     * @param systemConfiguration
     */
    void setSystemConfiguration ( SystemConfigurationMutable systemConfiguration );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getSystemConfiguration()
     */
    @Override
    SystemConfigurationMutable getSystemConfiguration ();


    /**
     * @param dateTimeConfiguration
     */
    void setDateTimeConfiguration ( DateTimeConfigurationMutable dateTimeConfiguration );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getDateTimeConfiguration()
     */
    @Override
    DateTimeConfigurationMutable getDateTimeConfiguration ();


    /**
     * @param networkConfiguration
     */
    void setNetworkConfiguration ( NetworkConfigurationMutable networkConfiguration );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getNetworkConfiguration()
     */
    @Override
    NetworkConfigurationMutable getNetworkConfiguration ();


    /**
     * @param resolverConfiguration
     */
    void setResolverConfiguration ( ResolverConfigurationMutable resolverConfiguration );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getResolverConfiguration()
     */
    @Override
    ResolverConfigurationMutable getResolverConfiguration ();


    /**
     * @param storageConfiguration
     */
    void setStorageConfiguration ( StorageConfigurationMutable storageConfiguration );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getStorageConfiguration()
     */
    @Override
    StorageConfigurationMutable getStorageConfiguration ();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getKeystoreConfiguration()
     */
    @Override
    KeystoresConfigMutable getKeystoreConfiguration ();


    /**
     * @param keystoreConfiguration
     */
    void setKeystoreConfiguration ( KeystoresConfigMutable keystoreConfiguration );


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getTrustConfiguration()
     */
    @Override
    TruststoresConfigMutable getTrustConfiguration ();


    /**
     * @param trustConfiguration
     */
    void setTrustConfiguration ( TruststoresConfigMutable trustConfiguration );


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getRealmsConfiguration()
     */
    @Override
    RealmsConfigMutable getRealmsConfiguration ();


    /**
     * @param realmsConfiguration
     */
    void setRealmsConfiguration ( RealmsConfigMutable realmsConfiguration );


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.HostConfiguration#getMailingConfiguration()
     */
    @Override
    public MailingConfigurationMutable getMailingConfiguration ();


    /**
     * @param mailingConfiguration
     */
    void setMailingConfiguration ( MailingConfigurationMutable mailingConfiguration );

}
