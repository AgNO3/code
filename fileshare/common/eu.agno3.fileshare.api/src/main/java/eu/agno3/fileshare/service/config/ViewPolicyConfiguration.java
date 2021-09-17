/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface ViewPolicyConfiguration {

    /**
     * @param mimeType
     * @return whether the mime type is generally viewable
     */
    boolean isViewable ( String mimeType );


    /**
     * 
     * Use with extreme caution!
     * 
     * This setting allows legacy browser without proper sandboxing or CSP support to display
     * these file types. This implies that active content might be rendered or browser
     * plugins may handle the content.
     * 
     * @param contentType
     * @return whether this is a safe type (may be viewed even if sandboxing is not available)
     */
    boolean isSafe ( String contentType );


    /**
     * 
     * Use with extreme caution!
     * 
     * This setting allows to disable the sandboxing features for file viewing.
     * Therefor these file types can be rendered by browser plugins, but this has
     * major security implications.
     * 
     * 
     * @return the mime types for which sandboxing should be disabled
     */
    Set<String> getNoSandboxMimeTypes ();


    /**
     * 
     * Use with caution!
     * 
     * This setting allows to set a relaxed CSP policy (default-src 'self' instead of 'none') for
     * certain file types.
     * 
     * @return the mime types for which unsupported CSP should be ignored
     */
    Set<String> getRelaxedCSPMimeTypes ();


    /**
     * @return the maximum preview file size in bytes
     */
    long getMaxPreviewFileSize ();

}
