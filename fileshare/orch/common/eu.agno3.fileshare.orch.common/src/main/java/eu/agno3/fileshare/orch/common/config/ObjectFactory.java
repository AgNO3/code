/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public FileshareConfiguration createFileshareConfiguration () {
        return new FileshareConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareContentConfig createFileshareContentConfig () {
        return new FileshareContentConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareContentPreviewConfig createFileshareContentPreviewConfig () {
        return new FileshareContentPreviewConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareQuotaRule createFileshareQuotaRule () {
        return new FileshareQuotaRuleImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareContentSearchConfig createFileshareContentSearchConfig () {
        return new FileshareContentSearchConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareContentScanConfig createFileshareContentScanConfig () {
        return new FileshareContentScanConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareNotificationConfig createFileshareNotificationConfig () {
        return new FileshareNotificationConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareSecurityPolicyConfig createFileshareSecurityPolicyConfig () {
        return new FileshareSecurityPolicyConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareSecurityPolicy createFileshareSecurityPolicy () {
        return new FileshareSecurityPolicyImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareUserLabelRule createFileshareUserLabelRule () {
        return new FileshareUserLabelRuleImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareAuthConfig createFileshareAuthConfig () {
        return new FileshareAuthConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareUserConfig createFileshareUserConfig () {
        return new FileshareUserConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareUserQuotaConfig createFileshareUserQuotaConfig () {
        return new FileshareUserQuotaConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareUserSelfServiceConfig createFileshareUserSelfServiceConfig () {
        return new FileshareUserSelfServiceConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareUserTrustLevelConfig createFileshareUserTrustLevelConfig () {
        return new FileshareUserTrustLevelConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareUserTrustLevel createFileshareUserTrustLevel () {
        return new FileshareUserTrustLevelImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareWebConfig createFileshareWebConfig () {
        return new FileshareWebConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareStorageConfig createFileshareStorageConfig () {
        return new FileshareStorageConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareAdvancedConfig createFileshareAdvancedConfig () {
        return new FileshareAdvancedConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareCIFSPassthroughGroup createFileshareCIFSPassthroughGroup () {
        return new FileshareCIFSPassthroughGroupImpl();
    }


    /**
     * 
     * @return default impl
     */
    public FileshareLoggerConfig createFileshareLoggerConfig () {
        return new FileshareLoggerConfigImpl();
    }
}
