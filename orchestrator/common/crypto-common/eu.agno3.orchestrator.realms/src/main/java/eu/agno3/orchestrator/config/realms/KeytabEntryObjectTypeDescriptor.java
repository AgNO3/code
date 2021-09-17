/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectTypeDescriptor.class )
public class KeytabEntryObjectTypeDescriptor extends AbstractObjectTypeDescriptor<KeytabEntry, KeytabEntryImpl> {

    /**
     * 
     */
    public KeytabEntryObjectTypeDescriptor () {
        super(KeytabEntry.class, KeytabEntryImpl.class, RealmsConfigMessages.BASE, "urn:agno3:objects:1.0:realms:realms"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull KeytabEntry getGlobalDefaults () {
        return emptyInstance();

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull KeytabEntry newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull KeytabEntryMutable emptyInstance () {
        return new KeytabEntryImpl();
    }

}
