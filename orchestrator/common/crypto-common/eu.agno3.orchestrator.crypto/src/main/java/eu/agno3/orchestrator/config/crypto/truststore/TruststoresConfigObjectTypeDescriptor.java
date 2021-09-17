/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.crypto.i18n.CryptoConfigMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class TruststoresConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<TruststoresConfig, TruststoresConfigImpl> {

    /**
     * 
     */
    private static final String CLIENT_TRUSTSTORE = "global"; //$NON-NLS-1$
    private static final String CLIENT_TRUSTSTORE_ALIAS = "client"; //$NON-NLS-1$
    private static final UUID CLIENT_TRUSTSTORE_ID = UUID.fromString("5a42cac2-3d33-4699-853f-5247c8a14b5d"); //$NON-NLS-1$

    private static final String INTERNAL_TRUSTSTORE = "internal"; //$NON-NLS-1$
    private static final UUID INTERNAL_TRUSTSTORE_ID = UUID.fromString("75a8d944-3a62-4056-9910-4304769c8418"); //$NON-NLS-1$


    /**
     * 
     */
    public TruststoresConfigObjectTypeDescriptor () {
        super(TruststoresConfig.class, TruststoresConfigImpl.class, CryptoConfigMessages.BASE, "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull TruststoresConfig getGlobalDefaults () {
        TruststoresConfigImpl def = new TruststoresConfigImpl();
        TruststoreConfigImpl client = TruststoreConfigObjectTypeDescriptor.emptyInstance();
        client.setId(CLIENT_TRUSTSTORE_ID);
        client.setTrustLibrary(CLIENT_TRUSTSTORE);
        client.setAlias(CLIENT_TRUSTSTORE_ALIAS);

        TruststoreConfigImpl internal = TruststoreConfigObjectTypeDescriptor.emptyInstance();
        internal.setId(INTERNAL_TRUSTSTORE_ID);
        internal.setTrustLibrary(INTERNAL_TRUSTSTORE);
        internal.setAlias(INTERNAL_TRUSTSTORE);

        def.getTruststores().add(client);
        def.getTruststores().add(internal);

        return def;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull TruststoresConfig newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull TruststoresConfigMutable emptyInstance () {
        return new TruststoresConfigImpl();
    }
}
