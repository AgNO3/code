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
public class WebEndpointConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<WebEndpointConfig, WebEndpointConfigImpl> {

    /**
     * 
     */
    public WebEndpointConfigObjectTypeDescriptor () {
        super(WebEndpointConfig.class, WebEndpointConfigImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull WebEndpointConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull WebEndpointConfig getGlobalDefaults () {
        return defaultInstance();
    }


    /**
     * @return a default instance
     */
    public static @NonNull WebEndpointConfigMutable defaultInstance () {
        WebEndpointConfigImpl cfg = new WebEndpointConfigImpl();
        cfg.setBehindReverseProxy(false);
        cfg.setBindPort(443);
        cfg.setDisableSSL(false);
        cfg.setEnableHPKP(false);
        cfg.setEnableHSTS(false);
        cfg.setHstsTimeout(Duration.standardDays(365));
        cfg.setHstsIncludeSubdomains(true);
        cfg.setSessionInactiveTimeout(Duration.standardHours(1));

        SSLEndpointConfigurationMutable sslConfig = SSLEndpointConfigurationObjectTypeDescriptor.emptyInstance();
        sslConfig.setKeyAlias("web"); //$NON-NLS-1$
        sslConfig.setKeystoreAlias("web"); //$NON-NLS-1$
        sslConfig.setSecurityMode(SSLSecurityMode.DEFAULT);
        cfg.setSslEndpointConfiguration(sslConfig);
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull WebEndpointConfigMutable emptyInstance () {
        WebEndpointConfigImpl wec = new WebEndpointConfigImpl();
        wec.setSslEndpointConfiguration(SSLEndpointConfigurationObjectTypeDescriptor.emptyInstance());
        wec.setReverseProxyConfig(WebReverseProxyConfigurationObjectTypeDescriptor.emptyInstance());
        return wec;
    }

}
