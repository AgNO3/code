/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.shiro.web.env.DefaultWebEnvironment;
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
public class SecurityEnvironmentLoaderListener extends EnvironmentLoaderListener {

    @Inject
    @Any
    private static Instance<SecurityInitializer> initializers;


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.env.EnvironmentLoader#createEnvironment(javax.servlet.ServletContext)
     */
    @Override
    protected WebEnvironment createEnvironment ( ServletContext ctx ) {
        if ( initializers == null ) {
            throw new IllegalStateException("Security initializer has not been set"); //$NON-NLS-1$
        }
        DefaultWebEnvironment env = (DefaultWebEnvironment) super.createEnvironment(ctx);
        for ( SecurityInitializer initializer : initializers ) {
            initializer.initWebEnvironment(env);
        }
        return env;
    }
}
