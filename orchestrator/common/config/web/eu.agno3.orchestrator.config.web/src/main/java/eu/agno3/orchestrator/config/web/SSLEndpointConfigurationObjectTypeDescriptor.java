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
public class SSLEndpointConfigurationObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<SSLEndpointConfiguration, SSLEndpointConfigurationImpl> {

    /**
     * 
     */
    public SSLEndpointConfigurationObjectTypeDescriptor () {
        super(SSLEndpointConfiguration.class, SSLEndpointConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull SSLEndpointConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull SSLEndpointConfiguration getGlobalDefaults () {
        SSLEndpointConfigurationImpl cfg = new SSLEndpointConfigurationImpl();
        cfg.setSecurityMode(SSLSecurityMode.DEFAULT);
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull SSLEndpointConfigurationMutable emptyInstance () {
        return new SSLEndpointConfigurationImpl();
    }

}
