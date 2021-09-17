/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Locale;
import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareNotificationConfig.class )
public interface FileshareNotificationConfigMutable extends FileshareNotificationConfig {

    /**
     * 
     * @param expirationNotificationPeriod
     */
    void setExpirationNotificationPeriod ( Duration expirationNotificationPeriod );


    /**
     * 
     * @param footer
     */
    void setFooter ( String footer );


    /**
     * 
     * @param adminContactAddress
     */
    void setAdminContactAddress ( String adminContactAddress );


    /**
     * 
     * @param defaultSenderAddress
     */
    void setDefaultSenderAddress ( String defaultSenderAddress );


    /**
     * 
     * @param defaultSenderName
     */
    void setDefaultSenderName ( String defaultSenderName );


    /**
     * 
     * @param sendAsUserNotificationDomains
     */
    void setSendAsUserNotificationDomains ( Set<String> sendAsUserNotificationDomains );


    /**
     * 
     * @param defaultNotificationLocale
     */
    void setDefaultNotificationLocale ( Locale defaultNotificationLocale );


    /**
     * 
     * @param templateLibrary
     */
    void setTemplateLibrary ( String templateLibrary );


    /**
     * 
     * @param notificationDisabled
     */
    void setNotificationDisabled ( Boolean notificationDisabled );

}
