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
import eu.agno3.fileshare.service.config.ViewPolicyDefaults;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareContentPreviewConfigObjectTypeDescriptor extends
        AbstractObjectTypeDescriptor<FileshareContentPreviewConfig, FileshareContentPreviewConfigImpl> {

    /**
     * 
     */
    public FileshareContentPreviewConfigObjectTypeDescriptor () {
        super(FileshareContentPreviewConfig.class, FileshareContentPreviewConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull FileshareContentPreviewConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareContentPreviewConfig getGlobalDefaults () {
        FileshareContentPreviewConfigImpl fcp = new FileshareContentPreviewConfigImpl();
        fcp.setLimitPreviewFileSize(false);
        fcp.setPreviewMimeTypes(ViewPolicyDefaults.DEFAULT_VIEWABLE_MIME_TYPES);
        fcp.setPreviewSafeMimeTypes(ViewPolicyDefaults.DEFAULT_SAFE_MIME_TYPES);
        fcp.setPreviewNoSandboxMimeTypes(ViewPolicyDefaults.DEFAULT_NO_SANDBOX_MIME_TYPES);
        fcp.setPreviewRelaxedCSPMimeTypes(ViewPolicyDefaults.DEFAULT_RELAXED_CSP_MIME_TYPES);
        return fcp;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareContentPreviewConfigMutable emptyInstance () {
        return new FileshareContentPreviewConfigImpl();
    }

}
