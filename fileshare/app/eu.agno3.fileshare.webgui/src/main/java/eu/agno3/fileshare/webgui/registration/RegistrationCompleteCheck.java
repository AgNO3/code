/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.registration;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserExistsException;
import eu.agno3.fileshare.model.tokens.RegistrationToken;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "registrationCompleteCheck" )
public class RegistrationCompleteCheck {

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private RegistrationCompletionBean completeBean;


    /**
     * 
     * @param ev
     */
    public void checkRegistration ( ComponentSystemEvent ev ) {

        TokenPrincipal tokPrinc = SecurityUtils.getSubject().getPrincipals().oneByType(TokenPrincipal.class);
        if ( tokPrinc == null || ! ( tokPrinc.getData() instanceof RegistrationToken ) ) {
            FacesContext.getCurrentInstance()
                    .addMessage(
                        null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            FileshareMessages.get(FileshareMessages.INVALID_REGISTRATION_TOKEN),
                            StringUtils.EMPTY));

            return;
        }

        RegistrationToken tok = (RegistrationToken) tokPrinc.getData();
        try {
            this.fsp.getRegistrationService().checkRegistration(tok.getUserName());
        }
        catch ( UndeclaredThrowableException e ) {
            if ( e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof UserExistsException ) {
                usernameExists();
            }
            else {
                ExceptionHandler.handleException(e);
                this.completeBean.invalidate();
            }
        }
        catch ( UserExistsException e ) {
            usernameExists();
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            this.completeBean.invalidate();
        }
    }


    /**
     * 
     */
    private static void usernameExists () {
        FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, FileshareMessages.get(FileshareMessages.REGISTRATION_USERNAME_EXISTS), StringUtils.EMPTY));
    }

}
