/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:content:preview" )
public interface FileshareContentPreviewConfig extends ConfigurationObject {

    /**
     * @return whether to limit previews to files by size
     */
    Boolean getLimitPreviewFileSize ();


    /**
     * 
     * @return maximum file size for file previews, where applicable
     */
    Long getMaxPreviewFileSize ();


    /**
     * 
     * @return mime types to which to apply relaxed CSP restriction (i.e allow accessing local resources)
     */
    Set<String> getPreviewRelaxedCSPMimeTypes ();


    /**
     * 
     * @return mime types for which sandboxing will be disabled, i.e. allowing them to render inside a browser plugin
     */
    Set<String> getPreviewNoSandboxMimeTypes ();


    /**
     * 
     * @return mime types which are still rendered even if sandbox/CSP support may be unavailable
     */
    Set<String> getPreviewSafeMimeTypes ();


    /**
     * 
     * @return mime types for which previews are allowed
     */
    Set<String> getPreviewMimeTypes ();

}
