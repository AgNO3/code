/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public final class ReflectionUtil {

    private static final Logger log = Logger.getLogger(ReflectionUtil.class);


    /**
     * 
     */
    private ReflectionUtil () {}

    private static Map<Class<?>, Map<String, ReferencedObject>> annotationCache = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, Map<String, ReferencedObject>>());


    /**
     * Clear the annotation cache
     */
    public static void clearCache () {
        synchronized ( annotationCache ) {
            annotationCache.clear();
        }
    }


    /**
     * Check whether the given method is a reference getter and retrieves the reference anntoation
     * 
     * @param type
     * @param m
     * @return the reference annotation
     */
    public static ReferencedObject getReference ( Class<? extends ConfigurationObject> type, Method m ) {
        if ( ReflectionUtil.isPotentialReferenceGetter(m) ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Not returning configuration object %s.%s", m.getDeclaringClass().getName(), m.getName())); //$NON-NLS-1$
            }
            return null;
        }

        Map<String, ReferencedObject> cached = annotationCache.get(type);
        if ( cached != null && cached.containsKey(m.getName()) ) {
            return cached.get(m.getName());
        }

        ReferencedObject ref = ReflectionUtil.getInheritedMethodAnnotation(ReferencedObject.class, type, m, ConfigurationObject.class);
        addToAnnotationCache(type, m, ref);

        if ( ref == null ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("No referenced object annotation %s.%s", m.getDeclaringClass().getName(), m.getName())); //$NON-NLS-1$
            }
            return null;
        }

        return ref;
    }


    /**
     * @param type
     * @param m
     * @param ref
     */
    private static void addToAnnotationCache ( Class<? extends ConfigurationObject> type, Method m, ReferencedObject ref ) {
        synchronized ( annotationCache ) {
            Map<String, ReferencedObject> map = annotationCache.get(type);

            if ( map == null ) {
                map = Collections.synchronizedMap(new HashMap<String, ReferencedObject>());
                annotationCache.put(type, map);
            }

            map.put(m.getName(), ref);
        }
    }


    private static boolean isPotentialReferenceGetter ( Method m ) {
        return m.getParameterTypes().length > 0
                || ( !ConfigurationObject.class.isAssignableFrom(m.getReturnType()) && !Collection.class.isAssignableFrom(m.getReturnType()) );
    }


    /**
     * 
     * @param annot
     * @param c
     * @param m
     * @param restrictTo
     * @param parameterTypes
     * @return annotation from the method or one of it's parents
     */
    public static <T extends Annotation> @Nullable T getInheritedMethodAnnotation ( Class<T> annot, Class<?> c, Method m, Class<?> restrictTo,
            Class<?>... parameterTypes ) {

        @Nullable
        T res = m.getAnnotation(annot);

        if ( res != null ) {
            return res;
        }

        res = getInheritedFromSuperclass(annot, c, m, restrictTo, parameterTypes);

        if ( res != null ) {
            return res;
        }

        res = getInheritedFromInterfaces(annot, c, m, restrictTo, parameterTypes);

        return res;
    }


    private static <T extends Annotation> @Nullable T getInheritedFromInterfaces ( Class<T> annot, Class<?> c, Method m, Class<?> restrictTo,
            Class<?>... parameterTypes ) {
        for ( Class<?> intf : c.getInterfaces() ) {
            if ( !restrictTo.isAssignableFrom(intf) ) {
                continue;
            }
            if ( log.isTraceEnabled() ) {
                log.trace("Trying interface " + intf.getName()); //$NON-NLS-1$
            }
            try {
                Method superMethod = intf.getMethod(m.getName(), parameterTypes);
                return getInheritedMethodAnnotation(annot, intf, superMethod, restrictTo, parameterTypes);
            }
            catch ( NoSuchMethodException e ) {
                log.trace("Interface does not define the method", e); //$NON-NLS-1$
            }
        }

        return null;
    }


    private static <T extends Annotation> @Nullable T getInheritedFromSuperclass ( Class<T> annot, Class<?> c, Method m, Class<?> restrictTo,
            Class<?>... parameterTypes ) {
        Class<?> superclass = c.getSuperclass();
        if ( superclass == null || !restrictTo.isAssignableFrom(superclass) ) {
            return null;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Trying superclass " + superclass.getName()); //$NON-NLS-1$
        }
        try {
            Method superMethod = superclass.getMethod(m.getName(), parameterTypes);
            return getInheritedMethodAnnotation(annot, superclass, superMethod, restrictTo, parameterTypes);
        }
        catch ( NoSuchMethodException e ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Superclass does not define the method", e); //$NON-NLS-1$
            }
        }

        return null;
    }


    /**
     * Tries to locate a setter for a property given by getter
     * 
     * @param obj
     * @param getter
     * @return the setter method
     * @throws ModelServiceException
     */
    public static Method getCorrespondingSetter ( AbstractConfigurationObject<?> obj, Method getter ) throws ModelServiceException {
        if ( !getter.getName().startsWith("get") ) { //$NON-NLS-1$
            log.warn("Getter does not start with get " + getter.getName()); //$NON-NLS-1$
            throw new ModelServiceException("Found a reference getter that does not start with get: " + getter.getName()); //$NON-NLS-1$
        }

        String setterName = "set" + getter.getName().substring(3); //$NON-NLS-1$

        try {
            for ( Method potentialSetter : obj.getClass().getMethods() ) {
                if ( setterName.equals(potentialSetter.getName()) ) {
                    return potentialSetter;
                }
            }
        }
        catch ( SecurityException e ) {
            throw new ModelServiceException("Failure while enumerating methods for finding setter", e);//$NON-NLS-1$
        }

        String err = String.format("Could not locate setter %s.%s", obj.getClass().getName(), setterName);//$NON-NLS-1$
        log.warn(err);
        throw new ModelServiceException(err);
    }

}
