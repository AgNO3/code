/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.network;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "networkConfigBean" )
public class NetworkConfigBean {

    private static final String NETWORK_CONFIG_TYPE = "urn:agno3:objects:1.0:hostconfig:network";//$NON-NLS-1$


    public boolean isIpv6Enabled ( OuterWrapper<?> outer ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        if ( outer == null ) {
            return true;
        }

        AbstractObjectEditor<?> netConfigEditor = outer.resolve(NETWORK_CONFIG_TYPE, null);

        if ( netConfigEditor == null ) {
            Object parameter = outer.getEditor().getParameter("ipv6Enabled"); //$NON-NLS-1$
            if ( parameter != null ) {
                return (boolean) parameter;
            }
            return true;
        }

        NetworkConfiguration netConfigEnforced = (NetworkConfiguration) netConfigEditor.getEnforced();

        if ( netConfigEnforced != null && netConfigEnforced.getIpv6Enabled() != null ) {
            return netConfigEnforced.getIpv6Enabled();
        }

        NetworkConfiguration netConfig = (NetworkConfiguration) netConfigEditor.getCurrent();
        if ( netConfig != null && netConfig.getIpv6Enabled() != null ) {
            return netConfig.getIpv6Enabled();
        }

        NetworkConfiguration netConfigInherited = (NetworkConfiguration) netConfigEditor.getDefaults();

        return netConfigInherited.getIpv6Enabled();

    }
}
