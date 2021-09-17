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
public class WebReverseProxyConfigurationObjectTypeDescriptor extends
        AbstractObjectTypeDescriptor<WebReverseProxyConfiguration, WebReverseProxyConfigurationImpl> {

    /**
     * 
     */
    public WebReverseProxyConfigurationObjectTypeDescriptor () {
        super(WebReverseProxyConfiguration.class, WebReverseProxyConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull WebReverseProxyConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull WebReverseProxyConfiguration getGlobalDefaults () {
        WebReverseProxyConfigurationMutable wrc = new WebReverseProxyConfigurationImpl();
        wrc.setProxyType(WebReverseProxyType.NONE);
        wrc.setForwardedSSLCiphersType(WebReverseProxySSLType.NONE);
        wrc.setForwardedSSLMatchValue("https"); //$NON-NLS-1$
        return wrc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull WebReverseProxyConfigurationMutable emptyInstance () {
        WebReverseProxyConfigurationMutable wrc = new WebReverseProxyConfigurationImpl();

        return wrc;
    }

}
