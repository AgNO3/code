/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.web.ICAPConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareContentScanConfig.class )
public interface FileshareContentScanConfigMutable extends FileshareContentScanConfig {

    /**
     * 
     * @param icapConfig
     */
    void setIcapConfig ( ICAPConfigurationMutable icapConfig );


    /**
     * 
     * @param enableICAP
     */
    void setEnableICAP ( Boolean enableICAP );

}
