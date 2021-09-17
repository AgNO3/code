//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package eu.agno3.runtime.http.service.webapp.internal;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.jetty.osgi.boot.internal.webapp.OSGiWebappClassLoader;
import org.eclipse.jetty.osgi.boot.utils.internal.DefaultBundleClassLoaderHelper;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.Bundle;


/**
 * OSGiClassLoader
 * 
 * Class loader that is aware of a bundle. Similar to WebAppClassLoader from Jetty
 * and the OSGiWebAppClassLoader, but works without webapps.
 */
@SuppressWarnings ( "all" )
@Generated ( "thirdparty" )
public class OSGiClassLoader extends OSGiWebappClassLoader {

    private static final Logger log = Log.getLogger(OSGiClassLoader.class);

    private Bundle bundle;
    private WeakReference<ClassLoader> osgiBundleClassLoader;
    private Map<String, WeakReference<Class<?>>> classCache = new HashMap<>();
    private boolean lookInOsgiFirst = true;
    private ClassLoader parent;
    private boolean caching;


    /* ------------------------------------------------------------ */
    public OSGiClassLoader ( ClassLoader parent, WebAppContext webapp, Bundle bundle ) throws IOException {
        super(parent, webapp, bundle);
        this.parent = getParent();
        this.bundle = bundle;
        this.osgiBundleClassLoader = new WeakReference(new DefaultBundleClassLoaderHelper().getBundleClassLoader(bundle));
    }


    /* ------------------------------------------------------------ */
    /**
     * Get a resource from the classloader
     * 
     * Copied from WebAppClassLoader
     */
    @Override
    public URL getResource ( String name ) {
        URL url = null;
        boolean triedParent = false;

        if ( parent != null && !lookInOsgiFirst ) {
            triedParent = true;

            if ( parent != null )
                url = parent.getResource(name);
        }

        if ( url == null ) {
            url = osgiBundleClassLoader.get().getResource(name);

            if ( url == null && name.startsWith("/") ) {
                if ( log.isDebugEnabled() )
                    log.debug("HACK leading / off " + name);

                url = osgiBundleClassLoader.get().getResource(name.substring(1));
            }
        }

        if ( url == null && !triedParent ) {
            if ( parent != null )
                url = parent.getResource(name);
        }

        if ( url != null )
            if ( log.isDebugEnabled() )
                log.debug("getResource(" + name + ")=" + url);

        return url;
    }


    /* ------------------------------------------------------------ */
    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {
        return loadClass(name, false);
    }


    /* ------------------------------------------------------------ */
    @Override
    protected Class<?> loadClass ( String name, boolean resolve ) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        ClassNotFoundException ex = null;
        boolean triedParent = false;

        if ( this.caching && c == null ) {
            WeakReference<Class<?>> weakReference = this.classCache.get(name);
            if ( weakReference != null ) {
                Class<?> clz = weakReference.get();
                if ( clz != null ) {
                    c = clz;
                }
                else {
                    synchronized ( this ) {
                        this.classCache.remove(name);
                    }
                }
            }
        }

        synchronized ( this ) {
            if ( c == null && parent != null && !lookInOsgiFirst ) {
                triedParent = true;
                try {
                    c = parent.loadClass(name);
                    if ( log.isDebugEnabled() )
                        log.debug("loaded " + c);
                }
                catch ( ClassNotFoundException e ) {
                    ex = e;
                }
            }

            if ( c == null ) {
                try {
                    c = this.findClass(name);
                }
                catch ( ClassNotFoundException e ) {
                    ex = e;
                }
            }

            if ( c == null && parent != null && !triedParent )
                c = parent.loadClass(name);

            if ( c == null && ex != null )
                throw ex;

            if ( this.caching ) {
                this.classCache.put(name, new WeakReference<Class<?>>(c));
            }
        }

        if ( resolve )
            resolveClass(c);

        if ( log.isDebugEnabled() && c != null ) {
            log.debug("loaded " + c + " from " + c.getClassLoader());
        }

        return c;
    }


    /* ------------------------------------------------------------ */
    @Override
    public Enumeration<URL> getResources ( String name ) throws IOException {
        Enumeration<URL> osgiUrls = osgiBundleClassLoader.get().getResources(name);
        Enumeration<URL> urls = super.getResources(name);
        if ( lookInOsgiFirst ) {
            return Collections.enumeration(toList(osgiUrls, urls));
        }
        else {
            return Collections.enumeration(toList(urls, osgiUrls));
        }
    }


    /* ------------------------------------------------------------ */
    @Override
    protected Class<?> findClass ( String name ) throws ClassNotFoundException {
        try {
            return lookInOsgiFirst ? osgiBundleClassLoader.get().loadClass(name) : super.findClass(name);
        }
        catch ( ClassNotFoundException cne ) {
            try {
                return lookInOsgiFirst ? super.findClass(name) : osgiBundleClassLoader.get().loadClass(name);
            }
            catch ( ClassNotFoundException cne2 ) {
                log.debug("Class not found:", cne2);
                throw cne;
            }
        }
    }


    /* ------------------------------------------------------------ */
    /**
     * @param e
     * @param e2
     * @return
     */
    private List<URL> toList ( Enumeration<URL> e, Enumeration<URL> e2 ) {
        List<URL> list = new ArrayList<URL>();
        while ( e != null && e.hasMoreElements() )
            list.add(e.nextElement());
        while ( e2 != null && e2.hasMoreElements() )
            list.add(e2.nextElement());
        return list;
    }
}
