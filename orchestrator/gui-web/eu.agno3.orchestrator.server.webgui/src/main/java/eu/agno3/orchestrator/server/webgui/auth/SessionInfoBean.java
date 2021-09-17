/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientSessionContext;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;


/**
 * @author mbechler
 *
 */
@Named ( "sessionInfoBean" )
@ApplicationScoped
public class SessionInfoBean {

    @Inject
    @OsgiService
    private GuiWsClientSessionContext sessContext;


    public SessionInfo getSessionInfo () throws SessionException {
        return this.sessContext.getCurrentSessionInfo();
    }


    public String getUsername () throws SessionException {
        return this.getSessionInfo().getUserPrincipal().getUserName();
    }


    public Set<String> getRoles () throws SessionException {
        return this.getSessionInfo().getRoles();
    }


    public Set<String> getPermissions () throws SessionException {
        return this.getSessionInfo().getPermissions();
    }
}
