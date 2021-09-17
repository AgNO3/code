/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.security.web.gui.init;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;

import eu.agno3.runtime.cdi.Eager;
import eu.agno3.runtime.security.web.SecurityInitializer;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Eager
public class OSGIEnvironmentLoaderListener extends EnvironmentLoaderListener {

    @Inject
    private static SecurityInitializer initializer;


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.env.EnvironmentLoader#createEnvironment(javax.servlet.ServletContext)
     */
    @Override
    protected WebEnvironment createEnvironment ( ServletContext ctx ) {
        if ( initializer == null ) {
            throw new IllegalStateException("Security initializer has not been set"); //$NON-NLS-1$
        }
        WebEnvironment env = super.createEnvironment(ctx);
        initializer.initWebEnvironment(env);
        return env;
    }
}
