/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2016 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.hibernate.SessionFactory;


/**
 * @author mbechler
 *
 */
public class StatisticsInvocationHandler implements InvocationHandler {

    private DynamicEntityManagerFactoryProxy proxy;


    /**
     * @param proxy
     * 
     */
    public StatisticsInvocationHandler ( DynamicEntityManagerFactoryProxy proxy ) {
        this.proxy = proxy;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke ( Object obj, Method method, Object[] args ) throws Throwable {
        return method.invoke(this.proxy.unwrap(SessionFactory.class).getStatistics(), args);
    }

}
