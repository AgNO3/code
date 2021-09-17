/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class SMTPConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<SMTPConfiguration, SMTPConfigurationImpl> {

    /**
     * Type name
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:web:smtp"; //$NON-NLS-1$


    /**
     * 
     */
    public SMTPConfigurationObjectTypeDescriptor () {
        super(SMTPConfiguration.class, SMTPConfigurationImpl.class, WebConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull SMTPConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull SMTPConfiguration getGlobalDefaults () {
        SMTPConfigurationImpl cfg = new SMTPConfigurationImpl();
        cfg.setSslClientMode(SSLClientMode.DISABLE);
        cfg.setSocketTimeout(Duration.standardSeconds(10));
        cfg.setAuthMechanisms(new HashSet<>(Arrays.asList(
            "LOGIN", //$NON-NLS-1$
            "PLAIN", //$NON-NLS-1$
            "DIGEST-MD5"))); //$NON-NLS-1$
        return cfg;
    }


    /**
     * @return empty instance
     */
    public static @NonNull SMTPConfigurationMutable emptyInstance () {
        SMTPConfigurationImpl ic = new SMTPConfigurationImpl();
        ic.setSslClientConfiguration(SSLClientConfigurationObjectTypeDescriptor.emptyInstance());
        return ic;
    }

}
