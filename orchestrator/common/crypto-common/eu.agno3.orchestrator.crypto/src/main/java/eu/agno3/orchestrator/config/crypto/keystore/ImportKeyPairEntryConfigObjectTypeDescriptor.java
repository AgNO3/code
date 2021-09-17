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
public class ImportKeyPairEntryConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<ImportKeyPairEntry, ImportKeyPairEntryImpl> {

    /**
     * 
     */
    public ImportKeyPairEntryConfigObjectTypeDescriptor () {
        super(ImportKeyPairEntry.class, ImportKeyPairEntryImpl.class, CryptoConfigMessages.BASE, "urn:agno3:objects:1.0:crypto:keystores:keystore"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull ImportKeyPairEntry getGlobalDefaults () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull ImportKeyPairEntry newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull ImportKeyPairEntryMutable emptyInstance () {
        return new ImportKeyPairEntryImpl();
    }
}
