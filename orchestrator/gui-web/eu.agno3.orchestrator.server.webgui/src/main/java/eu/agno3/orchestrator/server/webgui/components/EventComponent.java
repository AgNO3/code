/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import javax.faces.component.UINamingContainer;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public class EventComponent extends UINamingContainer {

    /**
     * 
     * @return the function name for the components remote command
     */
    public String getRemoteFunction () {
        return this.getId().replace(':', '_').concat("_remote"); //$NON-NLS-1$
    }


    /**
     * 
     * @return a javascript snippet that defines the removeCommand variable with this components remoteCommand
     */
    public String getRemoteFunctionDefinition () {
        if ( this.getAttributes().get("actionListener") != null ) { //$NON-NLS-1$
            return String.format("var remoteCommand = %s;", this.getRemoteFunction()); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

}
