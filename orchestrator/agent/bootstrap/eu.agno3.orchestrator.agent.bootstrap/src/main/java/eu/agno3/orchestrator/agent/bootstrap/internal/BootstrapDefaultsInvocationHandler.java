/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2016 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * 
 * This does not really follow our normal inheritance logic
 * 
 * But for bootstrapping it should be sufficient to achieve a valid configuration.
 * 
 * @author mbechler
 * @param <TConfig>
 *
 */
public class BootstrapDefaultsInvocationHandler <TConfig extends ConfigurationObject> implements InvocationHandler {

    private static final Logger log = Logger.getLogger(BootstrapDefaultsInvocationHandler.class);
    private ObjectTypeRegistry objectRegistry;
    private TConfig delegate;
    private TConfig defaults;


    /**
     * @param objectRegistry
     * @param delegate
     * @param defaults
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    public BootstrapDefaultsInvocationHandler ( ObjectTypeRegistry objectRegistry, TConfig delegate, TConfig defaults ) throws ModelServiceException {
        Objects.requireNonNull(objectRegistry);
        Objects.requireNonNull(delegate);
        this.objectRegistry = objectRegistry;
        this.delegate = delegate;
        try {
            if ( defaults == null ) {
                this.defaults = (TConfig) objectRegistry.getConcrete(delegate.getType()).getGlobalDefaults();
            }
            else {
                this.defaults = defaults;
            }
        }
        catch ( ModelServiceException e ) {
            log.warn("Failed to get defaults for " + delegate.getType()); //$NON-NLS-1$
            log.debug("Default lookup failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        try {
            Object res = method.invoke(this.delegate, args);
            Object defVal = this.defaults != null ? method.invoke(this.defaults, args) : null;
            if ( "getRevision".equals(method.getName()) ) { //$NON-NLS-1$
                return -1L;
            }
            else if ( method.getName().startsWith("get") ) { //$NON-NLS-1$
                ReferencedObject refObj = findReferencedObject(this.delegate.getType(), method);
                if ( refObj != null && ConfigurationObject.class.isAssignableFrom(method.getReturnType()) ) {
                    return proxyValueOrDefault(method, res, defVal);
                }
                else if ( refObj != null && res instanceof Collection ) {
                    return proxyCollection(res, defVal);
                }

                if ( res == null || ( res instanceof Collection && ( (Collection<?>) res ).isEmpty() )
                        || ( res instanceof Map && ( (Map<?, ?>) res ).isEmpty() ) ) {

                    return defVal;
                }
            }

            return res;
        }
        catch ( UndeclaredThrowableException e ) {
            if ( e.getCause() instanceof InvocationTargetException ) {
                throw e.getCause().getCause();
            }
            throw e;
        }
    }


    /**
     * @param method
     * @param res
     * @return
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private Object proxyValueOrDefault ( Method method, Object res, Object def ) throws ModelServiceException {
        ConfigurationObject refCfg = (ConfigurationObject) res;
        ConfigurationObject defCfg = (ConfigurationObject) def;
        if ( refCfg == null && defCfg == null ) {
            // use global default
            try {
                ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>> descriptor = this.objectRegistry
                        .getConcrete((Class<? extends ConfigurationObject>) method.getReturnType());
                refCfg = descriptor.getGlobalDefaults();
                if ( log.isDebugEnabled() ) {
                    log.debug("Using global default for " + refCfg.getType()); //$NON-NLS-1$
                }
            }
            catch ( Exception e ) {
                log.error("Failed to locate descriptor for object type " + method.getReturnType()); //$NON-NLS-1$
                return null;
            }
        }
        else if ( refCfg == null ) {
            refCfg = defCfg;
        }
        // proxy nested
        return makeProxy(refCfg, defCfg, this.objectRegistry);
    }


    /**
     * @param res
     * @param defVal
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private Object proxyCollection ( Object res, Object defVal ) throws InstantiationException, IllegalAccessException, ModelServiceException {
        Collection<ConfigurationObject> wrapped = (Collection<ConfigurationObject>) res.getClass().newInstance();

        Collection<ConfigurationObject> vC = (Collection<ConfigurationObject>) res;
        Collection<ConfigurationObject> defC = (Collection<ConfigurationObject>) defVal;

        Collection<ConfigurationObject> tgt;
        if ( vC.isEmpty() ) {
            tgt = defC;
        }
        else {
            tgt = vC;
        }

        for ( ConfigurationObject cf : tgt ) {
            wrapped.add(makeProxy(cf, this.objectRegistry));
        }
        return wrapped;
    }


    /**
     * @param method
     * @return
     */
    @SuppressWarnings ( "null" )
    @Nullable
    ReferencedObject findReferencedObject ( Class<?> type, Method method ) {
        return getInheritedMethodAnnotation(ReferencedObject.class, type, method, ConfigurationObject.class);
    }


    private static <@Nullable T extends Annotation> T getInheritedMethodAnnotation ( Class<T> annot, Class<?> c, Method m, Class<?> restrictTo,
            Class<?>... parameterTypes ) {

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


    private static <@Nullable T extends Annotation> T getInheritedFromInterfaces ( Class<T> annot, Class<?> c, Method m, Class<?> restrictTo,
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


    private static <@Nullable T extends Annotation> T getInheritedFromSuperclass ( Class<T> annot, Class<?> c, Method m, Class<?> restrictTo,
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
     * 
     * @param obj
     * @param otr
     * @return a proxied instance where defaults are included
     * @throws IllegalArgumentException
     * @throws ModelServiceException
     */
    public static <TConfig extends ConfigurationObject> TConfig makeProxy ( TConfig obj, ObjectTypeRegistry otr )
            throws IllegalArgumentException, ModelServiceException {
        return makeProxy(obj, null, otr);
    }


    /**
     * @param obj
     * @param def
     * @param otr
     * @return a proxied instance where defaults are included
     * @throws ModelServiceException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings ( "unchecked" )
    public static <TConfig extends ConfigurationObject> TConfig makeProxy ( TConfig obj, @Nullable TConfig def, ObjectTypeRegistry otr )
            throws IllegalArgumentException, ModelServiceException {
        return (TConfig) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[] {
            obj.getType()
        }, new BootstrapDefaultsInvocationHandler<>(otr, obj, def));
    }
}
