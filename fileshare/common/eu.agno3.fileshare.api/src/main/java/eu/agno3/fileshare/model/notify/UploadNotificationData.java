/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.User;


/**
 * @author mbechler
 *
 */
public class UploadNotificationData extends EntityNotificationData {

    private User uploadingUser;
    private Grant uploadingGrant;


    /**
     * @return the uploadingUser
     */
    public User getUploadingUser () {
        return this.uploadingUser;
    }


    /**
     * @param user
     */
    public void setUploadingUser ( User user ) {
        this.uploadingUser = user;
    }


    /**
     * @return the uploadingGrant
     */
    public Grant getUploadingGrant () {
        return this.uploadingGrant;
    }


    /**
     * @param g
     */
    public void setUploadingGrant ( Grant g ) {
        this.uploadingGrant = g;
    }

}
