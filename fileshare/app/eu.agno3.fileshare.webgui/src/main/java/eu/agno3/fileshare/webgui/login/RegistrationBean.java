/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.login;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "registrationBean" )
@ApplicationScoped
public class RegistrationBean {

    /**
     * 
     */
    private static final String LOCAL_REALM = "LOCAL"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @param realm
     * @return whether user registration is enabled
     */
    public boolean isRegistrationEnabled ( String realm ) {
        if ( !LOCAL_REALM.equals(realm) ) {
            return false;
        }

        if ( this.fsp.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            return false;
        }

        return this.fsp.getConfigurationProvider().getUserConfig().isRegistrationEnabled();
    }


    /**
     * 
     * @return whether user invitation is enabled
     */
    public boolean isInvitationEnabled () {
        if ( this.fsp.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            return false;
        }

        return this.fsp.getConfigurationProvider().getUserConfig().isInvitationEnabled();
    }


    /**
     * 
     * @return whether local password recovery is enabled
     */
    public boolean isLocalPasswordRecoveryEnabled () {
        return this.fsp.getConfigurationProvider().getUserConfig().isLocalPasswordRecoveryEnabled();
    }


    /**
     * @param realm
     * @return whether a lost password url is known
     */
    public boolean haveLostPasswordUrl ( String realm ) {

        if ( this.fsp.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            return false;
        }

        if ( LOCAL_REALM.equals(realm) && this.fsp.getConfigurationProvider().getUserConfig().isLocalPasswordRecoveryEnabled() ) {
            return true;
        }

        return this.fsp.getConfigurationProvider().getUserConfig().getLostPasswordUrl(realm) != null;
    }


    /**
     * @param realm
     * @param username
     * @return a url
     */
    public String getLostPasswordUrl ( String realm, String username ) {
        String lostPasswordUrl;
        if ( LOCAL_REALM.equals(realm) && this.fsp.getConfigurationProvider().getUserConfig().isLocalPasswordRecoveryEnabled() ) {
            lostPasswordUrl = ( (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext() ).getContextPath()
                    + "/auth/lostPassword.xhtml"; //$NON-NLS-1$
        }
        else {
            lostPasswordUrl = this.fsp.getConfigurationProvider().getUserConfig().getLostPasswordUrl(realm);
        }
        if ( lostPasswordUrl == null ) {
            return null;
        }

        try {
            return String.format("%s%srealm=%s&user=%s", //$NON-NLS-1$
                lostPasswordUrl,
                lostPasswordUrl.indexOf('?') >= 0 ? '&' : '?',
                realm != null ? URLEncoder.encode(realm, UTF_8) : StringUtils.EMPTY,
                username != null ? URLEncoder.encode(username, UTF_8) : StringUtils.EMPTY);
        }
        catch ( UnsupportedEncodingException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }
}
