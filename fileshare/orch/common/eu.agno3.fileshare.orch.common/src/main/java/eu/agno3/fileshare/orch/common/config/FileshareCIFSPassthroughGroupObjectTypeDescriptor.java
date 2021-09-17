/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.hostconfig.storage.CIFSAuthType;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareCIFSPassthroughGroupObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<FileshareCIFSPassthroughGroup, FileshareCIFSPassthroughGroupImpl> {

    /**
     * 
     */
    public FileshareCIFSPassthroughGroupObjectTypeDescriptor () {
        super(FileshareCIFSPassthroughGroup.class, FileshareCIFSPassthroughGroupImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FilesharePassthroughGroupObjectTypeDescriptor.OBJECT_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareCIFSPassthroughGroup newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareCIFSPassthroughGroup getGlobalDefaults () {
        return globalDefaults();
    }


    /**
     * @return global defaults
     */
    public static @NonNull FileshareCIFSPassthroughGroup globalDefaults () {
        FileshareCIFSPassthroughGroupImpl ac = new FileshareCIFSPassthroughGroupImpl();
        ac.setSecurityPolicy("UNCLASSIFIED"); //$NON-NLS-1$
        ac.setAllowSharing(true);
        ac.setAuthType(CIFSAuthType.NTLMSSP);
        ac.setEnableSigning(false);
        ac.setAllowSMB1(false);
        ac.setDisableSMB2(false);
        return ac;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareCIFSPassthroughGroup emptyInstance () {
        return new FileshareCIFSPassthroughGroupImpl();
    }

}
