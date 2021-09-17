/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.network;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.instance.sysinfo.AgentSysInfoContextBean;
import eu.agno3.orchestrator.system.info.network.InterfaceType;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInterface;
import eu.agno3.orchestrator.types.net.HardwareAddress;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "networkInterfaceAddBean" )
public class InterfaceAddBean implements Serializable {

    private static final Logger log = Logger.getLogger(InterfaceAddBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -4213875768982692956L;

    private String selected;

    @Inject
    private AgentSysInfoContextBean sysInfoContext;

    private transient SelectItem[] cachedSelectItems;


    /**
     * @return the selected
     */
    public String getSelected () {
        return this.selected;
    }


    /**
     * @param selected
     *            the selected to set
     */
    public void setSelected ( String selected ) {
        this.selected = selected;
    }


    public SelectItem[] getSelectItems ( Object existingInterfaces ) {
        Set<Integer> existingIndices = new HashSet<>();
        Set<HardwareAddress> existingAddresses = new HashSet<>();

        if ( existingInterfaces instanceof Collection ) {
            Collection<?> existingNetworkInterfaces = (Collection<?>) existingInterfaces;
            for ( Object o : existingNetworkInterfaces ) {
                if ( o instanceof InterfaceEntry ) {
                    InterfaceEntry ie = (InterfaceEntry) o;
                    if ( ie.getInterfaceIndex() != null ) {
                        existingIndices.add(ie.getInterfaceIndex());
                    }
                    if ( ie.getHardwareAddress() != null ) {
                        existingAddresses.add(ie.getHardwareAddress());
                    }
                }
            }
        }

        if ( this.cachedSelectItems != null ) {
            return this.cachedSelectItems;
        }

        NetworkInformation ni;
        if ( ( ni = getNetworkInformaton() ) == null ) {
            log.debug("Network information unavailable"); //$NON-NLS-1$
            this.cachedSelectItems = new SelectItem[] {
                new SelectItem(null, "Custom") //$NON-NLS-1$
            };
            return this.cachedSelectItems;
        }

        boolean first = true;
        List<SelectItem> items = new ArrayList<>();
        for ( NetworkInterface netif : ni.getNetworkInterfaces() ) {
            if ( netif.getInterfaceType() != InterfaceType.ETH ) {
                continue;
            }

            if ( netif.getHardwareAddress() != null && existingAddresses.contains(netif.getHardwareAddress()) ) {
                log.debug("Skipping interfaces as address is already matched"); //$NON-NLS-1$
                continue;
            }

            if ( existingIndices.contains(netif.getInterfaceIndex()) ) {
                log.debug("Skipping interfaces as index is already matched"); //$NON-NLS-1$
                continue;
            }

            String label = netif.getInterfaceIndex() + " - " + netif.getDisplayName(); //$NON-NLS-1$
            String description = "MAC: " + netif.getHardwareAddress(); //$NON-NLS-1$
            items.add(new SelectItem(netif.getName(), label, description));

            if ( first ) {
                this.selected = netif.getName();
                first = false;
            }
        }

        items.add(new SelectItem(null, "Custom")); //$NON-NLS-1$

        this.cachedSelectItems = items.toArray(new SelectItem[0]);
        return this.cachedSelectItems;
    }


    /**
     * @return
     */
    private NetworkInformation getNetworkInformaton () {
        NetworkInformation ni;
        try {
            ni = this.sysInfoContext.getNetworkInformation();
            if ( ni == null ) {
                return null;
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
        return ni;
    }


    public InterfaceEntry getObject ( ConfigContext<?, ?> ctx ) {
        if ( ctx == null ) {
            return null;
        }

        try {
            InterfaceEntryMutable ife = (InterfaceEntryMutable) ctx.getEmptyObject("urn:agno3:objects:1.0:hostconfig:network:interface"); //$NON-NLS-1$

            NetworkInterface found = null;
            if ( !StringUtils.isBlank(this.selected) ) {
                for ( NetworkInterface netIf : this.getNetworkInformaton().getNetworkInterfaces() ) {
                    if ( this.selected.equals(netIf.getName()) ) {
                        found = netIf;
                        break;
                    }
                }
            }
            if ( found != null ) {
                ife.setAlias(found.getName());
                ife.setHardwareAddress(found.getHardwareAddress());
            }
            return ife;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);

        }

        return null;
    }
}
