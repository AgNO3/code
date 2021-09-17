/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader.file.internal;


import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.configloader.ConfigSearchPaths;
import eu.agno3.runtime.configloader.file.ConfigFileLoader;
import eu.agno3.runtime.configloader.file.ConfigFileLoaderBuilder;
import eu.agno3.runtime.configloader.file.ConfigFileLoaderImpl;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigFileLoaderBuilder.class )
public class ConfigFileLoaderBuilderImpl implements ConfigFileLoaderBuilder {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.file.ConfigFileLoaderBuilder#setup(eu.agno3.runtime.configloader.file.ConfigFileLoader)
     */
    @Override
    public void setup ( ConfigFileLoader configFileLoaderFactory ) {
        configFileLoaderFactory.setSearchDirs(ConfigSearchPaths.getSearchDirs());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.file.ConfigFileLoaderBuilder#createForBundle(org.osgi.framework.Bundle)
     */
    @Override
    public ConfigFileLoader createForBundle ( Bundle b ) {
        return new ConfigFileLoaderImpl(ConfigSearchPaths.getSearchDirs(), b);
    }

}
