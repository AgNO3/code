/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config.cdi;


import java.lang.annotation.Annotation;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerType;
import org.osgi.framework.Bundle;


/**
 * @author mbechler
 *
 */
public class FakeCDIContainer implements CdiContainer {

    private Bundle bundle;


    /**
     * @param bundle
     */
    public FakeCDIContainer ( Bundle bundle ) {
        this.bundle = bundle;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#getBeanManager()
     */
    @Override
    public BeanManager getBeanManager () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#getBundle()
     */
    @Override
    public Bundle getBundle () {
        return this.bundle;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#getContainerType()
     */
    @Override
    public CdiContainerType getContainerType () {
        return CdiContainerType.WEB;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#getContextClassLoader()
     */
    @Override
    public ClassLoader getContextClassLoader () {
        return new BundleClassLoader(this.bundle);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#getEvent()
     */
    @Override
    public <T> Event<T> getEvent () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#getInstance()
     */
    @Override
    public Instance<Object> getInstance () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#start(java.lang.Object)
     */
    @Override
    public void start ( Object arg0 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#startContext(java.lang.Class)
     */
    @Override
    public void startContext ( Class<? extends Annotation> arg0 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#stop()
     */
    @Override
    public void stop () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#stopContext(java.lang.Class)
     */
    @Override
    public void stopContext ( Class<? extends Annotation> arg0 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainer#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap ( Class<T> arg0 ) {
        throw new UnsupportedOperationException();
    }

}
