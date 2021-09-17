/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class CIFSMountEntryObjectTypeDescriptor extends AbstractObjectTypeDescriptor<CIFSMountEntry, CIFSMountEntryImpl> {

    /**
     * 
     */
    public CIFSMountEntryObjectTypeDescriptor () {
        super(CIFSMountEntry.class, CIFSMountEntryImpl.class, HostConfigurationMessages.BASE_PACKAGE, MountEntryObjectTypeDescriptor.TYPE_NAME);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull CIFSMountEntry getGlobalDefaults () {
        CIFSMountEntryMutable ci = new CIFSMountEntryImpl();
        ci.setAuthType(CIFSAuthType.NTLMSSP);
        ci.setEnableSigning(false);
        ci.setAllowSMB1(false);
        ci.setDisableSMB2(false);
        return ci;
    }
}
