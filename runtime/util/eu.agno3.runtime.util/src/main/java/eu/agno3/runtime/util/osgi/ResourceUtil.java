/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2014 by mbechler
 */
package eu.agno3.runtime.util.osgi;


import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import eu.agno3.runtime.ldap.filter.FilterEscaping;


/**
 * @author mbechler
 * 
 */
public final class ResourceUtil {

    private static final Logger log = Logger.getLogger(ResourceUtil.class);


    private ResourceUtil () {}


    /**
     * 
     * 
     * @param b
     * @param base
     * @param path
     * @return a list of entries matching path while being located under base
     */
    public static List<URL> safeFindEntries ( Bundle b, String base, String path ) {
        String normalizedPath = FilenameUtils.normalize(path);
        String escapedPath = FilterEscaping.escape(FilenameUtils.getName(normalizedPath));
        String baseDir = FilenameUtils.concat(base, FilenameUtils.getPath(normalizedPath));
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Searching for %s in %s", escapedPath, baseDir)); //$NON-NLS-1$
        }
        return safeFindPattern(b, baseDir, escapedPath, false);
    }


    /**
     * 
     * 
     * @param b
     * @param base
     * @param path
     * @return the first entry matching path while being located under base
     */
    public static URL safeFindEntry ( Bundle b, String base, String path ) {
        List<URL> matches = safeFindEntries(b, base, path);
        if ( matches.isEmpty() ) {
            return null;
        }

        return matches.get(0);
    }


    /**
     * @param b
     * @param base
     * @param pattern
     *            may include * wildcards
     * @param recursive
     * @return a list of matching files
     */
    public static List<URL> safeFindPattern ( Bundle b, String base, String pattern, boolean recursive ) {
        Enumeration<URL> urls = b.findEntries(base, pattern, recursive);

        if ( urls == null || !urls.hasMoreElements() ) {
            return Collections.EMPTY_LIST;
        }

        return Collections.list(urls);
    }

}
