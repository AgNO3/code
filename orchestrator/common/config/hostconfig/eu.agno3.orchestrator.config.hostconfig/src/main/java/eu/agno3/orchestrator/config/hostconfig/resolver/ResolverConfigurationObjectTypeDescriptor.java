/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.resolver;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class ResolverConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<ResolverConfiguration, ResolverConfigurationImpl> {

    /**
     * 
     */
    public ResolverConfigurationObjectTypeDescriptor () {
        super(
            ResolverConfiguration.class,
            ResolverConfigurationImpl.class,
            HostConfigurationMessages.BASE_PACKAGE,
            "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull ResolverConfiguration getGlobalDefaults () {
        ResolverConfigurationMutable defaults = emptyInstance();
        defaults.setAutoconfigureDns(false);
        return defaults;
    }


    /**
     * @return empty instance
     */
    public static ResolverConfigurationImpl emptyInstance () {
        return new ResolverConfigurationImpl();
    }
}
