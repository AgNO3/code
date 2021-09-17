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
public class FileshareContentSearchConfigObjectTypeDescriptor extends
        AbstractObjectTypeDescriptor<FileshareContentSearchConfig, FileshareContentSearchConfigImpl> {

    /**
     * 
     */
    public FileshareContentSearchConfigObjectTypeDescriptor () {
        super(FileshareContentSearchConfig.class, FileshareContentSearchConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareContentConfigObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareContentSearchConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareContentSearchConfig getGlobalDefaults () {
        FileshareContentSearchConfigImpl csc = new FileshareContentSearchConfigImpl();
        csc.setSearchDisabled(false);
        csc.setSearchAllowPaging(true);
        csc.setSearchPageSize(20);
        return csc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareContentSearchConfigMutable emptyInstance () {
        return new FileshareContentSearchConfigImpl();
    }

}
