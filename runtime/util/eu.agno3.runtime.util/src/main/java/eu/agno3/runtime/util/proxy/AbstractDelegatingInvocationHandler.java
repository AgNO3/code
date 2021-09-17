/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2013 by mbechler
 */
package eu.agno3.runtime.util.proxy;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;


/**
 * Dynamic proxy delegating all calls to a backing object
 * 
 * Adds special logic to correctly handle equals() calls.
 * 
 * @author mbechler
 * 
 */
public abstract class AbstractDelegatingInvocationHandler implements DelegatingInvocationHandler {

    private static final Logger log = Logger.getLogger(AbstractDelegatingInvocationHandler.class);

    private Object delegate;
    private Method equalsMethod;


    /**
     * @param delegate
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public AbstractDelegatingInvocationHandler ( Object delegate ) throws NoSuchMethodException {
        this.delegate = delegate;
        this.equalsMethod = Object.class.getMethod("equals", Object.class); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws Throwable
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public final Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        if ( method.equals(this.equalsMethod) ) {
            Object other = args[ 0 ];
            log.trace("Call to equals() method"); //$NON-NLS-1$

            if ( Proxy.isProxyClass(other.getClass()) ) {
                log.trace("Other object is proxy"); //$NON-NLS-1$
                InvocationHandler handler = Proxy.getInvocationHandler(other);

                if ( handler instanceof DelegatingInvocationHandler ) {
                    DelegatingInvocationHandler delegatingHandler = (DelegatingInvocationHandler) handler;
                    log.trace("Unwrapping delegated object for comparison"); //$NON-NLS-1$
                    other = delegatingHandler.getDelegate();
                }
            }

            return this.delegate.equals(other);
        }

        try {
            return this.delegateCall(proxy, method, args);
        }
        catch ( InvocationTargetException e ) {
            log.error("Exception in delegated method call:", e); //$NON-NLS-1$
            throw e.getCause();
        }
    }


    /**
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected abstract Object delegateCall ( Object proxy, Method method, Object[] args ) throws InvocationTargetException, IllegalAccessException;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.util.proxy.DelegatingInvocationHandler#getDelegate()
     */
    @Override
    public Object getDelegate () {
        return this.delegate;
    }

}
