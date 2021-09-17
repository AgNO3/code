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
public class ADRealmConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<ADRealmConfig, ADRealmConfigImpl> {

    /**
     * 
     */
    public ADRealmConfigObjectTypeDescriptor () {
        super(ADRealmConfig.class, ADRealmConfigImpl.class, RealmsConfigMessages.BASE, "urn:agno3:objects:1.0:realms:realms"); //$NON-NLS-1$
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
    public @NonNull ADRealmConfig getGlobalDefaults () {
        ADRealmConfigMutable adrc = new ADRealmConfigImpl();
        adrc.setSecurityLevel(KerberosSecurityLevel.LEGACY); // AD by default does not provide AES but only RC4
        adrc.setRekeyMachineAccount(true);
        adrc.setMachineRekeyInterval(Duration.standardDays(30));
        adrc.setRekeyServices(false);
        adrc.setMaximumTicketLifetime(Duration.standardHours(10));
        adrc.setUpdateDns(true);
        adrc.setUpdateDnsForceSecure(false);
        adrc.setUpdateDnsTtl(Duration.standardMinutes(15)); // that is the windows default

        adrc.setJoinType(ADJoinType.ADMIN);
        adrc.setDoJoin(true); // join on first apply
        adrc.setDoRekey(false);
        adrc.setDoLeave(false);

        adrc.setAllowSMB1(false);
        adrc.setDisableSMB2(false);
        return adrc;
    }
}
