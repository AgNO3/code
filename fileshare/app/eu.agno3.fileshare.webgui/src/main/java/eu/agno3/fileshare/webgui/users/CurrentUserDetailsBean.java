/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.users;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;


/**
 * @author mbechler
 *
 */
@Named ( "currentUserDetailsBean" )
@ViewScoped
public class CurrentUserDetailsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 9112854564496225507L;

    private UserDetails cachedCurrentUserDetails;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private CurrentUserBean currentUserBean;


    /**
     * @return the current user's user details
     */
    public UserDetails getCurrentUserDetails () {
        if ( this.cachedCurrentUserDetails == null ) {
            try {
                this.cachedCurrentUserDetails = this.fsp.getUserService().getUserDetails(this.currentUserBean.getCurrentUser().getId());
            }
            catch ( FileshareException e ) {
                ExceptionHandler.handleException(e);
            }
        }
        return this.cachedCurrentUserDetails;
    }


    /**
     * @return outcome
     */
    public String updateCurrentUser () {
        if ( this.cachedCurrentUserDetails == null ) {
            return null;
        }
        try {
            this.cachedCurrentUserDetails = this.fsp.getUserService().updateUserDetails(
                this.currentUserBean.getCurrentUser().getId(),
                this.cachedCurrentUserDetails);

            return null;
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
        }
        return null;
    }

}
