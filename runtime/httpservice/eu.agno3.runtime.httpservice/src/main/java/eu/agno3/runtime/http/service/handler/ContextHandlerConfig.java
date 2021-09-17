/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.handler;


/**
 * @author mbechler
 * 
 */
public final class ContextHandlerConfig {

    private ContextHandlerConfig () {}

    /**
     * Allow null PathInfo
     */
    public static final String ALLOW_NULL_PATHINFO_ATTR = "allowNullPathInfo"; //$NON-NLS-1$

    /**
     * Compact multiple
     */
    public static final String COMPACT_PATH_ATTR = "compactPaths"; //$NON-NLS-1$

    /**
     * Context path to register with
     */
    public static final String CONTEXT_PATH_ATTR = "contextPath"; //$NON-NLS-1$

    /**
     * Context name
     */
    public static final String CONTEXT_NAME_ATTR = "contextName"; //$NON-NLS-1$

    /**
     * Handler display name
     */
    public static final String DISPLAY_NAME_ATTR = "displayName"; //$NON-NLS-1$

    /**
     * Relative path of static resources served
     */
    public static final String RESOURCE_BASE_ATTR = "resourceBase"; //$NON-NLS-1$

    /**
     * Maximum size of form body
     */
    public static final String MAX_FORM_CONTENT_SIZE_ATTR = "maxFormContentSize"; //$NON-NLS-1$

    /**
     * Maximum number of form keys
     */
    public static final String MAX_FORM_KEYS_ATTR = "maxFormKeys"; //$NON-NLS-1$

    /**
     * Welcome files for this context, comma separated
     */
    public static final String WELCOME_FILES_ATTR = "welcomeFiles"; //$NON-NLS-1$

    /**
     * MIME types mapped for this context
     */
    public static final String MIME_TYPES_ATTR = "mimeTypes"; //$NON-NLS-1$

    /**
     * Encodings to use for specific locales
     */
    public static final String LOCALE_ENCODING_ATTR = "localeEncodings"; //$NON-NLS-1$

    /**
     * Virtual hosts for which this handler will be used
     */
    public static final String VIRTUAL_HOSTS_ATTR = "virtualHosts"; //$NON-NLS-1$

    /**
     * Protected targets
     */
    public static final String PROTECTED_TARGETS_ATTR = "protectedTargets"; //$NON-NLS-1$

    /**
     * Enable directory listings (resource)
     */
    public static final String DIRECTORY_LISTING_ATTR = "directoryListing"; //$NON-NLS-1$

    /**
     * Stylesheet used in directordy listings (resource)
     */
    public static final String DIRLISTING_STYLESHEET_ATTR = "directoryListingStylesheet"; //$NON-NLS-1$

    /**
     * Enable ETAGs (resource)
     */
    public static final String ETAG_ATTR = "etags"; //$NON-NLS-1$

    /**
     * Minimum content length for memory mapped transfers, -1 to disable (default)
     */
    public static final String MIN_MMAP_LENGTH_ATTR = "minMMapLength"; //$NON-NLS-1$

    /**
     * Minimum content length for asynchronous transfers, -1 to disable, 0 (default) for buffer size
     */
    public static final String MIN_ASYNC_LENGTH_ATTR = "minASyncLength"; //$NON-NLS-1$

    /**
     * Default cache control header
     */
    public static final String CACHE_CONTROL_ATTR = "defaultCacheControl"; //$NON-NLS-1$

    /**
     * Temporary file directory (uploads)
     */
    public static final Object TEMP_DIR = "tempDir"; //$NON-NLS-1$
}
