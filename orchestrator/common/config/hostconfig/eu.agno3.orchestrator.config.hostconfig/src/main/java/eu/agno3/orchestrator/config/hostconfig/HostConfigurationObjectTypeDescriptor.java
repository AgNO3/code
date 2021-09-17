/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.system.SystemConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.realms.RealmsConfigObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class HostConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<HostConfiguration, HostConfigurationImpl> {

    /**
     * 
     */
    public HostConfigurationObjectTypeDescriptor () {
        super(HostConfiguration.class, HostConfigurationImpl.class, HostConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull HostConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull HostConfiguration getGlobalDefaults () {
        return new HostConfigurationImpl();
    }


    /**
     * @return empty instance
     */
    public static @NonNull HostConfigurationImpl emptyInstance () {
        HostConfigurationImpl hc = new HostConfigurationImpl();
        hc.setHostIdentification(HostIdentificationObjectTypeDescriptor.emptyInstance());
        hc.setDateTimeConfiguration(DateTimeConfigurationObjectTypeDescriptor.emptyInstance());
        hc.setResolverConfiguration(ResolverConfigurationObjectTypeDescriptor.emptyInstance());
        hc.setNetworkConfiguration(NetworkConfigurationObjectTypeDescriptor.emptyInstance());
        hc.setTrustConfiguration(TruststoresConfigObjectTypeDescriptor.emptyInstance());
        hc.setKeystoreConfiguration(KeystoresConfigObjectTypeDescriptor.emptyInstance());
        hc.setStorageConfiguration(StorageConfigurationObjectTypeDescriptor.emptyInstance());
        hc.setSystemConfiguration(SystemConfigurationObjectTypeDescriptor.emptyInstance());
        hc.setRealmsConfiguration(RealmsConfigObjectTypeDescriptor.emptyInstance());
        hc.setMailingConfiguration(MailingConfigurationObjectTypeDescriptor.emptyInstance());
        return hc;
    }

}
