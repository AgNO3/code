/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.06.2015 by mbechler
 */
package eu.agno3.runtime.cdi.bootstrap;


import java.io.Serializable;
import java.util.logging.Level;

import javax.el.ELResolver;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import org.apache.webbeans.annotation.InitializedLiteral;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectableBeanManager;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.lifecycle.AbstractLifeCycle;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.web.util.ServletCompatibilityUtil;


/**
 * Manages container lifecycle.
 *
 * <p>
 * Behaves according to the request, session, and application contexts of the web application.
 * </p>
 *
 * @version $Rev$ $Date$
 * @see org.apache.webbeans.servlet.WebBeansConfigurationListener
 */
public final class WebContainerLifecycle extends AbstractLifeCycle {

    /**
     * Creates a new lifecycle instance and initializes
     * the instance variables.
     */
    public WebContainerLifecycle () {
        super(null);
        this.logger = WebBeansLoggerFacade.getLogger(WebContainerLifecycle.class);
    }


    /**
     * Creates a new lifecycle instance and initializes
     * the instance variables.
     * 
     * @param webBeansContext
     */
    public WebContainerLifecycle ( WebBeansContext webBeansContext ) {
        super(null, webBeansContext);
        this.logger = WebBeansLoggerFacade.getLogger(WebContainerLifecycle.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void startApplication ( Object startupObject ) {
        ServletContext servletContext = getServletContext(startupObject);
        super.startApplication(servletContext);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void stopApplication ( Object endObject ) {
        ServletContext servletContext = getServletContext(endObject);
        super.stopApplication(servletContext);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterStartApplication ( final Object startupObject ) {
        ELAdaptor elAdaptor = getWebBeansContext().getService(ELAdaptor.class);
        ELResolver resolver = elAdaptor.getOwbELResolver();
        // Application is configured as JSP
        if ( getWebBeansContext().getOpenWebBeansConfiguration().isJspApplication() ) {
            this.logger.log(Level.FINE, "Application is configured as JSP. Adding EL Resolver."); //$NON-NLS-1$
            setJspELFactory((ServletContext) startupObject, resolver);
        }
        ServletContext servletContext = null;
        if ( startupObject instanceof ServletContext ) {
            servletContext = (ServletContext) ( startupObject );
            // Add BeanManager to the 'javax.enterprise.inject.spi.BeanManager' servlet context attribute
            servletContext.setAttribute(BeanManager.class.getName(), getBeanManager());
        }
        // fire @Initialized(ApplicationScoped.class) if any observer for it exists
        if ( this.webBeansContext.getBeanManagerImpl().getNotificationManager()
                .hasContextLifecycleObserver(InitializedLiteral.INSTANCE_APPLICATION_SCOPED) ) {
            // we need to temporarily start the ReqeustContext
            this.webBeansContext.getContextsService().startContext(RequestScoped.class, null);
            this.webBeansContext.getBeanManagerImpl()
                    .fireEvent(servletContext != null ? servletContext : new Object(), InitializedLiteral.INSTANCE_APPLICATION_SCOPED);
            // shut down the RequestContext again
            this.webBeansContext.getContextsService().endContext(RequestScoped.class, null);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.webbeans.lifecycle.AbstractLifeCycle#getBeanManager()
     */
    @Override
    public BeanManager getBeanManager () {
        BeanManager beanManager = super.getBeanManager();
        if ( beanManager instanceof Serializable ) {
            return beanManager;
        }

        return new InjectableBeanManager((BeanManagerImpl) beanManager);
    }


    @Override
    protected void beforeStartApplication ( Object startupObject ) {
        this.scannerService.init(startupObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeStopApplication ( Object stopObject ) {
        this.webBeansContext.getContextsService().endContext(RequestScoped.class, null);
        this.webBeansContext.getContextsService().endContext(ConversationScoped.class, null);
        this.webBeansContext.getContextsService().endContext(SessionScoped.class, null);
        this.webBeansContext.getContextsService().endContext(ApplicationScoped.class, null);
        this.webBeansContext.getContextsService().endContext(Singleton.class, null);
        // clean up the EL caches after each request
        ELContextStore elStore = ELContextStore.getInstance(false);
        if ( elStore != null ) {
            elStore.destroyELContextStore();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterStopApplication ( Object stopObject ) {
        ServletContext servletContext;
        if ( stopObject instanceof ServletContext ) {
            servletContext = (ServletContext) stopObject;
        }
        else {
            servletContext = getServletContext(stopObject);
        }
        // Clear the resource injection service
        ResourceInjectionService injectionServices = getWebBeansContext().getService(ResourceInjectionService.class);
        if ( injectionServices != null ) {
            injectionServices.clear();
        }
        // Comment out for commit OWB-502
        // ContextFactory.cleanUpContextFactory();
        this.cleanupShutdownThreadLocals();
        if ( this.logger.isLoggable(Level.INFO) ) {
            this.logger.log(Level.INFO, OWBLogConst.INFO_0002, ServletCompatibilityUtil.getServletInfo(servletContext));
        }
    }


    /**
     * Ensures that all ThreadLocals, which could have been set in this
     * (shutdown-) Thread, are removed in order to prevent memory leaks.
     */
    private void cleanupShutdownThreadLocals () {
        this.contextsService.removeThreadLocals();
    }


    /**
     * Returns servelt context otherwise throws exception.
     * 
     * @param object
     *            object
     * @return servlet context
     */
    private static ServletContext getServletContext ( Object object ) {
        if ( object != null ) {
            if ( object instanceof ServletContextEvent ) {
                return ( (ServletContextEvent) object ).getServletContext();
            }

            throw new WebBeansException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0018));
        }
        throw new IllegalArgumentException("ServletContextEvent object but found null"); //$NON-NLS-1$
    }


    protected void setJspELFactory ( ServletContext startupObject, ELResolver resolver ) {
        JspFactory factory = JspFactory.getDefaultFactory();
        if ( factory == null ) {
            try { // no need of using the tccl since in OSGi it is init elsewhere and using container shortcut can just
                  // make it faster
                Class.forName("org.apache.jasper.compiler.JspRuntimeContext", true, WebContainerLifecycle.class.getClassLoader()); //$NON-NLS-1$
                factory = JspFactory.getDefaultFactory();
            }
            catch ( Exception e ) {
                // ignore
            }
        }
        if ( factory != null ) {
            JspApplicationContext applicationCtx = factory.getJspApplicationContext(startupObject);
            applicationCtx.addELResolver(resolver);
        }
        else {
            this.logger.log(Level.FINE, "Default JSPFactroy instance has not found"); //$NON-NLS-1$
        }
    }
}