/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
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
 * @author mbechler
 * @param <TConfig>
 *
 */
public class FillMissingInvocationHandler <TConfig extends ConfigurationObject> implements InvocationHandler {

    private static final Logger log = Logger.getLogger(FillMissingInvocationHandler.class);
    private ObjectTypeRegistry objectRegistry;
    private TConfig delegate;
    private TConfig defaults;


    /**
     * @param objectRegistry
     * @param delegate
     * @param defaults
     */
    public FillMissingInvocationHandler ( ObjectTypeRegistry objectRegistry, TConfig delegate, TConfig defaults ) {
        Objects.requireNonNull(objectRegistry);
        Objects.requireNonNull(delegate);
        this.objectRegistry = objectRegistry;
        this.delegate = delegate;
        this.defaults = defaults;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        try {
            Object res = method.invoke(this.delegate, args);
            if ( method.getName().startsWith("get") ) { //$NON-NLS-1$
                ReferencedObject refObj = findReferencedObject(this.delegate.getType(), method);
                if ( refObj != null && ConfigurationObject.class.isAssignableFrom(method.getReturnType()) ) {
                    ConfigurationObject refCfg = (ConfigurationObject) res;
                    ConfigurationObject defCfg = null;
                    try {
                        ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>> descriptor = this.objectRegistry
                                .getConcrete((Class<? extends ConfigurationObject>) method.getReturnType());
                        defCfg = descriptor.getGlobalDefaults();
                    }
                    catch ( Exception e ) {
                        log.error("Failed to locate descriptor for object type " + method.getReturnType()); //$NON-NLS-1$
                        return res;
                    }

                    if ( refCfg == null ) {
                        // use default
                        if ( log.isDebugEnabled() ) {
                            log.debug(String.format(
                                "Injecting default for empty reference %s in %s", //$NON-NLS-1$
                                method.getName(),
                                this.delegate.getType().getName()));
                            refCfg = defCfg;
                        }

                    }
                    // proxy nested
                    return makeProxy(refCfg, defCfg, this.objectRegistry);
                }
            }

            if ( res == null && this.defaults != null ) {
                // fill in default value if it is null
                return method.invoke(this.defaults, args);
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
     * @param obj
     * @param otr
     * @return a proxied instance where null references are replaced with defaults
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    public static <TConfig extends ConfigurationObject> TConfig makeProxy ( TConfig obj, ObjectTypeRegistry otr ) throws ModelServiceException {
        return (TConfig) makeProxy(obj, otr.getConcrete(obj.getType()).getGlobalDefaults(), otr);
    }


    /**
     * @param obj
     * @param def
     * @param otr
     * @return a proxied instance where null references are replaced with defaults
     */
    @SuppressWarnings ( "unchecked" )
    public static <TConfig extends ConfigurationObject> TConfig makeProxy ( TConfig obj, @Nullable TConfig def, ObjectTypeRegistry otr ) {
        return (TConfig) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[] {
            obj.getType()
        }, new FillMissingInvocationHandler<>(otr, obj, def));
    }
}
