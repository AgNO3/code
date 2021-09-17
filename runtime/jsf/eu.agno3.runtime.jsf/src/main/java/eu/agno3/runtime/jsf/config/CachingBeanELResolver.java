/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 22, 2016 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.el.BeanELResolver;
import javax.el.ELContext;


/**
 * @author mbechler
 *
 */
public class CachingBeanELResolver extends BeanELResolver {

    private Map<Class<?>, Method[]> classMethodCache = new ConcurrentHashMap<>();


    @Override
    public Object invoke ( ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params ) {
        if ( context == null ) {
            throw new NullPointerException();
        }
        Class<?>[] cachedParamTypes = paramTypes;
        if ( cachedParamTypes == null && method != null ) {
            Method findMethod = findMethod(base, method.toString(), paramTypes, params != null ? params.length : 0);
            if ( findMethod != null ) {
                cachedParamTypes = findMethod.getParameterTypes();
            }
        }
        return super.invoke(context, base, method, cachedParamTypes, params);
    };


    private static Method findAccessibleMethod ( Method method ) {
        if ( method == null || method.isAccessible() ) {
            return method;
        }
        try {
            method.setAccessible(true);
        }
        catch ( SecurityException e ) {
            for ( Class<?> cls : method.getDeclaringClass().getInterfaces() ) {
                Method mth = null;
                try {
                    mth = cls.getMethod(method.getName(), method.getParameterTypes());
                    mth = findAccessibleMethod(mth);
                    if ( mth != null ) {
                        return mth;
                    }
                }
                catch ( NoSuchMethodException ignore ) {
                    // do nothing
                }
            }
            Class<?> cls = method.getDeclaringClass().getSuperclass();
            if ( cls != null ) {
                Method mth = null;
                try {
                    mth = cls.getMethod(method.getName(), method.getParameterTypes());
                    mth = findAccessibleMethod(mth);
                    if ( mth != null ) {
                        return mth;
                    }
                }
                catch ( NoSuchMethodException ignore ) {
                    // do nothing
                }
            }
            return null;
        }
        return method;
    }


    private Method findMethod ( Object base, String name, Class<?>[] types, int paramCount ) {
        if ( types != null ) {
            try {
                return findAccessibleMethod(base.getClass().getMethod(name, types));
            }
            catch ( NoSuchMethodException e ) {
                return null;
            }
        }
        Method varArgsMethod = null;
        for ( Method method : getClassMethods(base.getClass()) ) {
            if ( method.getName().equals(name) ) {
                int formalParamCount = method.getParameterTypes().length;
                if ( method.isVarArgs() && paramCount >= formalParamCount - 1 ) {
                    varArgsMethod = method;
                }
                else if ( paramCount == formalParamCount ) {
                    return findAccessibleMethod(method);
                }
            }
        }
        return varArgsMethod == null ? null : findAccessibleMethod(varArgsMethod);
    }


    /**
     * @param klass
     * @return
     */
    protected final Method[] getClassMethods ( Class<?> klass ) {
        Method[] cached = this.classMethodCache.get(klass);
        if ( cached != null ) {
            return cached;
        }
        cached = klass.getMethods();
        this.classMethodCache.put(klass, cached);
        return cached;
    }

}
