/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.net.URI;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.web.WebEndpointConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareWebConfig.class )
public interface FileshareWebConfigMutable extends FileshareWebConfig {

    /**
     * 
     * @param intentTimeout
     */
    void setIntentTimeout ( Duration intentTimeout );


    /**
     * 
     * @param enableWebDAV
     */
    void setEnableWebDAV ( Boolean enableWebDAV );


    /**
     * 
     * @param webEndpointConfig
     */
    void setWebEndpointConfig ( WebEndpointConfigMutable webEndpointConfig );


    /**
     * @param themeLibrary
     */
    void setThemeLibrary ( String themeLibrary );


    /**
     * 
     * @param overrideBaseURI
     */
    void setOverrideBaseURI ( URI overrideBaseURI );


    /**
     * 
     * @param webDAVAllowSetModificationTime
     */
    void setWebDAVAllowSetModificationTime ( Boolean webDAVAllowSetModificationTime );


    /**
     * 
     * @param sessionIncompleteExpireDuration
     */
    void setSessionIncompleteExpireDuration ( Duration sessionIncompleteExpireDuration );


    /**
     * 
     * @param perSessionIncompleteSizeLimitMB
     */
    void setPerSessionIncompleteSizeLimit ( Long perSessionIncompleteSizeLimitMB );


    /**
     * 
     * @param userIncompleteExpireDuration
     */
    void setUserIncompleteExpireDuration ( Duration userIncompleteExpireDuration );


    /**
     * 
     * @param perUserIncompleteSizeLimitMB
     */
    void setPerUserIncompleteSizeLimit ( Long perUserIncompleteSizeLimitMB );


    /**
     * @param perUserIncompleteSizeLimitEnabled
     */
    void setPerUserIncompleteSizeLimitEnabled ( Boolean perUserIncompleteSizeLimitEnabled );


    /**
     * @param perSessionIncompleteSizeLimitEnabled
     */
    void setPerSessionIncompleteSizeLimitEnabled ( Boolean perSessionIncompleteSizeLimitEnabled );


    /**
     * @param defaultUploadChunkSize
     */
    void setDefaultUploadChunkSize ( Long defaultUploadChunkSize );


    /**
     * @param maximumUploadChunkSize
     */
    void setMaximumUploadChunkSize ( Long maximumUploadChunkSize );


    /**
     * @param optimalUploadChunkCount
     */
    void setOptimalUploadChunkCount ( Integer optimalUploadChunkCount );

}
