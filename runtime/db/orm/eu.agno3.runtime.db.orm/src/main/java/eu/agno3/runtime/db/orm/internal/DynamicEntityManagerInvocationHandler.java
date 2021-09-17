/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class DynamicEntityManagerInvocationHandler implements InvocationHandler {

    private static final Logger log = Logger.getLogger(DynamicEntityManagerInvocationHandler.class);
    private EntityManager em;
    private DynamicEntityManagerFactoryProxy emf;
    private ClassLoader classLoader;


    /**
     * @param em
     * @param emf
     */
    public DynamicEntityManagerInvocationHandler ( EntityManager em, DynamicEntityManagerFactoryProxy emf ) {
        this.em = em;
        this.emf = emf;
        this.classLoader = this.emf.getPersistenceUnitInfo().getClassLoader();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classLoader);
            synchronized ( this.em ) {
                if ( "close".equals(method.getName()) ) { //$NON-NLS-1$
                    try {
                        throw new IllegalArgumentException();
                    }
                    catch ( IllegalArgumentException e ) {
                        log.warn("Trying to close proxied entity manager", e); //$NON-NLS-1$
                        return null;
                    }
                }

                if ( !this.em.isOpen() ) {
                    this.em = this.emf.createEntityManager();
                }
                return method.invoke(this.em, args);
            }
        }
        catch ( InvocationTargetException e ) {
            log.debug("Exception in call", e); //$NON-NLS-1$
            throw e.getCause();
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }

    }

}
