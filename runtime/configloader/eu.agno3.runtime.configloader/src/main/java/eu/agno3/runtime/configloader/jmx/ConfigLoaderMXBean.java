/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader.jmx;


import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface ConfigLoaderMXBean {

    /**
     * @param spec
     * @return whether the operation was successful
     */
    public boolean reloadConfig ( String spec );


    /**
     * 
     * @param spec
     * @return whether the operation was successful
     */
    public boolean forceReloadConfig ( String spec );


    /**
     * @param pids
     * @return whether the operation was successful
     */
    public boolean reloadConfig ( Set<String> pids );

}
