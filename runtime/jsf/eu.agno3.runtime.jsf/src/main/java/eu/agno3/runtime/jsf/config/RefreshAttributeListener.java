/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.12.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.lang.reflect.Field;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;
import javax.faces.view.facelets.FaceletCache;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.apache.log4j.Logger;
import org.apache.myfaces.application.ResourceHandlerImpl;
import org.apache.myfaces.application.ViewHandlerImpl;
import org.apache.myfaces.shared.resource.ResourceHandlerCache;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;


/**
 * @author mbechler
 *
 */
public class RefreshAttributeListener implements ServletContextAttributeListener {

    private static final Logger log = Logger.getLogger(RefreshAttributeListener.class);
    private static final String REFRESH_ATTRIBUTE = "refresh"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextAttributeListener#attributeAdded(javax.servlet.ServletContextAttributeEvent)
     */
    @Override
    public void attributeAdded ( ServletContextAttributeEvent ev ) {
        if ( REFRESH_ATTRIBUTE.equals(ev.getName()) ) {
            doRefresh(ev);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextAttributeListener#attributeRemoved(javax.servlet.ServletContextAttributeEvent)
     */
    @Override
    public void attributeRemoved ( ServletContextAttributeEvent ev ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextAttributeListener#attributeReplaced(javax.servlet.ServletContextAttributeEvent)
     */
    @Override
    public void attributeReplaced ( ServletContextAttributeEvent ev ) {
        if ( REFRESH_ATTRIBUTE.equals(ev.getName()) ) {
            doRefresh(ev);
        }
    }


    /**
     * 
     */
    private static void doRefresh ( ServletContextAttributeEvent ev ) {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ev.getServletContext().getClassLoader());
            ApplicationFactory f = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            if ( f == null ) {
                log.warn("Application factory not found"); //$NON-NLS-1$
                return;
            }

            Application app = f.getApplication();
            if ( app == null ) {
                log.warn("Application not found"); //$NON-NLS-1$
                return;
            }

            clearResourceCache(app);
            clearFaceletCache(app);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * @param app
     */
    private static void clearFaceletCache ( Application app ) {
        try {

            // nasty, but no other way
            ViewHandler viewHandler = app.getViewHandler();

            while ( viewHandler instanceof ViewHandlerWrapper ) {
                viewHandler = ( (ViewHandlerWrapper) viewHandler ).getWrapped();
            }

            if ( ! ( viewHandler instanceof ViewHandlerImpl ) ) {
                log.debug("Wrong type"); //$NON-NLS-1$
                return;
            }

            Field vdlfField = viewHandler.getClass().getDeclaredField("_vdlFactory"); //$NON-NLS-1$
            vdlfField.setAccessible(true);
            ViewDeclarationLanguageFactory vdlf = (ViewDeclarationLanguageFactory) vdlfField.get(viewHandler);

            Field initField = vdlf.getClass().getDeclaredField("_initialized"); //$NON-NLS-1$
            initField.setAccessible(true);
            boolean initializedVDL = initField.getBoolean(vdlf);

            if ( !initializedVDL ) {
                log.debug("Uninitialized"); //$NON-NLS-1$
                return;
            }

            ViewDeclarationLanguage vdl = vdlf.getViewDeclarationLanguage("refresh.xhtml"); //$NON-NLS-1$

            if ( ! ( vdl instanceof FaceletViewDeclarationLanguage ) ) {
                log.debug("Not found"); //$NON-NLS-1$
                return;
            }

            Field faceletFactoryField = vdl.getClass().getDeclaredField("_faceletFactory"); //$NON-NLS-1$
            faceletFactoryField.setAccessible(true);
            FaceletFactory f = (FaceletFactory) faceletFactoryField.get(vdl);
            Field faceletCacheField = f.getClass().getDeclaredField("_faceletCache"); //$NON-NLS-1$
            faceletCacheField.setAccessible(true);
            FaceletCache<?> faceletCache = (FaceletCache<?>) faceletCacheField.get(f);

            Field cacheField = faceletCache.getClass().getDeclaredField("_facelets"); //$NON-NLS-1$
            cacheField.setAccessible(true);
            Map<?, ?> fcache = (Map<?, ?>) cacheField.get(faceletCache);
            fcache.clear();

            Field metadataCacheField = faceletCache.getClass().getDeclaredField("_viewMetadataFacelets"); //$NON-NLS-1$
            metadataCacheField.setAccessible(true);
            Map<?, ?> fmcache = (Map<?, ?>) cacheField.get(faceletCache);
            fmcache.clear();
        }
        catch (
            NoSuchFieldException |
            SecurityException |
            IllegalArgumentException |

            IllegalAccessException e )

        {
            log.warn("Failed to access facelet cache", e); //$NON-NLS-1$
        }

    }


    /**
     * @param app
     */
    private static void clearResourceCache ( Application app ) {
        ResourceHandler resourceHandler = app.getResourceHandler();

        while ( resourceHandler instanceof ResourceHandlerWrapper ) {
            resourceHandler = ( (ResourceHandlerWrapper) resourceHandler ).getWrapped();
        }

        if ( ! ( resourceHandler instanceof ResourceHandlerImpl ) ) {
            log.warn("Root resource handler not found"); //$NON-NLS-1$
            return;
        }

        ResourceHandlerImpl impl = (ResourceHandlerImpl) resourceHandler;
        ResourceHandlerCache cache = new ResourceHandlerCache();

        try {
            // nasty, but no other way
            Field cacheField = impl.getClass().getDeclaredField("_resourceHandlerCache"); //$NON-NLS-1$
            cacheField.setAccessible(true);
            cacheField.set(impl, cache);
        }
        catch (
            NoSuchFieldException |
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException e ) {
            log.warn("Failed to access resource cache", e); //$NON-NLS-1$
        }
    }

}
