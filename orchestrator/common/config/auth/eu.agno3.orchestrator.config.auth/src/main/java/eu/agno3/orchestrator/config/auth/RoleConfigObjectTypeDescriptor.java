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
public class RoleConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<RoleConfig, RoleConfigImpl> {

    /**
     * 
     */
    public RoleConfigObjectTypeDescriptor () {
        super(RoleConfig.class, RoleConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return StaticRolesConfigObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull RoleConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull RoleConfig getGlobalDefaults () {
        RoleConfigImpl rc = new RoleConfigImpl();
        rc.setHidden(false);
        return rc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull RoleConfig emptyInstance () {
        return new RoleConfigImpl();
    }

}
