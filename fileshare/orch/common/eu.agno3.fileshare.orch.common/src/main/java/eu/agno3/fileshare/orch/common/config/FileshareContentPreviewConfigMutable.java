/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareContentPreviewConfig.class )
public interface FileshareContentPreviewConfigMutable extends FileshareContentPreviewConfig {

    /**
     * 
     * @param maxPreviewFileSize
     */
    void setMaxPreviewFileSize ( Long maxPreviewFileSize );


    /**
     * 
     * @param relaxedCSPMimeTypes
     */
    void setPreviewRelaxedCSPMimeTypes ( Set<String> relaxedCSPMimeTypes );


    /**
     * 
     * @param noSandboxMimeTypes
     */
    void setPreviewNoSandboxMimeTypes ( Set<String> noSandboxMimeTypes );


    /**
     * 
     * @param safeMimeTypes
     */
    void setPreviewSafeMimeTypes ( Set<String> safeMimeTypes );


    /**
     * 
     * @param viewableMimeTypes
     */
    void setPreviewMimeTypes ( Set<String> viewableMimeTypes );


    /**
     * @param limitPreviewFileSize
     */
    void setLimitPreviewFileSize ( Boolean limitPreviewFileSize );
}
