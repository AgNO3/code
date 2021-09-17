/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import eu.agno3.fileshare.model.Subject;


/**
 * @author mbechler
 *
 */
public interface NotificationChecker {

    /**
     * 
     * @param s
     * @return whether the subject should be notified
     */
    public boolean shouldNotify ( Subject s );
}
