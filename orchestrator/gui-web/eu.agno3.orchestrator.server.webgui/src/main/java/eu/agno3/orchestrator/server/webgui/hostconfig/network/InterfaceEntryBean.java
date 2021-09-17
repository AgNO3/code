/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.network;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV4;
import eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV6;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryImpl;
import eu.agno3.orchestrator.config.hostconfig.network.MediaType;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.EmptyCompleter;
import eu.agno3.orchestrator.server.webgui.util.i18n.I18NUtil;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "interfaceEntryBean" )
public class InterfaceEntryBean extends AbstractConfigObjectBean<InterfaceEntry, InterfaceEntryImpl> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return HostConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<InterfaceEntry> getObjectType () {
        return InterfaceEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<InterfaceEntryImpl> getInstanceType () {
        return InterfaceEntryImpl.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, InterfaceEntryImpl cloned, InterfaceEntry obj, InterfaceEntry def ) {
        cloned.setAlias(obj.getAlias());
        super.cloneDefault(ctx, cloned, obj, def);
    }


    @Override
    public String labelForInternal ( InterfaceEntry ife ) {
        ResourceBundle hcResourceBundle = this.getLocalizationBundle();

        if ( ife.getAlias() == null ) {
            return hcResourceBundle.getString("interface.label.noAlias"); //$NON-NLS-1$
        }

        if ( ife.getInterfaceIndex() != null && ife.getHardwareAddress() != null ) {
            return labelWithIndexAndAddr(ife, hcResourceBundle);
        }
        else if ( ife.getInterfaceIndex() != null ) {
            return labelWithIndex(ife, hcResourceBundle);
        }
        else if ( ife.getHardwareAddress() != null ) {
            return labelWithAddress(ife, hcResourceBundle);
        }
        return labelWithoutAttrs(ife, hcResourceBundle);
    }


    /**
     * @param ife
     * @param hcResourceBundle
     * @return
     */
    private static String labelWithoutAttrs ( InterfaceEntry ife, ResourceBundle hcResourceBundle ) {
        return I18NUtil.format(hcResourceBundle, "interface.label.noMatchFmt", ife.getAlias()); //$NON-NLS-1$
    }


    /**
     * @param ife
     * @param hcResourceBundle
     * @return
     */
    private static String labelWithAddress ( InterfaceEntry ife, ResourceBundle hcResourceBundle ) {
        return I18NUtil.format(
            hcResourceBundle,
            "interface.label.matchHwAddrFmt", //$NON-NLS-1$
            ife.getAlias(),
            ife.getHardwareAddress());
    }


    /**
     * @param ife
     * @param hcResourceBundle
     * @return
     */
    private static String labelWithIndex ( InterfaceEntry ife, ResourceBundle hcResourceBundle ) {
        return I18NUtil.format(
            hcResourceBundle,
            "interface.label.matchIndexFmt", //$NON-NLS-1$
            ife.getAlias(),
            ife.getInterfaceIndex());
    }


    /**
     * @param ife
     * @param hcResourceBundle
     * @return
     */
    private static String labelWithIndexAndAddr ( InterfaceEntry ife, ResourceBundle hcResourceBundle ) {
        return I18NUtil.format(
            hcResourceBundle,
            "interface.label.matchBothFmt", //$NON-NLS-1$
            ife.getAlias(),
            ife.getInterfaceIndex(),
            ife.getHardwareAddress());
    }


    public NetworkSpecification makeStaticAddress () {
        return new NetworkSpecification();
    }


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getInterfaceCompleter ( OuterWrapper<?> outer )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        AbstractObjectEditor<?> interfaceConfig = outer.resolve(
            "urn:agno3:objects:1.0:hostconfig:network", //$NON-NLS-1$
            "interfaceConfiguration/interfaces");//$NON-NLS-1$

        if ( interfaceConfig == null ) {
            Object parameter = outer.getEditor().getParameter("interfaceConfig"); //$NON-NLS-1$
            if ( ! ( parameter instanceof InterfaceConfiguration ) ) {
                return new EmptyCompleter();
            }
            InterfaceConfiguration cfg = (InterfaceConfiguration) parameter;
            return makeCompleter(new ArrayList<>(cfg.getInterfaces()));
        }

        final List<InterfaceEntry> interfaces = (List<InterfaceEntry>) interfaceConfig.getEffective();

        return makeCompleter(interfaces);
    }


    /**
     * @param interfaces
     * @return
     */
    Completer<String> makeCompleter ( final List<InterfaceEntry> interfaces ) {
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( InterfaceEntry e : interfaces ) {
                    String alias = e.getAlias();
                    if ( alias != null && !alias.isEmpty() && alias.startsWith(query) ) {
                        res.add(alias);
                    }
                }
                return res;
            }
        };
    }


    public Comparator<InterfaceEntry> getComparator () {
        return new InterfaceEntryComparator();
    }


    public Comparator<NetworkSpecification> getStaticAddressComparator () {
        return new StaticAddressComparator();
    }


    public List<AddressConfigurationTypeV4> getAddressConfigurationTypesV4 () {
        return Arrays.asList(AddressConfigurationTypeV4.values());
    }


    public String translateAddressConfigurationTypeV4 ( Object type ) {
        return translateEnumValue(AddressConfigurationTypeV4.class, type);
    }


    public List<AddressConfigurationTypeV6> getAddressConfigurationTypesV6 () {
        return Arrays.asList(AddressConfigurationTypeV6.values());
    }


    public String translateAddressConfigurationTypeV6 ( Object type ) {
        return translateEnumValue(AddressConfigurationTypeV6.class, type);
    }


    public List<MediaType> getMediaTypes () {
        return Arrays.asList(MediaType.values());
    }


    public String translateMediaType ( Object type ) {
        return translateEnumValue(MediaType.class, type);
    }


    public boolean shouldShowStaticAddresses ( InterfaceEntry current, InterfaceEntry defaults, InterfaceEntry enforced ) {

        if ( enforced != null && ( hasStaticConfiguration(enforced) || hasStaticAddresses(enforced) ) ) {
            return true;
        }

        if ( current != null && ( hasStaticConfiguration(current) || hasStaticAddresses(current) ) ) {
            return true;
        }

        if ( hasInheritedStaticAddressConfig(current, defaults) ) {
            return true;
        }

        return false;
    }


    /**
     * @param current
     * @param defaults
     * @return
     */
    private static boolean hasInheritedStaticAddressConfig ( InterfaceEntry current, InterfaceEntry defaults ) {
        if ( defaults != null && ( current != null && current.getV4AddressConfigurationType() == null )
                && defaults.getV4AddressConfigurationType() == AddressConfigurationTypeV4.STATIC ) {
            return true;
        }

        if ( defaults != null && ( current != null && current.getV6AddressConfigurationType() == null )
                && defaults.getV6AddressConfigurationType() == AddressConfigurationTypeV6.STATIC ) {
            return true;
        }

        return false;
    }


    /**
     * @param current
     * @return
     */
    private static boolean hasStaticAddresses ( InterfaceEntry entry ) {
        return entry != null && ( entry.getStaticAddresses() != null && !entry.getStaticAddresses().isEmpty() );
    }


    private static boolean hasStaticConfiguration ( InterfaceEntry enforced ) {
        return enforced.getV4AddressConfigurationType() == AddressConfigurationTypeV4.STATIC
                || enforced.getV6AddressConfigurationType() == AddressConfigurationTypeV6.STATIC;
    }

}
