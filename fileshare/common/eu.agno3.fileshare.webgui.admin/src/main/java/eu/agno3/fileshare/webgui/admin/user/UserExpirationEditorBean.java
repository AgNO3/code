/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.io.Serializable;
import java.util.Date;

import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;

import eu.agno3.fileshare.model.User;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_userExpirationEditorBean" )
@ViewScoped
public class UserExpirationEditorBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1982455309622257675L;

    @Inject
    private UserSelectionBean userSelection;

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
            }
        }
        return this.expires;
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
