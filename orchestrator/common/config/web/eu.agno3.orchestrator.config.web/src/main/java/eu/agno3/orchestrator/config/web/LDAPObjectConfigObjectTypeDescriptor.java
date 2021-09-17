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
public class LDAPObjectConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<LDAPObjectConfig, LDAPObjectConfigImpl> {

    /**
     * 
     */
    public LDAPObjectConfigObjectTypeDescriptor () {
        super(LDAPObjectConfig.class, LDAPObjectConfigImpl.class, WebConfigurationMessages.BASE_PACKAGE);
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
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return LDAPConfigurationObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull LDAPObjectConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull LDAPObjectConfig getGlobalDefaults () {
        LDAPObjectConfigImpl loc = new LDAPObjectConfigImpl();
        loc.setScope(LDAPSearchScope.SUB);
        return loc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull LDAPObjectConfigMutable emptyInstance () {
        return new LDAPObjectConfigImpl();
    }

}
