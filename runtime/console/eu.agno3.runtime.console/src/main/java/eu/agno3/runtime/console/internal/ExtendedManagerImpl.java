/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.runtime.console.internal;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.karaf.shell.api.action.lifecycle.Init;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.console.Registry;
import org.apache.karaf.shell.impl.action.command.ManagerImpl;
import org.apache.karaf.shell.support.converter.GenericType;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class ExtendedManagerImpl extends ManagerImpl {

    private static final Logger log = Logger.getLogger(ExtendedManagerImpl.class);

    private Object context;


    /**
     * @param context
     * @param dependencies
     * @param registrations
     */
    public ExtendedManagerImpl ( Object context, Registry dependencies, Registry registrations ) {
        super(dependencies, registrations);
        this.context = context;
    }


    /**
     * @param context
     * @param dependencies
     * @param registrations
     * @param allow
     */
    public ExtendedManagerImpl ( Object context, Registry dependencies, Registry registrations, boolean allow ) {
        super(dependencies, registrations, allow);
        this.context = context;
    }


    @Override
    public <T> T instantiate ( Class<? extends T> clazz, Registry registry ) throws Exception {
        T instance;
        Constructor<? extends T> constructor;
        try {
            constructor = clazz.getConstructor(new Class[] {});
            instance = constructor.newInstance();
        }
        catch ( NoSuchMethodException e ) {
            log.trace("Failed to get contstructor", e); //$NON-NLS-1$
            constructor = clazz.getConstructor(this.context.getClass());
            instance = constructor.newInstance(this.context);
        }

        // Inject services
        for ( Class<?> cl = clazz; cl != Object.class; cl = cl.getSuperclass() ) {
            for ( Field field : cl.getDeclaredFields() ) {
                setReference(registry, instance, field);
            }
        }
        for ( Method method : clazz.getDeclaredMethods() ) {
            Init ann = method.getAnnotation(Init.class);
            if ( ann != null && method.getParameterTypes().length == 0 && method.getReturnType() == void.class ) {
                method.setAccessible(true);
                method.invoke(instance);
            }
        }
        return instance;
    }


    /**
     * @param registry
     * @param instance
     * @param field
     * @throws IllegalAccessException
     */
    protected <T> void setReference ( Registry registry, T instance, Field field ) throws IllegalAccessException {
        Reference ref = field.getAnnotation(Reference.class);
        if ( ref != null ) {
            GenericType type = new GenericType(field.getGenericType());
            Object value;
            if ( type.getRawClass() == List.class ) {
                Set<Object> set = new HashSet<>();
                set.addAll(registry.getServices(type.getActualTypeArgument(0).getRawClass()));
                value = new ArrayList<>(set);
            }
            else {
                value = registry.getService(type.getRawClass());
            }
            field.setAccessible(true);
            field.set(instance, value);
        }
    }
}
