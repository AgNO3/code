/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.List;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareUserQuotaConfig.class )
public interface FileshareUserQuotaConfigMutable extends FileshareUserQuotaConfig {

    /**
     * 
     * @param disableSizeTrackingWithoutQuota
     */
    void setDisableSizeTrackingWithoutQuota ( Boolean disableSizeTrackingWithoutQuota );


    /**
     * 
     * @param globalDefaultQuota
     */
    void setGlobalDefaultQuota ( Long globalDefaultQuota );


    /**
     * 
     * @param defaultQuotaRules
     */
    void setDefaultQuotaRules ( List<FileshareQuotaRule> defaultQuotaRules );


    /**
     * @param enableDefaultQuota
     */
    void setEnableDefaultQuota ( Boolean enableDefaultQuota );
}
