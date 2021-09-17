/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


/**
 * @author mbechler
 *
 */
public interface ConfigFileManager {

    /**
     * 
     * @return the present files
     */
    List<String> getFiles ();


    /**
     * 
     * @param file
     * @throws IOException
     */
    void remove ( String file ) throws IOException;


    /**
     * 
     * @param file
     * @param f
     * @throws IOException
     */
    void createOrReplace ( String file, Path f ) throws IOException;


    /**
     * @param path
     * @return the real file path
     * @throws IOException
     */
    Path getPath ( String path ) throws IOException;


    /**
     * 
     * @return the base path
     */
    Path getBasePath ();

}
