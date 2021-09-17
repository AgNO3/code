/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;


/**
 * @author mbechler
 *
 */
public interface ConfigManager {

    /**
     * 
     * @return whether the config exists
     */
    boolean exists ();


    /**
     * @throws IOException
     * 
     */
    void remove () throws IOException;


    /**
     * 
     * @return the configured properties
     * @throws IOException
     */
    Properties read () throws IOException;


    /**
     * 
     * @param data
     * @throws IOException
     */
    void write ( Properties data ) throws IOException;


    /**
     * 
     * @param f
     * @throws IOException
     */
    void replace ( Path f ) throws IOException;


    /**
     * @return whether this is a factory instance
     */
    boolean isFactoryInstance ();


    /**
     * @return the factory id, null if not a factory
     */
    String getFactoryId ();


    /**
     * @return the instance id
     */
    String getInstanceId ();


    /**
     * @return the path to the config file
     */
    Path getPath ();
}
