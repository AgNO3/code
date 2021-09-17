/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public final class PathEscapeUtil {

    /**
     * 
     */
    private static final String SLASH = "/"; //$NON-NLS-1$
    private static final String ESCAPE_SLASH = "%2F"; //$NON-NLS-1$


    /**
     * 
     */
    private PathEscapeUtil () {}


    /**
     * 
     * @param pathSegment
     * @return escaped path segment
     */
    public static String escapePathSegment ( String pathSegment ) {
        return StringUtils.replace(pathSegment, SLASH, ESCAPE_SLASH);
    }


    /**
     * 
     * @param pathSegment
     * @return unescaped path segment
     */
    public static String unescapePathSegment ( String pathSegment ) {
        return StringUtils.replace(pathSegment, ESCAPE_SLASH, SLASH);
    }


    /**
     * @param relative
     * @return unescaped path segments
     */
    public static String[] splitPathIntoSegments ( String relative ) {
        String[] split = StringUtils.splitPreserveAllTokens(relative, '/');
        if ( split == null ) {
            return new String[0];
        }

        for ( int i = 0; i < split.length; i++ ) {
            split[ i ] = unescapePathSegment(split[ i ]);
        }
        return split;
    }
}
