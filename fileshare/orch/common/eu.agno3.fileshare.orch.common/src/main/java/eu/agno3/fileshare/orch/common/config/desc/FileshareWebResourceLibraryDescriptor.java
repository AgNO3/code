/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config.desc;


import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor;


/**
 * @author mbechler
 *
 */
@Component ( service = ResourceLibraryDescriptor.class )
public class FileshareWebResourceLibraryDescriptor implements ResourceLibraryDescriptor {

    /**
     * 
     */
    public static final String RESOURCE_LIBRARY_TYPE = "fs_web"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor#getLibraryType()
     */
    @Override
    public String getLibraryType () {
        return RESOURCE_LIBRARY_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return FileshareConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor#getEditorType()
     */
    @Override
    public String getEditorType () {
        return "webTheme"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor#haveDefaultsFor(java.lang.String)
     */
    @Override
    public boolean haveDefaultsFor ( String name ) {
        return false;
    }
}
