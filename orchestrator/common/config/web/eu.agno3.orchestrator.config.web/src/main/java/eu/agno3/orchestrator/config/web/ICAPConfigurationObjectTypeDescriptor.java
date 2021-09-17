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
public class ICAPConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<ICAPConfiguration, ICAPConfigurationImpl> {

    /**
     * 
     */
    public ICAPConfigurationObjectTypeDescriptor () {
        super(ICAPConfiguration.class, ICAPConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull ICAPConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull ICAPConfiguration getGlobalDefaults () {
        ICAPConfigurationMutable cfg = new ICAPConfigurationImpl();
        cfg.setSslClientMode(SSLClientMode.DISABLE);
        cfg.setSendICAPSInRequestUri(false);
        cfg.setSocketTimeout(Duration.standardSeconds(5));
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull ICAPConfigurationMutable emptyInstance () {
        ICAPConfigurationMutable ic = new ICAPConfigurationImpl();
        ic.setSslClientConfiguration(SSLClientConfigurationObjectTypeDescriptor.emptyInstance());
        return ic;
    }

}
