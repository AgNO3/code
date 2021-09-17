/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInterface;


/**
 * @author mbechler
 * 
 */
@Named ( "agentSysInfoInterfacesTree" )
@ViewScoped
public class InterfacesTreeBean implements Serializable {

    private static final Logger log = Logger.getLogger(InterfacesTreeBean.class);
    /**
     * 
     */
    private static final long serialVersionUID = 1438773421459938335L;

    private TreeNode root;
    private TreeNode selectedInterface;

    @Inject
    private AgentSysInfoContextBean sysInfo;


    /**
     * @return the root
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public TreeNode getRoot () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.root == null ) {
            this.root = makeTree();
        }
        return this.root;
    }


    public String getInterfaceDisplayName ( String id, String defName ) {

        Map<String, String> ifAliases;
        try {
            ifAliases = this.sysInfo.getNetworkInterfaceAliasMap();
        }
        catch ( Exception e ) {
            log.debug("Invalid state", e); //$NON-NLS-1$
            return defName;
        }

        String alias = ifAliases.get(id);
        if ( !StringUtils.isBlank(alias) ) {
            return alias;
        }
        return defName;
    }


    /**
     * @return the selectedInterface
     */
    public TreeNode getSelectedInterface () {
        return this.selectedInterface;
    }


    public void setSelectedInterface ( TreeNode node ) {
        if ( node != null && log.isDebugEnabled() ) {
            log.debug("Interface selected: " + node.getData()); //$NON-NLS-1$
        }
        this.selectedInterface = node;
    }


    public boolean isInterfaceSelected () {
        return this.selectedInterface != null;
    }


    public String getPanelActive () {
        if ( this.isInterfaceSelected() ) {
            return "0"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    public void setPanelActive ( String active ) {
        // ignore
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private TreeNode makeTree () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        DefaultTreeNode r = new DefaultTreeNode("root", null, null); //$NON-NLS-1$

        NetworkInformation networkInformation = this.sysInfo.getNetworkInformation();
        if ( networkInformation != null ) {
            List<NetworkInterface> ifs = networkInformation.getNetworkInterfaces();
            Collections.sort(ifs, new InterfaceIndexComparator());

            for ( NetworkInterface iface : ifs ) {
                DefaultTreeNode ifNode = makeInterfaceNode(iface, r);
                attachSubinterfaces(ifNode, iface);
            }
        }
        return r;

    }


    private void attachSubinterfaces ( DefaultTreeNode parent, NetworkInterface iface ) {
        if ( !iface.getSubInterfaces().isEmpty() ) {
            List<NetworkInterface> subifs = iface.getSubInterfaces();
            Collections.sort(subifs, new InterfaceIndexComparator());

            for ( NetworkInterface subif : subifs ) {
                DefaultTreeNode subNode = makeInterfaceNode(subif, parent);
                attachSubinterfaces(subNode, subif);
            }
        }
    }


    private static DefaultTreeNode makeInterfaceNode ( NetworkInterface iface, DefaultTreeNode parent ) {
        DefaultTreeNode t = new DefaultTreeNode("interface", iface, parent); //$NON-NLS-1$
        t.setRowKey(String.valueOf(iface.getInterfaceIndex()));
        return t;
    }
}
