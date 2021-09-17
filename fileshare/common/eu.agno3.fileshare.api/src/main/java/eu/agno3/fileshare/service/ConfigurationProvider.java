/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import eu.agno3.fileshare.service.config.FrontendConfiguration;
import eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration;
import eu.agno3.fileshare.service.config.NotificationConfiguration;
import eu.agno3.fileshare.service.config.QuotaConfiguration;
import eu.agno3.fileshare.service.config.SearchConfiguration;
import eu.agno3.fileshare.service.config.SecurityPolicyConfiguration;
import eu.agno3.fileshare.service.config.TrustLevelConfiguration;
import eu.agno3.fileshare.service.config.UserConfiguration;
import eu.agno3.fileshare.service.config.ViewPolicyConfiguration;


/**
 * @author mbechler
 *
 */
public interface ConfigurationProvider {

    /**
     * @return the view policy configuration
     */
    ViewPolicyConfiguration getViewPolicyConfig ();


    /**
     * @return the mime type policy configuration
     */
    MimeTypePolicyConfiguration getMimeTypePolicyConfiguration ();


    /**
     * @return the user configuration
     */
    UserConfiguration getUserConfig ();


    /**
     * @return the security policy configuration
     */
    SecurityPolicyConfiguration getSecurityPolicyConfiguration ();


    /**
     * @return the notification configuration
     */
    NotificationConfiguration getNotificationConfiguration ();


    /**
     * @return the frontend configuration
     */
    FrontendConfiguration getFrontendConfiguration ();


    /**
     * 
     * @return the quota configuration
     */
    QuotaConfiguration getQuotaConfiguration ();


    /**
     * 
     * @return the trust level configuration
     */
    TrustLevelConfiguration getTrustLevelConfiguration ();


    /**
     * @return the search configuration
     */
    SearchConfiguration getSearchConfiguration ();
}
