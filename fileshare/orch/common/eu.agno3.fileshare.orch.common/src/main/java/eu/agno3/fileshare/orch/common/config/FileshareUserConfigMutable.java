/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import eu.agno3.orchestrator.config.terms.TermsConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareUserConfig.class )
public interface FileshareUserConfigMutable extends FileshareUserConfig {

    /**
     * 
     * @param quotaConfig
     */
    void setQuotaConfig ( FileshareUserQuotaConfigMutable quotaConfig );


    /**
     * @param selfServiceConfig
     */
    void setSelfServiceConfig ( FileshareUserSelfServiceConfigMutable selfServiceConfig );


    /**
     * @param userTrustLevelConfig
     */
    void setUserTrustLevelConfig ( FileshareUserTrustLevelConfigMutable userTrustLevelConfig );


    /**
     * @param termsConfig
     */
    void setTermsConfig ( TermsConfigurationMutable termsConfig );


    /**
     * @param noSubjectRootRoles
     */
    void setNoSubjectRootRoles ( Set<String> noSubjectRootRoles );


    /**
     * @param defaultRoles
     */
    void setDefaultRoles ( Set<String> defaultRoles );

}
