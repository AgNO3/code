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
public class TermsConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<TermsConfiguration, TermsConfigurationImpl> {

    /**
     * 
     */
    public static final String OBJECT_TYPE = "urn:agno3:objects:1.0:terms"; //$NON-NLS-1$


    /**
     * 
     */
    public TermsConfigurationObjectTypeDescriptor () {
        super(TermsConfiguration.class, TermsConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull TermsConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull TermsConfiguration getGlobalDefaults () {
        TermsConfigurationImpl tc = new TermsConfigurationImpl();
        tc.setTermsLibrary("terms"); //$NON-NLS-1$
        return tc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull TermsConfigurationMutable emptyInstance () {
        return new TermsConfigurationImpl();
    }

}
