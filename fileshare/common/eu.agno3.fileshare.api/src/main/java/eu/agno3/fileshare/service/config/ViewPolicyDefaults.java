/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.HashSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public final class ViewPolicyDefaults {

    /**
     * 
     */
    private ViewPolicyDefaults () {}

    /**
     * 
     */
    public static final Set<String> DEFAULT_VIEWABLE_MIME_TYPES = new HashSet<>();

    /**
     * 
     */
    public static final Set<String> DEFAULT_SAFE_MIME_TYPES = new HashSet<>();

    /**
     * 
     */
    public static final Set<String> DEFAULT_NO_SANDBOX_MIME_TYPES = new HashSet<>();

    /**
     * 
     */
    public static final Set<String> DEFAULT_RELAXED_CSP_MIME_TYPES = new HashSet<>();


    static {
        DEFAULT_VIEWABLE_MIME_TYPES.add("text/html"); //$NON-NLS-1$
        DEFAULT_VIEWABLE_MIME_TYPES.add("text/xml"); //$NON-NLS-1$
        DEFAULT_VIEWABLE_MIME_TYPES.add("application/pdf"); //$NON-NLS-1$
        DEFAULT_VIEWABLE_MIME_TYPES.add("application/xhtml+xml"); //$NON-NLS-1$
        DEFAULT_VIEWABLE_MIME_TYPES.add("image/svg+xml"); //$NON-NLS-1$

        DEFAULT_SAFE_MIME_TYPES.add("text/plain"); //$NON-NLS-1$

        DEFAULT_SAFE_MIME_TYPES.add("image/jpeg"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("image/gif"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("image/png"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("image/bmp"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("image/tiff"); //$NON-NLS-1$

        DEFAULT_SAFE_MIME_TYPES.add("audio/aac"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("audio/mp4"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("audio/mpeg"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("audio/ogg"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("audio/wav"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("audio/webm"); //$NON-NLS-1$

        DEFAULT_SAFE_MIME_TYPES.add("video/mpeg"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("video/mp4"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("video/ogg"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("video/webm"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("video/x-m4v"); //$NON-NLS-1$
        DEFAULT_SAFE_MIME_TYPES.add("video/x-msvideo"); //$NON-NLS-1$

        // PDF is assumed not to access any external resources when rendered, as it is rendered through PDF.js
        DEFAULT_RELAXED_CSP_MIME_TYPES.add("application/pdf"); //$NON-NLS-1$

        // these are potentially rendered without sandboxing anyway
        // browser builtin viewers may require a relaxed CSP policy for accessing the resource itself
        DEFAULT_RELAXED_CSP_MIME_TYPES.addAll(DEFAULT_SAFE_MIME_TYPES);
    }
}
