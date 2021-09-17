/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader.file;


import org.osgi.framework.Bundle;


/**
 * @author mbechler
 *
 */
public interface ConfigFileLoaderBuilder {

    /**
     * @param configFileLoaderFactory
     * 
     */
    void setup ( ConfigFileLoader configFileLoaderFactory );


    /**
     * @param b
     * @return a config file loader for the given bundle
     */
    ConfigFileLoader createForBundle ( Bundle b );

}
