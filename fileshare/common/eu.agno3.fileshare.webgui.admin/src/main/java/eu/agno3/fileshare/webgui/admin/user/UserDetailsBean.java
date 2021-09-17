/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_userDetailsBean" )
@ViewScoped
public class UserDetailsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6944071332930564303L;

    @Inject
    private UserSelectionBean userSelection;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    private boolean userDetailsLoaded;
    private UserDetails cachedSelectedUserDetails;


    /**
     * @return the selected user's user details
     */
    public UserDetails getSelectedUserDetails () {

        if ( !this.userDetailsLoaded ) {
            try {
                this.userDetailsLoaded = true;
                this.cachedSelectedUserDetails = this.fsp.getUserService().getUserDetails(this.userSelection.getSingleSelectionId());
            }
            catch ( FileshareException e ) {
                this.exceptionHandler.handleException(e);
            }
        }
        return this.cachedSelectedUserDetails;
    }


    /**
     * @return outcome
     * 
     */
    public String updateSelected () {
        if ( this.cachedSelectedUserDetails == null ) {
            return null;
        }
        try {
            this.cachedSelectedUserDetails = this.fsp.getUserService()
                    .updateUserDetails(this.userSelection.getSingleSelectionId(), this.cachedSelectedUserDetails);
        }
        catch ( FileshareException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * 
     * @param details
     * @return null
     */
    public String togglePreferredNameVerified ( UserDetails details ) {
        if ( details == null ) {
            return null;
        }

        details.setPreferredNameVerified(!details.getPreferredNameVerified());
        return null;
    }


    /**
     * 
     * @param details
     * @return null
     */
    public String toggleMailAddressVerified ( UserDetails details ) {
        if ( details == null ) {
            return null;
        }

        details.setMailAddressVerified(!details.getMailAddressVerified());
        return null;
    }

}
