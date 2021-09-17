/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class LDAPConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<LDAPConfiguration, LDAPConfigurationImpl> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:web:ldap"; //$NON-NLS-1$


    /**
     * 
     */
    public LDAPConfigurationObjectTypeDescriptor () {
        super(LDAPConfiguration.class, LDAPConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull LDAPConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull LDAPConfiguration getGlobalDefaults () {
        return globalDefaults();
    }


    /**
     * @return global default settings
     */
    public static @NonNull LDAPConfigurationMutable globalDefaults () {
        LDAPConfigurationImpl cfg = new LDAPConfigurationImpl();
        cfg.setSslClientMode(SSLClientMode.DISABLE);
        cfg.setAuthType(LDAPAuthType.ANONYMOUS);
        cfg.setServerType(LDAPServerType.PLAIN);
        cfg.setSaslQOP(SASLQOP.CONF);
        cfg.setSocketTimeout(Duration.standardSeconds(10));
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull LDAPConfigurationMutable emptyInstance () {
        LDAPConfigurationImpl ic = new LDAPConfigurationImpl();
        ic.setSslClientConfiguration(SSLClientConfigurationObjectTypeDescriptor.emptyInstance());
        return ic;
    }

}
