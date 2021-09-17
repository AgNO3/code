/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.service.SessionService;
import eu.agno3.runtime.security.cas.client.CasPrincipalWrapper;


/**
 * @author mbechler
 *
 */
@Named ( "loginController" )
public class LoginController {

    private static final Logger log = Logger.getLogger(LoginController.class);

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private GuiWsClientFactory wsClientFactory;

    @Inject
    @OsgiService ( dynamic = true, timeout = 100 )
    private GuiConfig guiConfig;


    public String logout () {
        try {
            this.wsClientFactory.createService(SessionService.class).logout();
        }
        catch ( SessionException e ) {
            log.warn("Failed to log out from backend session", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.warn("Failed to get session service", e); //$NON-NLS-1$
        }

        Subject subject = SecurityUtils.getSubject();
        String authServerName = getAuthServerName(subject);

        subject.logout();

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(getLogoutUrl(authServerName));
        }
        catch (
            IOException |
            EncoderException e ) {
            log.warn("Failed to redirect to logout URL", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param subject
     * @return
     */
    private static String getAuthServerName ( Subject subject ) {
        String authServerName = null;
        CasPrincipalWrapper casPrincipal = subject.getPrincipals().oneByType(CasPrincipalWrapper.class);
        if ( casPrincipal != null ) {
            authServerName = (String) casPrincipal.getAttributes().get("authServerName"); //$NON-NLS-1$
        }
        return authServerName;
    }


    public String getChangePasswordLink () {
        return this.guiConfig.getAuthServerURL(getAuthServerName(SecurityUtils.getSubject())) + "changePassword.xhtml"; //$NON-NLS-1$
    }


    protected String getLogoutUrl ( String authServerName ) throws EncoderException, MalformedURLException {
        URLCodec codec = new URLCodec();
        return this.guiConfig.getAuthServerURL(authServerName) + "logout?service=" + codec.encode(this.getLocalLogoutPage()); //$NON-NLS-1$
    }


    protected String getLocalLogoutPage () throws MalformedURLException {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        URL u = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath() + "/loggedOut.xhtml", null); //$NON-NLS-1$
        return u.toString();
    }
}
