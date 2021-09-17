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
public class RuntimeConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<RuntimeConfiguration, RuntimeConfigurationImpl> {

    /**
     * 
     */
    public RuntimeConfigurationObjectTypeDescriptor () {
        super(RuntimeConfiguration.class, RuntimeConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull RuntimeConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#isHidden()
     */
    @Override
    public boolean isHidden () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull RuntimeConfiguration getGlobalDefaults () {
        RuntimeConfigurationImpl rtc = new RuntimeConfigurationImpl();
        rtc.setAutoMemoryLimit(true);
        return rtc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull RuntimeConfigurationImpl emptyInstance () {
        return new RuntimeConfigurationImpl();
    }

}
