/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp.login;


import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Named ( "guiAuthController" )
@ApplicationScoped
public class GUIAuthController {

    private static final Logger log = Logger.getLogger(GUIAuthController.class);


    /**
     * 
     */
    public void loggedIn () {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("/gui/"); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.warn("Failed to redirect to application", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @return the logged in principal
     */
    public UserPrincipal getPrincipal () {
        return SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);
    }
}
