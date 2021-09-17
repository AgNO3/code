/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.10.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import eu.agno3.runtime.util.classloading.CompositeClassLoader;


/**
 * @author mbechler
 *
 */
public class ContextClassLoaderHolder extends CompositeClassLoader {

    private static final Logger log = Logger.getLogger(ContextClassLoaderHolder.class);

    private Map<Bundle, Set<Object>> objects = new HashMap<>();


    /**
     * 
     */
    public ContextClassLoaderHolder () {
        super(new HashSet<>(Arrays.asList(ContextClassLoaderHolder.class.getClassLoader())));
    }


    /**
     * @param o
     */
    public synchronized void bindObject ( Object o ) {
        if ( o == null ) {
            return;
        }

        Class<?> cl = o.getClass();

        Bundle b = FrameworkUtil.getBundle(cl);
        if ( b == null ) {
            return;
        }

        Set<Object> objectSet;
        if ( !this.objects.containsKey(b) ) {
            objectSet = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
            this.objects.put(b, objectSet);
            if ( log.isDebugEnabled() ) {
                log.debug("Adding classloader from bundle " + b.getSymbolicName()); //$NON-NLS-1$
            }
            this.addClassLoader(cl.getClassLoader());
        }
        else {
            objectSet = this.objects.get(b);
        }

        objectSet.add(o);
    }


    /**
     * @param o
     */
    public synchronized void unbindObject ( Object o ) {
        if ( o == null ) {
            return;
        }

        Class<?> cl = o.getClass();

        Bundle b = FrameworkUtil.getBundle(cl);
        if ( b == null ) {
            return;
        }

        if ( !this.objects.containsKey(b) ) {
            return;
        }
        Set<Object> objectSet = this.objects.get(b);
        objectSet.remove(o);

        if ( objectSet.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Remove classloader, no more objects from bundle " + b.getSymbolicName()); //$NON-NLS-1$
            }
            this.removeClassLoader(cl.getClassLoader());
            this.objects.remove(b);
        }
    }

}
