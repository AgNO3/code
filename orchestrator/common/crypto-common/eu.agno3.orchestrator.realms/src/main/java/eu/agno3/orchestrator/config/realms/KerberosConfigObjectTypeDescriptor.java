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
public class KerberosConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<KerberosConfig, KerberosConfigImpl> {

    /**
     * 
     */
    public KerberosConfigObjectTypeDescriptor () {
        super(KerberosConfig.class, KerberosConfigImpl.class, RealmsConfigMessages.BASE, "urn:agno3:objects:1.0:realms:realms"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull KerberosConfig getGlobalDefaults () {
        KerberosConfigMutable def = emptyInstance();
        def.setDnsLookupKDC(true);
        def.setDnsLookupRealm(true);

        def.setAllowWeakCrypto(false);
        def.setDisableAddresses(true);
        def.setDefaultTGTRenewable(false);
        def.setDefaultTGTForwardable(false);
        def.setDefaultTGTRenewable(false);

        def.setMaxClockskew(Duration.standardSeconds(300));
        def.setKdcTimeout(Duration.standardSeconds(20));

        def.setMaxRetries(3);
        def.setUdpPreferenceLimit(1400);
        return def;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull KerberosConfig newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull KerberosConfigMutable emptyInstance () {
        return new KerberosConfigImpl();
    }

}
