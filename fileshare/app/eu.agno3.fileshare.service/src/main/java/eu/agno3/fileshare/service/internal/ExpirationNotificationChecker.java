/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;


/**
 * @author mbechler
 *
 */
public class ExpirationNotificationChecker implements NotificationChecker {

    private static final String DISABLE_EXPIRATION_NOTIFICATION = "disableExpirationNotification"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.internal.NotificationChecker#shouldNotify(eu.agno3.fileshare.model.Subject)
     */
    @Override
    public boolean shouldNotify ( Subject s ) {

        if ( s instanceof Group ) {
            return ! ( (Group) s ).getDisableNotifications();
        }
        else if ( s instanceof User ) {
            String disabledNotifications = ( (User) s ).getPreferences().get(DISABLE_EXPIRATION_NOTIFICATION);
            if ( !StringUtils.isBlank(disabledNotifications) && Boolean.parseBoolean(disabledNotifications) ) {
                return false;
            }

            return true;
        }

        return false;
    }

}
