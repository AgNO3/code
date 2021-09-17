/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.07.2013 by mbechler
 */
package eu.agno3.runtime.util.classloading;


import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class CompositeClassLoader extends ClassLoader {

    private static final String CLASSLOADER_GARBAGE_COLLECTED = "Classloader was garbage collected, removing"; //$NON-NLS-1$
    private static final String CLASSLOADER_DIED = "Classloader died in our arms, removing"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(CompositeClassLoader.class);
    private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
        "boolean", //$NON-NLS-1$
        "float", //$NON-NLS-1$
        "int", //$NON-NLS-1$
        "long", //$NON-NLS-1$
        "byte", //$NON-NLS-1$
        "char", //$NON-NLS-1$
        "short", //$NON-NLS-1$
        "double", //$NON-NLS-1$
        "float")); //$NON-NLS-1$

    private List<WeakReference<ClassLoader>> classloaders = new ArrayList<>();

    private Map<String, WeakReference<ClassLoader>> pkgCache = new WeakHashMap<>();


    /**
     * @param classloaders
     */
    public CompositeClassLoader ( Set<ClassLoader> classloaders ) {
        for ( ClassLoader cl : classloaders ) {
            this.addClassLoader(cl);
        }
    }


    protected final void addClassLoader ( ClassLoader cl ) {
        synchronized ( this.classloaders ) {
            this.classloaders.add(0, new WeakReference<>(cl));
        }
    }


    protected void removeClassLoader ( ClassLoader remove ) {
        Set<WeakReference<ClassLoader>> toRemove = new HashSet<>();
        for ( WeakReference<ClassLoader> classLoader : this.classloaders ) {

            ClassLoader cl = classLoader.get();

            if ( cl == null ) {
                log.info(CLASSLOADER_GARBAGE_COLLECTED);
                toRemove.add(classLoader);
                continue;
            }

            if ( cl.equals(remove) ) {
                toRemove.add(classLoader);
            }
        }

        synchronized ( this.classloaders ) {
            this.classloaders.removeAll(toRemove);
        }
    }


    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {
        if ( name == null ) {
            throw new ClassNotFoundException("Class cannot be null"); //$NON-NLS-1$
        }

        int arrEnd = name.lastIndexOf('[');
        if ( ( arrEnd == name.length() - 2 ) || name.startsWith("java.") || //$NON-NLS-1$
                name.startsWith("com.sun.") || //$NON-NLS-1$
                name.startsWith("sun.") || //$NON-NLS-1$
                PRIMITIVE_TYPES.contains(name) ) {
            return Class.forName(name, false, this.getClass().getClassLoader());
        }

        if ( name.startsWith("eu.agno3.") && //$NON-NLS-1$
                name.contains(".jaxws.") ) { //$NON-NLS-1$
            // workaround exzessive CXF classloading
            throw new ClassNotFoundException(name);
        }

        int packagePos = name.lastIndexOf('.');

        if ( packagePos > 0 ) {
            String packageName = name.substring(0, packagePos);
            WeakReference<ClassLoader> cachedRef = this.pkgCache.get(packageName);

            if ( this.classloaders.contains(cachedRef) ) {
                ClassLoader cl = cachedRef.get();
                if ( cl == null ) {
                    log.info(CLASSLOADER_GARBAGE_COLLECTED);
                    this.classloaders.remove(cachedRef);
                }
                else {
                    if ( log.isDebugEnabled() ) {
                        log.trace("Loading class through package cache " + name); //$NON-NLS-1$
                    }
                    return cl.loadClass(name);
                }
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Loading class %s through %d classloaders", name, this.classloaders.size())); //$NON-NLS-1$
        }

        Set<WeakReference<ClassLoader>> toRemove = new HashSet<>();

        for ( WeakReference<ClassLoader> classLoader : this.classloaders ) {

            ClassLoader cl = classLoader.get();

            if ( cl == null ) {
                log.info(CLASSLOADER_GARBAGE_COLLECTED);
                toRemove.add(classLoader);
                continue;
            }

            try {
                Class<?> loaded = cl.loadClass(name);
                String pkgName = loaded.getPackage() != null ? loaded.getPackage().getName() : null;
                if ( pkgName != null ) {
                    this.pkgCache.put(pkgName, classLoader);
                }
                return loaded;
            }
            catch ( ClassNotFoundException notFound ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Could not load class through " + cl, notFound); //$NON-NLS-1$
                }
            }
            catch ( IllegalStateException e ) {
                // classloader is broken, remove
                log.warn(CLASSLOADER_DIED, e);
                toRemove.add(classLoader);
            }
        }

        this.classloaders.removeAll(toRemove);
        if ( log.isDebugEnabled() ) {
            log.debug("No classloader did load class " + name); //$NON-NLS-1$
        }
        throw new ClassNotFoundException(name);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream ( String name ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Loading resource as stream " + name); //$NON-NLS-1$
        }

        InputStream res = null;
        Set<WeakReference<ClassLoader>> toRemove = new HashSet<>();

        for ( WeakReference<ClassLoader> classLoader : this.classloaders ) {

            ClassLoader cl = classLoader.get();

            if ( cl == null ) {
                log.info(CLASSLOADER_GARBAGE_COLLECTED);
                toRemove.add(classLoader);
                continue;
            }

            try {
                res = cl.getResourceAsStream(name);
            }
            catch ( IllegalStateException e ) {
                log.warn(CLASSLOADER_DIED, e);
                toRemove.add(classLoader);
            }

            if ( res != null ) {
                break;
            }
        }

        this.classloaders.removeAll(toRemove);

        if ( res != null ) {
            return res;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("No classloader did load resource " + name); //$NON-NLS-1$
        }
        return null;
    }


    @Override
    public URL getResource ( String name ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Loading resource " + name); //$NON-NLS-1$
        }
        return this.getClass().getClassLoader().getResource(name);
    }


    @Override
    public Enumeration<URL> getResources ( String name ) throws IOException {
        if ( log.isTraceEnabled() ) {
            log.trace("Loading resources " + name); //$NON-NLS-1$
        }
        return this.getClass().getClassLoader().getResources(name);
    }

}
