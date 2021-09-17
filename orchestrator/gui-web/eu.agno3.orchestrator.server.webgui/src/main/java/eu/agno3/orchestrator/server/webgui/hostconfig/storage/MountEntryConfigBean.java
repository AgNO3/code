/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.storage;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.hostconfig.storage.CIFSAuthType;
import eu.agno3.orchestrator.config.hostconfig.storage.MountEntry;
import eu.agno3.orchestrator.config.hostconfig.storage.MountEntryImpl;
import eu.agno3.orchestrator.config.hostconfig.storage.MountType;
import eu.agno3.orchestrator.config.hostconfig.storage.NFSSecurityType;
import eu.agno3.orchestrator.config.hostconfig.storage.NFSVersion;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean;


/**
 * @author mbechler
 *
 */
@Named ( "mountEntryBean" )
@ApplicationScoped
public class MountEntryConfigBean extends AbstractBaseConfigObjectBean<MountEntry, MountEntryImpl<MountEntry>> {

    @Override
    protected Class<MountEntry> getObjectType () {
        return MountEntry.class;
    }


    @Override
    protected String getMessageBase () {
        return HostConfigurationMessages.BASE_PACKAGE;
    }


    public Comparator<MountEntry> getComparator () {
        return new MountEntryComparator();
    }


    @Override
    public String labelForInternal ( MountEntry me ) {
        return me.getMountType() + ": " + ( me.getAlias() != null ? me.getAlias() : GuiMessages.get(GuiMessages.UNNAMED_CONFIG_OBJECT) ); //$NON-NLS-1$
    }


    public NFSVersion[] getNfsVersions () {
        return NFSVersion.values();
    }


    public String translateNfsVersion ( Object val ) {
        return translateEnumValue(NFSVersion.class, val);
    }


    public NFSSecurityType[] getNfsSecurityTypes () {
        return new NFSSecurityType[] {
            NFSSecurityType.NONE
        };
    }


    public String translateNfsSecurityType ( Object val ) {
        return translateEnumValue(NFSSecurityType.class, val);
    }


    public CIFSAuthType[] getCifsAuthTypes () {
        return CIFSAuthType.values();
    }


    public String translateCifsAuthType ( Object val ) {
        return translateEnumValue(CIFSAuthType.class, val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( MountEntryImpl<MountEntry> cloned, MountEntry obj ) {
        cloned.setAlias(obj.getAlias());
        cloned.clone(obj);
    }


    public String translateMountType ( Object type ) {
        return translateEnumValue(MountType.class, type);
    }


    public List<MountType> getMountTypes () {
        return Arrays.asList(MountType.values());
    }

}
