/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.07.2013 by mbechler
 */
package eu.agno3.runtime.configloader;


import java.io.IOException;
import java.util.Collection;
import java.util.Set;


/**
 * @author mbechler
 * 
 */
public interface ConfigLoader {

    /**
     * Reload the configuration
     * 
     * @param hint
     * @throws IOException
     * 
     */
    void reload ( String hint ) throws IOException;


    /**
     * @param spec
     * @throws IOException
     */
    void forceReload ( String spec ) throws IOException;


    /**
     * Reload the configuration
     * 
     * @param pids
     * @throws IOException
     */
    void reload ( Set<String> pids ) throws IOException;


    /**
     * @param pid
     * @return the contributions that affect the given pid
     */
    Collection<ConfigContribution> getSourcesForPid ( String pid );

}
