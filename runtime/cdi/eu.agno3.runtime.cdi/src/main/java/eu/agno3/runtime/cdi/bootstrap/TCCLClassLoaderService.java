/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.05.2014 by mbechler
 */
package eu.agno3.runtime.cdi.bootstrap;


import org.apache.webbeans.spi.ApplicationBoundaryService;


/**
 * @author mbechler
 * 
 */
public class TCCLClassLoaderService implements ApplicationBoundaryService {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.webbeans.spi.ApplicationBoundaryService#getApplicationClassLoader()
     */
    @Override
    public ClassLoader getApplicationClassLoader () {
        return this.getClass().getClassLoader();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.webbeans.spi.ApplicationBoundaryService#getBoundaryClassLoader(java.lang.Class)
     */
    @Override
    public ClassLoader getBoundaryClassLoader ( Class clazz ) {
        return Thread.currentThread().getContextClassLoader();
    }
}
