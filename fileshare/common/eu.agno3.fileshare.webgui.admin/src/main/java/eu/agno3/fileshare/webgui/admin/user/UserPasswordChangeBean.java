/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_userPasswordChangeBean" )
@ViewScoped
public class UserPasswordChangeBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5407126612849249631L;
    private String oldPassword;
    private String newPassword;


    /**
     * @return the oldPassword
     */
    public String getOldPassword () {
        return this.oldPassword;
    }


    /**
     * @param oldPassword
     *            the oldPassword to set
     */
    public void setOldPassword ( String oldPassword ) {
        this.oldPassword = oldPassword;
    }


    /**
     * @return the newPassword
     */
    public String getNewPassword () {
        return this.newPassword;
    }


    /**
     * @param newPassword
     *            the newPassword to set
     */
    public void setNewPassword ( String newPassword ) {
        this.newPassword = newPassword;
    }

}
