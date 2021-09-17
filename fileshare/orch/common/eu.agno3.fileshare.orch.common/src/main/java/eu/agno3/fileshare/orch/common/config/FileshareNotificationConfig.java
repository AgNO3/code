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

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:notification" )
public interface FileshareNotificationConfig extends ConfigurationObject {

    /**
     * 
     * @return whether to disable all notifications
     */
    Boolean getNotificationDisabled ();


    /**
     * 
     * @return time before the actual expiration
     */
    Duration getExpirationNotificationPeriod ();


    /**
     * 
     * @return the resource library to use for mail templates
     */
    String getTemplateLibrary ();


    /**
     * 
     * @return mail footer text
     */
    String getFooter ();


    /**
     * 
     * @return admin contact address
     */
    @ValidEmail
    String getAdminContactAddress ();


    /**
     * @return default from address
     */
    @ValidEmail
    String getDefaultSenderAddress ();


    /**
     * 
     * @return default from name
     */
    String getDefaultSenderName ();


    /**
     * 
     * @return the default locale to use when sending mails
     */
    Locale getDefaultNotificationLocale ();


    /**
     * 
     * @return list of domain for which to send e-mail as the appropriate user
     */
    Set<String> getSendAsUserNotificationDomains ();

}
