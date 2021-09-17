/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader.file;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;


/**
 * Binary config file loader
 * 
 * Looksup files:
 * 1. in <cfg>/files/<path>
 * 2. in <cfg>/defaults/files/<path>
 * 3. in the calling bundles /cfgfiles/<path> (if injected via service factory)
 * 
 * @author mbechler
 *
 */
public interface ConfigFileLoader {

    /**
     * 
     * @param path
     * @return whether the file exists
     * @throws IOException
     */
    boolean exists ( String path ) throws IOException;


    /**
     * 
     * @param path
     * @return an input stream
     * @throws IOException
     */
    InputStream getInputStream ( String path ) throws IOException;


    /**
     * 
     * @param path
     * @return a URL to the file
     * @throws IOException
     */
    URL getURL ( String path ) throws IOException;


    /**
     * @param searchDirs
     */
    void setSearchDirs ( List<File> searchDirs );

}
