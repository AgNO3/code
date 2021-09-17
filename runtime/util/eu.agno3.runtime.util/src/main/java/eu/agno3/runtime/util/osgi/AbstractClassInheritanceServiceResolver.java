/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.util.osgi;


import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * @param <TService>
 * @param <TObject>
 * 
 */
public abstract class AbstractClassInheritanceServiceResolver <TService, TObject> {

    private static final Logger log = Logger.getLogger(AbstractClassInheritanceServiceResolver.class);
    private Map<String, ServiceReference<TService>> cache = new ConcurrentHashMap<>();
    private BundleContext context;


    /**
     * @param context
     *            the context to set
     */
    public void setContext ( BundleContext context ) {
        this.context = context;
    }


    protected abstract String getClassProperty ();


    protected abstract Class<TObject> getObjectClass ();


    protected abstract Class<TService> getServiceClass ();


    protected @Nullable TService getServiceFor ( Class<? extends TObject> clazz ) throws InvalidSyntaxException {

        if ( this.context == null ) {
            throw new IllegalStateException("BundleContext has not been set"); //$NON-NLS-1$
        }

        Class<? extends TObject> targetClass = clazz;

        ServiceReference<TService> cachedResolverRef = this.cache.get(clazz.getName());

        if ( cachedResolverRef != null ) {
            TService resolver = this.context.getService(cachedResolverRef);
            if ( resolver != null ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Using cached service for type " + clazz.getName()); //$NON-NLS-1$
                }
                return resolver;
            }
        }

        return findServiceFor(clazz, targetClass);
    }


    /**
     * @param clazz
     * @param targetClass
     * @return
     * @throws InvalidSyntaxException
     */
    @SuppressWarnings ( "unchecked" )
    private @Nullable TService findServiceFor ( Class<? extends TObject> clazz, Class<? extends TObject> initialTarget )
            throws InvalidSyntaxException {
        Class<? extends TObject> targetClass = initialTarget;
        while ( true ) {
            String targetClassName = targetClass.getName();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Trying to find service %s for type %s", this.getServiceClass().getName(), targetClassName)); //$NON-NLS-1$
            }

            FilterExpression filter = FilterBuilder.get().eq(this.getClassProperty(), targetClassName);
            Collection<ServiceReference<TService>> refs = this.context.getServiceReferences(this.getServiceClass(), filter.toString());

            if ( refs != null && !refs.isEmpty() ) {
                ServiceReference<TService> ref = refs.iterator().next();
                TService resolver = this.context.getService(ref);

                if ( resolver != null ) {
                    return resolverFound(clazz, ref, resolver);
                }
            }

            if ( !this.getObjectClass().isAssignableFrom(targetClass.getSuperclass()) ) {
                break;
            }

            targetClass = (Class<? extends TObject>) targetClass.getSuperclass();
        }

        return null;
    }


    /**
     * @param clazz
     * @param ref
     * @param resolver
     * @return
     */
    private TService resolverFound ( Class<? extends TObject> clazz, ServiceReference<TService> ref, @NonNull TService resolver ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Found service " + resolver.getClass().getName()); //$NON-NLS-1$
        }
        this.cache.put(clazz.getName(), ref);
        return resolver;
    }
}
