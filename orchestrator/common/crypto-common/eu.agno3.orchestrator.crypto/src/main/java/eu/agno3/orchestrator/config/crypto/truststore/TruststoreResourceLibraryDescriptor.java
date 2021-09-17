/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.crypto.i18n.CryptoConfigMessages;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor;


/**
 * @author mbechler
 *
 */
@Component ( service = ResourceLibraryDescriptor.class )
public class TruststoreResourceLibraryDescriptor implements ResourceLibraryDescriptor {

    /**
     * 
     */
    public static final @NonNull String RESOURCE_LIBRARY_TYPE = "truststore"; //$NON-NLS-1$


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
        return CryptoConfigMessages.BASE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor#getEditorType()
     */
    @Override
    public String getEditorType () {
        return RESOURCE_LIBRARY_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryDescriptor#haveDefaultsFor(java.lang.String)
     */
    @Override
    public boolean haveDefaultsFor ( String name ) {
        if ( "global".equals(name) ) { //$NON-NLS-1$
            return true;
        }
        return false;
    }
}
