/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.terms;


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
public class TermsDefinitionObjectTypeDescriptor extends AbstractObjectTypeDescriptor<TermsDefinition, TermsDefinitionImpl> {

    /**
     * 
     */
    public TermsDefinitionObjectTypeDescriptor () {
        super(TermsDefinition.class, TermsDefinitionImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return TermsConfigurationObjectTypeDescriptor.OBJECT_TYPE;
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
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull TermsDefinition newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull TermsDefinition getGlobalDefaults () {
        TermsDefinitionImpl td = new TermsDefinitionImpl();
        td.setApplyType(TermsApplyType.ALL);
        td.setPriority(0);
        td.setPersistAcceptance(true);
        return td;
    }


    /**
     * @return empty instance
     */
    public static @NonNull TermsDefinitionMutable emptyInstance () {
        return new TermsDefinitionImpl();
    }

}
