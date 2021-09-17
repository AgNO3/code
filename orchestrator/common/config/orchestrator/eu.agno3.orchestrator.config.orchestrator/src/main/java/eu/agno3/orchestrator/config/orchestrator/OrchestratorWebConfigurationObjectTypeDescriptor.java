/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.i18n.OrchestratorConfigurationMessages;
import eu.agno3.orchestrator.config.web.WebEndpointConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.types.net.IPv4Address;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class OrchestratorWebConfigurationObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<OrchestratorWebConfiguration, OrchestratorWebConfigurationImpl> {

    /**
     * 
     */
    public OrchestratorWebConfigurationObjectTypeDescriptor () {
        super(OrchestratorWebConfiguration.class, OrchestratorWebConfigurationImpl.class, OrchestratorConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull OrchestratorWebConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull OrchestratorWebConfiguration getGlobalDefaults () {
        OrchestratorWebConfigurationImpl cfg = new OrchestratorWebConfigurationImpl();
        cfg.setWebEndpointConfig(WebEndpointConfigObjectTypeDescriptor.defaultInstance());
        cfg.getWebEndpointConfig().setContextPath(null); // $NON-NLS-1$
        cfg.getWebEndpointConfig().setBindPort(8443);
        cfg.setApiEndpointConfig(WebEndpointConfigObjectTypeDescriptor.defaultInstance());
        cfg.getApiEndpointConfig().setBindAddresses(Collections.singleton(IPv4Address.parseV4Address("127.0.0.1"))); //$NON-NLS-1$
        cfg.getApiEndpointConfig().setBindPort(8444);
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull OrchestratorWebConfigurationMutable emptyInstance () {
        OrchestratorWebConfigurationImpl oc = new OrchestratorWebConfigurationImpl();
        oc.setApiEndpointConfig(WebEndpointConfigObjectTypeDescriptor.emptyInstance());
        oc.setWebEndpointConfig(WebEndpointConfigObjectTypeDescriptor.emptyInstance());
        return oc;
    }
}
