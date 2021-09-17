/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectTypeDescriptor.class )
public class KRBRealmConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<KRBRealmConfig, KRBRealmConfigImpl> {

    /**
     * 
     */
    public KRBRealmConfigObjectTypeDescriptor () {
        super(KRBRealmConfig.class, KRBRealmConfigImpl.class, RealmsConfigMessages.BASE, "urn:agno3:objects:1.0:realms:realms"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return AbstractRealmConfigObjectTypeDescriptor.OBJECT_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull KRBRealmConfig getGlobalDefaults () {
        KRBRealmConfigImpl krc = new KRBRealmConfigImpl();
        krc.setSecurityLevel(KerberosSecurityLevel.HIGH);
        krc.setRekeyServices(false);
        krc.setMaximumTicketLifetime(Duration.standardDays(1));
        return krc;
    }
}
