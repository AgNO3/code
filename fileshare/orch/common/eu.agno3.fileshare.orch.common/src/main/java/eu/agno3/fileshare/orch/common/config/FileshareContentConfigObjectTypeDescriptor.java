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
public class FileshareContentConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareContentConfig, FileshareContentConfigImpl> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:fileshare:content"; //$NON-NLS-1$


    /**
     * 
     */
    public FileshareContentConfigObjectTypeDescriptor () {
        super(FileshareContentConfig.class, FileshareContentConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull FileshareContentConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareContentConfig getGlobalDefaults () {
        FileshareContentConfigImpl fcc = new FileshareContentConfigImpl();
        fcc.setFallbackMimeType("application/octet-stream"); //$NON-NLS-1$
        fcc.setAllowMimeTypeChanges(false);
        fcc.setUseUserSuppliedTypeInfo(false);
        return fcc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareContentConfigMutable emptyInstance () {
        FileshareContentConfigImpl cc = new FileshareContentConfigImpl();
        cc.setSearchConfig(FileshareContentSearchConfigObjectTypeDescriptor.emptyInstance());
        cc.setPreviewConfig(FileshareContentPreviewConfigObjectTypeDescriptor.emptyInstance());
        cc.setScanConfig(FileshareContentScanConfigObjectTypeDescriptor.emptyInstance());
        return cc;
    }

}
