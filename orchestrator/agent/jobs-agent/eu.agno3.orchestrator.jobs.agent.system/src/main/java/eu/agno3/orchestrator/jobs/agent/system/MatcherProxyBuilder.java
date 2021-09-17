/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;


/**
 * @author mbechler
 * 
 */
public class MatcherProxyBuilder {

    private ClassLoader proxyClassLoader;


    /**
     * @param proxyClassLoader
     */
    public MatcherProxyBuilder ( ClassLoader proxyClassLoader ) {
        this.proxyClassLoader = proxyClassLoader;
    }


    @SuppressWarnings ( "unchecked" )
    protected <T> @Nullable T buildMatcherProxy ( Class<T> cls, List<Method> parents ) throws MatcherException {

        if ( cls.isArray() || cls.isPrimitive() ) {
            throw new MatcherException("Matcher proxy can only handle object types"); //$NON-NLS-1$
        }

        if ( cls.isInterface() ) {
            T proxy = (T) Proxy.newProxyInstance(this.proxyClassLoader, new Class[] {
                cls
            }, new CallTraceInvocationHandler(this, parents));

            if ( proxy == null ) {
                throw new MatcherException("Failed to build proxy"); //$NON-NLS-1$
            }

            return proxy;
        }

        return null;
    }


    /**
     * Apply a method call chain to an object
     * 
     * 
     * 
     * @param obj
     * @param methods
     * @return the final return value, null if any intermediate value is null
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public Object applyMethodChain ( Object obj, List<Method> methods ) throws IllegalAccessException, InvocationTargetException {
        Object curObject = obj;

        for ( Method m : methods ) {
            if ( curObject == null ) {
                return null;
            }
            curObject = m.invoke(curObject);
        }

        return curObject;
    }

    private static class CallTraceHandler {

        private static final Logger log = Logger.getLogger(CallTraceHandler.class);

        private List<Method> parents;
        private MatcherProxyBuilder builder;


        protected CallTraceHandler ( MatcherProxyBuilder builder, List<Method> parents ) {
            this.parents = parents;
            this.builder = builder;
        }


        protected Object doHandle ( Object obj, Method m, Object[] args ) throws MatcherException {
            if ( args != null && args.length != 0 ) {
                throw new MatcherException("Matcher proxy can only handle getters"); //$NON-NLS-1$
            }

            if ( log.isTraceEnabled() ) {
                log.trace("Adding method " + m.getName()); //$NON-NLS-1$
            }
            this.parents.add(m);
            Class<?> returnType = m.getReturnType();

            if ( returnType != null ) {
                return this.builder.buildMatcherProxy(returnType, this.parents);
            }

            throw new MatcherException("Method does not have a return type"); //$NON-NLS-1$
        }

    }

    /**
     * @author mbechler
     * 
     */
    private static final class CallTraceInvocationHandler extends CallTraceHandler implements InvocationHandler {

        /**
         * 
         * @param builder
         * @param parents
         */
        public CallTraceInvocationHandler ( MatcherProxyBuilder builder, List<Method> parents ) {
            super(builder, parents);
        }


        @Override
        public Object invoke ( Object proxy, Method method, Object[] args ) throws MatcherException {
            return super.doHandle(proxy, method, args);
        }

    }
}
