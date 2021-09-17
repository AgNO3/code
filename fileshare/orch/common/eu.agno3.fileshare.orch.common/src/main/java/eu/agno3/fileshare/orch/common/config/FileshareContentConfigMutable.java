/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareContentConfig.class )
public interface FileshareContentConfigMutable extends FileshareContentConfig {

    /**
     * 
     * @param useUserSuppliedTypeInfo
     */
    void setUseUserSuppliedTypeInfo ( Boolean useUserSuppliedTypeInfo );


    /**
     * 
     * @param fallbackMimeType
     */
    void setFallbackMimeType ( String fallbackMimeType );


    /**
     * 
     * @param blacklistMimeTypes
     */
    void setBlacklistMimeTypes ( Set<String> blacklistMimeTypes );


    /**
     * 
     * @param whitelistMimeTypes
     */
    void setWhitelistMimeTypes ( Set<String> whitelistMimeTypes );


    /**
     * 
     * @param allowMimeTypeChanges
     */
    void setAllowMimeTypeChanges ( Boolean allowMimeTypeChanges );


    /**
     * 
     * @param scanConfig
     */
    void setScanConfig ( FileshareContentScanConfigMutable scanConfig );


    /**
     * 
     * @param searchConfig
     */
    void setSearchConfig ( FileshareContentSearchConfigMutable searchConfig );


    /**
     * 
     * @param previewConfig
     */
    void setPreviewConfig ( FileshareContentPreviewConfigMutable previewConfig );

}
