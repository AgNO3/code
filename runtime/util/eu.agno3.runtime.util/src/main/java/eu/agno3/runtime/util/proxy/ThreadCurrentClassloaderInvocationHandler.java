/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2013 by mbechler
 */
package eu.agno3.runtime.util.proxy;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author mbechler
 * 
 */
public class ThreadCurrentClassloaderInvocationHandler extends AbstractDelegatingInvocationHandler {

    private ClassLoader cl;


    /**
     * @param delegate
     * @param tccl
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public ThreadCurrentClassloaderInvocationHandler ( Object delegate, ClassLoader tccl ) throws NoSuchMethodException {
        super(delegate);
        this.cl = tccl;

    }


    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * 
     * @throws Throwable
     * 
     * @see eu.agno3.runtime.util.proxy.AbstractDelegatingInvocationHandler#delegateCall(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    protected Object delegateCall ( Object proxy, Method method, Object[] args ) throws InvocationTargetException, IllegalAccessException {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.cl);

        Object res = null;
        try {
            res = method.invoke(this.getDelegate(), args);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }

        return res;
    }

}
