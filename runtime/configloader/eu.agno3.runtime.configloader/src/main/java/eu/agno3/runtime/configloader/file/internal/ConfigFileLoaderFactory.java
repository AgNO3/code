/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader.file.internal;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.configloader.file.ConfigFileLoader;
import eu.agno3.runtime.configloader.file.ConfigFileLoaderBuilder;
import eu.agno3.runtime.configloader.file.ConfigFileLoaderImpl;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigFileLoader.class, servicefactory = true )
public class ConfigFileLoaderFactory extends ConfigFileLoaderImpl {

    private ConfigFileLoaderBuilder builder;


    @Reference
    protected synchronized void setConfigFileLoaderBuilder ( ConfigFileLoaderBuilder b ) {
        this.builder = b;
    }


    protected synchronized void unsetConfigFileLoaderBuilder ( ConfigFileLoaderBuilder b ) {
        if ( this.builder == b ) {
            this.builder = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.builder.setup(this);
        this.setFallbackBundle(ctx.getUsingBundle());
    }

}
