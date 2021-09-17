/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class SSLClientConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<SSLClientConfiguration, SSLClientConfigurationImpl> {

    /**
     * 
     */
    public SSLClientConfigurationObjectTypeDescriptor () {
        super(SSLClientConfiguration.class, SSLClientConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull SSLClientConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull SSLClientConfiguration getGlobalDefaults () {
        SSLClientConfigurationImpl cfg = new SSLClientConfigurationImpl();
        cfg.setSecurityMode(SSLSecurityMode.DEFAULT);
        cfg.setTruststoreAlias("client"); //$NON-NLS-1$
        cfg.setDisableHostnameVerification(false);
        cfg.setDisableCertificateVerification(false);
        cfg.setPublicKeyPinMode(PublicKeyPinMode.ADDITIVE);
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull SSLClientConfigurationMutable emptyInstance () {
        return new SSLClientConfigurationImpl();
    }

}
