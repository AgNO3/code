/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.runtime.cdi.comet;


import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.MetaBroadcaster;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.util.ExecutorsFactory;
import org.atmosphere.util.ForkJoinPool;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.primefaces.push.EventBusFactory;
import org.primefaces.push.impl.PushEndpointProcessor;


/**
 * @author mbechler
 *
 */
public class CDIPushServlet extends AtmosphereServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -1232065454737495197L;
    private static final Logger log = Logger.getLogger(CDIPushServlet.class);

    private ExecutorService asyncOperationExecutor;
    private ExecutorService messageDispatcher;
    private ExecutorService scheduler;


    @Override
    protected CDIPushServlet configureFramework ( ServletConfig sc ) throws ServletException {

        WebBeansContext ctx = WebBeansContext.currentInstance();
        if ( ctx == null ) {
            throw new IllegalStateException("No WebBeansContext available"); //$NON-NLS-1$
        }

        BeanManager bm = ctx.getBeanManagerImpl();
        if ( bm == null ) {
            throw new IllegalStateException("No BeanManager available"); //$NON-NLS-1$
        }

        framework().addInitParameter(ApplicationConfig.ANALYTICS, Boolean.FALSE.toString());

        boolean notConfigured = sc.getInitParameter(ApplicationConfig.READ_GET_BODY) == null;
        if ( notConfigured ) {
            // For Backward compatibility with Atmosphere 2.2.x
            framework().addInitParameter(ApplicationConfig.READ_GET_BODY, Boolean.TRUE.toString());
        }

        framework().addInitParameter(
            "org.atmosphere.cpr.broadcasterCacheClass", //$NON-NLS-1$
            "org.atmosphere.cache.UUIDBroadcasterCache"); //$NON-NLS-1$
        framework().addInitParameter(
            "org.atmosphere.cpr.AnnotationProcessor", //$NON-NLS-1$
            "eu.agno3.runtime.cdi.comet.CDIAnnotationProcessor"); //$NON-NLS-1$
        framework().addInitParameter(
            "org.atmosphere.cpr.asyncSupport", //$NON-NLS-1$
            "org.atmosphere.container.JSR356AsyncSupport"); //$NON-NLS-1$

        initExecutors(framework());

        framework().interceptor(new FixedAtmosphereResourceLifecycleInterceptor()).interceptor(new TrackMessageSizeInterceptor())
                .interceptor(new HeartbeatInterceptor()).addAnnotationPackage(PushEndpointProcessor.class).objectFactory(new CDIObjectFactory(bm));

        // set classloader to ours because jetty leaks it through ScheduledExecutorScheduler
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CDIPushServlet.class.getClassLoader());
        ServletContext servletContext = sc.getServletContext();
        try {
            WebSocketServerContainerInitializer.configureContext(unwrapServletContextHandler(servletContext));
            super.configureFramework(sc);
            try {
                EventBusProducer producer = framework().newClassInstance(EventBusProducer.class, EventBusProducer.class);
                producer.configure(framework());
                framework().newClassInstance(EventBusFactory.class, EventBusFactory.class);
                framework().getAtmosphereConfig().properties().put("evenBus", producer.getEventBus()); //$NON-NLS-1$
            }
            catch ( Exception ex ) {
                log.warn("Failed to create EventBusFactory", ex); //$NON-NLS-1$
            }
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }

        framework().getAtmosphereConfig().properties().put("servletPath", servletContext.getContextPath()); //$NON-NLS-1$
        framework().getAtmosphereConfig().startupHook(new AtmosphereConfig.StartupHook() {

            @Override
            public void started ( AtmosphereFramework fw ) {
                configureMetaBroadcasterCache(fw);
            }
        });

        if ( framework().getAtmosphereHandlers().isEmpty() ) {
            log.info("No Annotated class using @PushEndpoint found. Push will not work."); //$NON-NLS-1$
        }
        return this;
    }


    /**
     * @param servletContext
     * @return
     * @throws ServletException
     */
    private static ServletContextHandler unwrapServletContextHandler ( ServletContext servletContext ) throws ServletException {
        ContextHandler h = ContextHandler.getContextHandler(servletContext);

        if ( h instanceof ServletContextHandler ) {
            return (ServletContextHandler) h;
        }

        throw new ServletException("Failed to locate ServletContextHandler"); //$NON-NLS-1$
    }


    /**
     * 
     */
    private void initExecutors ( AtmosphereFramework framework ) {
        int procs = Runtime.getRuntime().availableProcessors();

        this.scheduler = Executors.newScheduledThreadPool(procs, new ThreadFactory() {

            private final AtomicInteger count = new AtomicInteger();


            @Override
            public Thread newThread ( final Runnable runnable ) {
                Thread t = new Thread(runnable, "Atmosphere-Scheduler-" + this.count.getAndIncrement()); //$NON-NLS-1$
                t.setDaemon(true);
                t.setContextClassLoader(CDIPushServlet.class.getClassLoader());
                return t;
            }
        });

        keepAliveThreads(this.scheduler);

        framework.getAtmosphereConfig().properties().put(ExecutorsFactory.SCHEDULER_THREAD_POOL, this.scheduler);

        this.asyncOperationExecutor = Executors.newCachedThreadPool(new AtmosphereThreadFactory(true, "Atmosphere-AsyncOp-")); //$NON-NLS-1$

        keepAliveThreads(this.asyncOperationExecutor);

        framework.getAtmosphereConfig().properties().put(ExecutorsFactory.ASYNC_WRITE_THREAD_POOL, this.asyncOperationExecutor);

        this.messageDispatcher = new ForkJoinPool(true, ExecutorsFactory.BROADCASTER_THREAD_POOL + "-DispatchOp-"); //$NON-NLS-1$
        keepAliveThreads(this.messageDispatcher);

        framework.getAtmosphereConfig().properties().put(ExecutorsFactory.BROADCASTER_THREAD_POOL, this.messageDispatcher);
    }


    private static void keepAliveThreads ( ExecutorService t ) {
        if ( !ThreadPoolExecutor.class.isAssignableFrom(t.getClass()) ) {
            return;
        }
        ThreadPoolExecutor e = ThreadPoolExecutor.class.cast(t);
        e.setKeepAliveTime(ExecutorsFactory.DEFAULT_KEEP_ALIVE, TimeUnit.SECONDS);
        e.allowCoreThreadTimeOut(true);
    }


    private void shutdownExecutors () {

        ExecutorService e = this.asyncOperationExecutor;
        if ( e != null ) {
            e.shutdown();
            this.asyncOperationExecutor = null;
        }

        e = this.scheduler;
        if ( e != null ) {
            e.shutdown();
            this.scheduler = null;
        }

        e = this.messageDispatcher;
        if ( e != null ) {
            e.shutdown();
            this.messageDispatcher = null;
        }
    }


    protected void configureMetaBroadcasterCache ( AtmosphereFramework framework ) {
        framework().getAtmosphereConfig().metaBroadcaster()
                .cache(new MetaBroadcaster.ThirtySecondsCache(framework().getAtmosphereConfig().metaBroadcaster(), framework.getAtmosphereConfig()));
    }


    @Override
    protected AtmosphereFramework newAtmosphereFramework () {
        AtmosphereFramework fw = super.newAtmosphereFramework();
        fw.annotationProcessorClassName(CDIAnnotationProcessor.class.getName());
        return fw;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service ( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        resp.setContentType("text/plain"); //$NON-NLS-1$
        GuardingResponseWrapper wrapper = new GuardingResponseWrapper(resp);
        super.service(req, wrapper);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.atmosphere.cpr.AtmosphereServlet#destroy()
     */
    @Override
    public void destroy () {
        this.framework().destroy();
        shutdownExecutors();
        super.destroy();
    }

    private final static class AtmosphereThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger();
        private final boolean shared;
        private final String name;


        public AtmosphereThreadFactory ( boolean shared, String name ) {
            this.shared = shared;
            this.name = name;
        }


        @Override
        public Thread newThread ( final Runnable runnable ) {
            Thread t = new Thread(runnable, ( this.shared ? "Atmosphere-Shared" : this.name ) + this.count.getAndIncrement()); //$NON-NLS-1$
            t.setDaemon(true);
            t.setContextClassLoader(CDIPushServlet.class.getClassLoader());
            return t;
        }
    }
}
