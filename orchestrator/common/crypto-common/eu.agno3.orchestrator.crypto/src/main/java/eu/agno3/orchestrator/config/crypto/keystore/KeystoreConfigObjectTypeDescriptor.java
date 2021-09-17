/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


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
public class KeystoreConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<KeystoreConfig, KeystoreConfigImpl> {

    /**
     * 
     */
    public KeystoreConfigObjectTypeDescriptor () {
        super(KeystoreConfig.class, KeystoreConfigImpl.class, CryptoConfigMessages.BASE, "urn:agno3:objects:1.0:crypto:keystores"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull KeystoreConfig getGlobalDefaults () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull KeystoreConfig newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull KeystoreConfigMutable emptyInstance () {
        return new KeystoreConfigImpl();
    }
}
