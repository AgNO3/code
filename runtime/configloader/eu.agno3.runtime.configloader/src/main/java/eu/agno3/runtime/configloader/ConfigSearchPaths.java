/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.runtime.configloader.file.internal.ConfigFileLoaderBuilderImpl;


/**
 * @author mbechler
 *
 */
public final class ConfigSearchPaths {

    private static final Logger log = Logger.getLogger(ConfigFileLoaderBuilderImpl.class);
    private static final String CONFIG_DIR_PROPERTY = "config.dir"; //$NON-NLS-1$


    /**
     * 
     */
    private ConfigSearchPaths () {}

    private static List<File> SEARCH_DIRS;


    /**
     * @return the configuration search dirs
     */
    public static List<File> getSearchDirs () {
        if ( SEARCH_DIRS == null ) {
            SEARCH_DIRS = initSearchDirs();
        }
        return SEARCH_DIRS;
    }


    /**
     * 
     */
    private static List<File> initSearchDirs () {

        List<File> searchDirs = new ArrayList<>();

        String configDirProperty = System.getProperty(CONFIG_DIR_PROPERTY);
        if ( configDirProperty != null ) {
            configDirProperty = configDirProperty.trim();
        }

        if ( configDirProperty != null && !configDirProperty.isEmpty() ) {
            configDirProperty = configDirProperty.replace("${user.home}", //$NON-NLS-1$
                System.getProperty("user.home")); //$NON-NLS-1$

            File baseDir = new File(configDirProperty, "files"); //$NON-NLS-1$
            searchDirs.add(baseDir);
            File defaultDir = new File(configDirProperty, "defaults/files"); //$NON-NLS-1$

            if ( defaultDir.isDirectory() && defaultDir.canRead() ) {
                searchDirs.add(defaultDir);
            }
        }
        else {
            log.debug("No config directory specified"); //$NON-NLS-1$
        }

        return Collections.unmodifiableList(searchDirs);
    }

}
