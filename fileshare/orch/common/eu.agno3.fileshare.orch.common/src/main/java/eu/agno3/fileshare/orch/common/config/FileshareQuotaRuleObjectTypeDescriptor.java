/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareQuotaRuleObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareQuotaRule, FileshareQuotaRuleImpl> {

    /**
     * 
     */
    public FileshareQuotaRuleObjectTypeDescriptor () {
        super(FileshareQuotaRule.class, FileshareQuotaRuleImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
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
        return FileshareConfigurationObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareQuotaRule newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareQuotaRule getGlobalDefaults () {
        return new FileshareQuotaRuleImpl();
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareQuotaRuleMutable emptyInstance () {
        return new FileshareQuotaRuleImpl();
    }

}
