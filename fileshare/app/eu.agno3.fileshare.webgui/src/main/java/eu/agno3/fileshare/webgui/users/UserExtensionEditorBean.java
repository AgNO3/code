/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.users;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;

import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "userExtensionEditorBean" )
@ViewScoped
public class UserExtensionEditorBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1982455309622257675L;

    @Inject
    private UserSelectionBean userSelection;

    @Inject
    private FileshareServiceProvider fsp;

    private boolean expirationLoaded;
    private DateTime expires;


    /**
     * @return the expiration date
     */
    public DateTime getExpires () {
        if ( !this.expirationLoaded ) {
            this.expirationLoaded = true;
            User u = this.userSelection.getSingleSelection();
            if ( u != null ) {
                this.expires = u.getExpiration();
                Duration extendPeriod = this.fsp.getConfigurationProvider().getUserConfig().getInvitationUserExpiration();
                if ( this.expires != null && extendPeriod != null ) {
                    this.expires = this.expires.plus(extendPeriod);
                }
            }
        }
        return this.expires;
    }


    /**
     * 
     * @return /index.xhtml on sucess, null otherwise
     */
    public String extend () {
        @Nullable
        User singleSelection = this.userSelection.getSingleSelection();

        if ( singleSelection == null ) {
            return null;
        }
        try {
            if ( this.expires != null && this.expires.isEqual(singleSelection.getExpiration()) ) {
                return null;
            }

            this.fsp.getUserService().setUserExpiry(this.userSelection.getSingleSelectionId(), this.expires);
            return "/index.xhtml?faces-redirect=true"; //$NON-NLS-1$
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param expires
     */
    public void setExpires ( DateTime expires ) {
        this.expires = expires;
    }


    /**
     * 
     * @param ev
     */
    public void unset ( ActionEvent ev ) {
        this.expires = null;
    }


    /**
     * 
     * @param ev
     */
    public void reset ( AjaxBehaviorEvent ev ) {
        this.expires = null;
        this.expirationLoaded = false;
    }


    /**
     * 
     * @return the minimum selectable expiry date
     */
    public Date getMinExpires () {
        return DateTime.now().plusDays(1).toDate();
    }

}
