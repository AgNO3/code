/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.Set;

import org.apache.shiro.subject.Subject;
import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public interface QuotaConfiguration {

    /**
     * @param roles
     * @return the default quota
     */
    Long getDefaultQuotaForRoles ( Set<String> roles );


    /**
     * @param s
     * @return the default quota
     */
    Long getDefaultQuotaForSubject ( Subject s );


    /**
     * @return whether to track combined sizes even without a quota
     */
    boolean isTrackCombinedSizesWithoutQuota ();


    /**
     * @return the interval in which full directory size updates are done
     */
    Duration getDirectoryUpdateInterval ();


    /**
     * @return the default quota when creating users
     */
    Long getGlobalDefaultQuota ();


    /**
     * @return quota persistence interval
     */
    Duration getQuotaPersistenceInterval ();
}
