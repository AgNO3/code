/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServletContextListener.class, immediate = true )
public class OSGIEnvironmentLoaderListener extends EnvironmentLoaderListener {

    private static final String CONTEXT_NAME_PROPERTY = "context"; //$NON-NLS-1$
    private static final String CONTEXT_NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(OSGIEnvironmentLoaderListener.class);
    private ComponentFactory filterRegFactory;
    private Map<ServletContext, ComponentInstance> filterReg = new ConcurrentHashMap<>();
    private Map<ServletContext, ComponentInstance> securityManagerReg = new ConcurrentHashMap<>();
    private Map<ServletContext, ComponentInstance> filterChainManagerReg = new ConcurrentHashMap<>();

    private ComponentFactory securityManagerFactory;
    private ComponentFactory filterChainManagerFactory;


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        for ( ComponentInstance instance : this.filterReg.values() ) {
            instance.dispose();
        }

        for ( ComponentInstance instance : this.securityManagerReg.values() ) {
            instance.dispose();
        }
    }


    @Reference ( target = "(component.factory=" + SecurityFilterRegistration.FACTORY + ")" )
    protected synchronized void setServletFilterFactory ( ComponentFactory factory ) {
        this.filterRegFactory = factory;
    }


    protected synchronized void unsetServletFilterFactory ( ComponentFactory factory ) {
        if ( this.filterRegFactory == factory ) {
            this.filterRegFactory = null;
        }
    }


    @Reference ( target = "(component.factory=" + OSGIWebSecurityManager.FACTORY + ")" )
    protected synchronized void setSecurityManagerFactory ( ComponentFactory factory ) {
        this.securityManagerFactory = factory;
    }


    protected synchronized void unsetSecurityManagerFactory ( ComponentFactory factory ) {
        if ( this.securityManagerFactory == factory ) {
            this.securityManagerFactory = null;
        }
    }


    @Reference ( target = "(component.factory=" + OSGIFilterChainManager.FACTORY + ")" )
    protected synchronized void setFilterChainManagerFactory ( ComponentFactory factory ) {
        this.filterChainManagerFactory = factory;
    }


    protected synchronized void unsetFilterChainManagerFactory ( ComponentFactory factory ) {
        if ( this.filterChainManagerFactory == factory ) {
            this.filterChainManagerFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shiro.web.env.EnvironmentLoaderListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized ( ServletContextEvent sce ) {
        super.contextInitialized(sce);
        Dictionary<String, Object> params = new Hashtable<>();
        params.put(CONTEXT_NAME_PROPERTY, sce.getServletContext().getAttribute(CONTEXT_NAME_ATTRIBUTE));
        this.filterReg.put(sce.getServletContext(), this.filterRegFactory.newInstance(params));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shiro.web.env.EnvironmentLoader#createEnvironment(javax.servlet.ServletContext)
     */
    @Override
    protected WebEnvironment createEnvironment ( ServletContext ctx ) {
        String contextName = (String) ctx.getAttribute(CONTEXT_NAME_ATTRIBUTE);
        if ( log.isDebugEnabled() ) {
            log.debug("Creating environment in " + contextName); //$NON-NLS-1$
        }
        DefaultWebEnvironment env = new DefaultWebEnvironment();
        env.setServletContext(ctx);

        Dictionary<String, Object> securityManagerParams = new Hashtable<>();
        securityManagerParams.put(CONTEXT_NAME_PROPERTY, contextName);
        securityManagerParams.put("Realm.target", OSGIWebSecurityManager.makeContextFilter(contextName)); //$NON-NLS-1$
        ComponentInstance securityManager = this.securityManagerFactory.newInstance(securityManagerParams);
        env.setSecurityManager((SecurityManager) securityManager.getInstance());
        this.securityManagerReg.put(ctx, securityManager);

        PathMatchingFilterChainResolver filterChainResolver = new PathMatchingFilterChainResolver();
        Dictionary<String, Object> filterChainParam = new Hashtable<>();
        filterChainParam.put(CONTEXT_NAME_PROPERTY, contextName);
        ComponentInstance filterChainManager = this.filterChainManagerFactory.newInstance(filterChainParam);
        filterChainResolver.setFilterChainManager((FilterChainManager) filterChainManager.getInstance());
        this.filterChainManagerReg.put(ctx, filterChainManager);
        env.setFilterChainResolver(filterChainResolver);

        return env;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shiro.web.env.EnvironmentLoader#destroyEnvironment(javax.servlet.ServletContext)
     */
    @Override
    public void destroyEnvironment ( ServletContext servletContext ) {
        ComponentInstance c = this.filterReg.remove(servletContext);
        if ( c != null ) {
            c.dispose();
        }
        super.destroyEnvironment(servletContext);

        c = this.securityManagerReg.remove(servletContext);
        if ( c != null ) {
            c.dispose();
        }

        c = this.filterChainManagerReg.remove(servletContext);
        if ( c != null ) {
            c.dispose();

        }
    }
}
