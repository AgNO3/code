/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class StaticRolesConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<StaticRolesConfig, StaticRolesConfigImpl> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:auth:roles"; //$NON-NLS-1$


    /**
     * 
     */
    public StaticRolesConfigObjectTypeDescriptor () {
        super(StaticRolesConfig.class, StaticRolesConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return AuthenticatorsConfigObjectTypeDescriptor.OBJECT_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull StaticRolesConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull StaticRolesConfig getGlobalDefaults () {
        return new StaticRolesConfigImpl();
    }


    /**
     * @return empty instance
     */
    public static @NonNull StaticRolesConfigMutable emptyInstance () {
        return new StaticRolesConfigImpl();
    }

}
