/**
OSGI * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.05.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import eu.agno3.runtime.configloader.file.ConfigFileLoader;
import eu.agno3.runtime.util.osgi.BundleUtil;
import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
public class OSGIResourceProvider {

    /**
     * 
     */
    private static final String CFGFILES_PREFIX = "/cfgfiles/"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(OSGIResourceProvider.class);

    private static final String RESOURCES_PREFIX = "/resources/"; //$NON-NLS-1$
    private static final String META_INF_RESOURCES = "/META-INF/resources/"; //$NON-NLS-1$
    private static final String WEBAPP_BASE = "/webapp/"; //$NON-NLS-1$
    private static final String WEBAPP_BASE_NOSLASH = "/webapp"; //$NON-NLS-1$
    private static final String WEB_INF = "/WEB-INF/"; //$NON-NLS-1$
    private static final Object MYFACES_BUNDLE = "org.apache.myfaces.core.bundle"; //$NON-NLS-1$

    private static final Set<String> MINIFIED_TYPES = new HashSet<>();


    static {
        MINIFIED_TYPES.add(".css"); //$NON-NLS-1$
        MINIFIED_TYPES.add(".js"); //$NON-NLS-1$
    }

    private boolean initialized = false;
    private Bundle contextBundle;
    private MultiValuedMap<String, Bundle> resourceContributions = new HashSetValuedHashMap<>();
    private Map<String, Bundle> webappContributions = new HashMap<>();

    private ConfigFileLoader configFileProvider;


    /**
     * @param contextBundle
     * @param configFileLoader
     */
    public OSGIResourceProvider ( Bundle contextBundle, ConfigFileLoader configFileLoader ) {
        this.contextBundle = contextBundle;
        this.configFileProvider = configFileLoader;
    }


    /**
     * 
     */
    public synchronized void init () {
        if ( this.initialized ) {
            return;
        }

        Set<String> libraries = new HashSet<>();
        for ( Bundle reqBundle : BundleUtil.getRequiredBundles(this.contextBundle) ) {
            if ( MYFACES_BUNDLE.equals(reqBundle.getSymbolicName()) ) {
                // let the delegate context handle myfaces resources
                continue;
            }

            libraries.clear();

            Enumeration<URL> webappData = reqBundle.findEntries(WEBAPP_BASE, "*.xhtml", true); //$NON-NLS-1$
            if ( webappData != null && webappData.hasMoreElements() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found webapp in " + reqBundle.getSymbolicName()); //$NON-NLS-1$
                }

                while ( webappData.hasMoreElements() ) {
                    URL u = webappData.nextElement();
                    if ( log.isTraceEnabled() ) {
                        log.trace("Found " + u.getPath()); //$NON-NLS-1$
                    }
                    if ( this.webappContributions.put(u.getPath(), reqBundle) != null ) {
                        log.warn("Duplicate webapp contribution " + u.getPath()); //$NON-NLS-1$
                    }
                }
            }

            Enumeration<URL> resources = reqBundle.findEntries(META_INF_RESOURCES, "*", false); //$NON-NLS-1$

            if ( resources == null || !resources.hasMoreElements() ) {
                continue;
            }

            while ( resources.hasMoreElements() ) {
                URL res = resources.nextElement();
                String libName = res.getPath().substring(META_INF_RESOURCES.length(), res.getPath().indexOf('/', META_INF_RESOURCES.length()));
                libraries.add(libName);
            }

            for ( String library : libraries ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Found library %s in %s", library, reqBundle.getSymbolicName())); //$NON-NLS-1$
                }

                this.resourceContributions.put(library, reqBundle);
            }
        }

        this.initialized = true;
    }


    /**
     * @param path
     * @param delegate
     * @return the resolved resource, or null if non existant
     * @throws MalformedURLException
     */
    public URL getResource ( String path, ExternalContext delegate ) throws MalformedURLException {
        int sepPos = path.lastIndexOf('.');
        if ( sepPos > 0 ) {
            String suffix = path.substring(sepPos);
            if ( MINIFIED_TYPES.contains(suffix) ) {
                String tpath = path.substring(0, path.length() - suffix.length()) + ".min" + suffix; //$NON-NLS-1$
                URL found = doGetResource(tpath, delegate);
                if ( found != null ) {
                    return found;
                }
                else if ( log.isDebugEnabled() ) {
                    log.debug("Not found in " + tpath); //$NON-NLS-1$
                }
            }
        }
        return doGetResource(path, delegate);
    }


    /**
     * @param path
     * @param delegate
     * @return
     * @throws MalformedURLException
     */
    private URL doGetResource ( String path, ExternalContext delegate ) throws MalformedURLException {
        if ( path != null && path.startsWith(RESOURCES_PREFIX) ) {
            String fixedPath = path.substring(RESOURCES_PREFIX.length());
            int dirSep = fixedPath.indexOf('/');
            if ( dirSep >= 0 ) {
                String libName = fixedPath.substring(0, dirSep);

                if ( "cfgfiles".equals(libName) ) { //$NON-NLS-1$
                    return serveCfgFile(fixedPath.substring(dirSep + 1));
                }

                Collection<Bundle> libraryBundles = this.resourceContributions.get(libName);

                if ( libraryBundles != null && !libraryBundles.isEmpty() ) {
                    return serveFromRequiredBundle(fixedPath, libName, libraryBundles);
                }
            }
        }

        return serveLocalOrSystemResource(path, delegate);
    }


    /**
     * @param path
     * @return
     */
    private URL serveCfgFile ( String path ) {

        if ( log.isTraceEnabled() ) {
            log.trace("Serving cfgfile " + path); //$NON-NLS-1$
        }

        try {
            return this.configFileProvider.getURL(path);
        }
        catch ( IOException e ) {
            log.warn("Failed to deliver config file", e); //$NON-NLS-1$
            return null;
        }
    }


    private URL serveLocalOrSystemResource ( String path, ExternalContext delegate ) throws MalformedURLException {
        String fixedPath = path;

        String base = WEBAPP_BASE;
        if ( path != null && path.startsWith(WEB_INF) ) {
            base = WEB_INF;
            fixedPath = path.substring(WEB_INF.length());
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Get resource %s with base %s", fixedPath, base)); //$NON-NLS-1$
        }

        if ( path != null && path.startsWith(CFGFILES_PREFIX) ) {
            return serveCfgFile(path.substring(CFGFILES_PREFIX.length()));
        }

        String fullPath = WEBAPP_BASE_NOSLASH + fixedPath;
        log.trace(fullPath);
        Bundle sourceBundle = this.webappContributions.get(fullPath);

        if ( sourceBundle == null ) {
            sourceBundle = this.contextBundle;
        }
        else if ( log.isTraceEnabled() ) {
            log.trace("Serving from " + sourceBundle.getSymbolicName()); //$NON-NLS-1$
        }

        URL u = ResourceUtil.safeFindEntry(sourceBundle, base, fixedPath);

        if ( u == null ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Not found: " + path); //$NON-NLS-1$
            }
            return delegate.getResource(FilenameUtils.concat(base, fixedPath));
        }

        return u;
    }


    private static URL serveFromRequiredBundle ( String fixedPath, String libName, Collection<Bundle> libraryBundles ) {
        URL override = ResourceUtil.safeFindEntry(FrameworkUtil.getBundle(OSGIResourceProvider.class), META_INF_RESOURCES, fixedPath);
        if ( override != null ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Overriding library %s path %s", libName, fixedPath)); //$NON-NLS-1$
            }
            return override;
        }

        for ( Bundle libBundle : libraryBundles ) {
            URL u = ResourceUtil.safeFindEntry(libBundle, META_INF_RESOURCES, fixedPath);

            if ( u != null ) {
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Serving library %s in bundle %s", libName, libBundle.getSymbolicName())); //$NON-NLS-1$
                }

                return u;
            }
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Not found in any bundle for the library " + fixedPath); //$NON-NLS-1$
        }
        return null;
    }
}
