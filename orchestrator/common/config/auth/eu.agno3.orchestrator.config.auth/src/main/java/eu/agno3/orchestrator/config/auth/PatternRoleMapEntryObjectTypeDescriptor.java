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
public class PatternRoleMapEntryObjectTypeDescriptor extends AbstractObjectTypeDescriptor<PatternRoleMapEntry, PatternRoleMapEntryImpl> {

    /**
     * 
     */
    public PatternRoleMapEntryObjectTypeDescriptor () {
        super(PatternRoleMapEntry.class, PatternRoleMapEntryImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
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
        return AuthenticatorsConfigObjectTypeDescriptor.OBJECT_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull PatternRoleMapEntry newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull PatternRoleMapEntry getGlobalDefaults () {
        return new PatternRoleMapEntryImpl();
    }


    /**
     * @return empty instance
     */
    public static @NonNull PatternRoleMapEntry emptyInstance () {
        return new PatternRoleMapEntryImpl();
    }

}
