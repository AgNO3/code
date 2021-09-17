/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareConfigurationMutable.class )
public interface FileshareConfigurationMutable extends FileshareConfiguration {

    /**
     * @param authConfiguration
     */
    void setAuthConfiguration ( FileshareAuthConfigMutable authConfiguration );


    /**
     * 
     * @param securityPolicyConfiguration
     */
    void setSecurityPolicyConfiguration ( FileshareSecurityPolicyConfigMutable securityPolicyConfiguration );


    /**
     * 
     * @param notificationConfiguration
     */
    void setNotificationConfiguration ( FileshareNotificationConfigMutable notificationConfiguration );


    /**
     * 
     * @param contentConfiguration
     */
    void setContentConfiguration ( FileshareContentConfigMutable contentConfiguration );


    /**
     * 
     * @param userConfiguration
     */
    void setUserConfiguration ( FileshareUserConfigMutable userConfiguration );


    /**
     * 
     * @param webConfiguration
     */
    void setWebConfiguration ( FileshareWebConfigMutable webConfiguration );


    /**
     * @param storageConfiguration
     */
    void setStorageConfiguration ( FileshareStorageConfigMutable storageConfiguration );


    /**
     * @param advancedConfiguration
     */
    void setAdvancedConfiguration ( FileshareAdvancedConfigMutable advancedConfiguration );


    /**
     * @param loggerConfiguration
     */
    void setLoggerConfiguration ( FileshareLoggerConfigMutable loggerConfiguration );

}
