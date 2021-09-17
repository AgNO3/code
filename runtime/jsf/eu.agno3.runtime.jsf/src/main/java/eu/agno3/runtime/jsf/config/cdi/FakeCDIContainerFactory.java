/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config.cdi;


import java.util.Collection;

import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerFactory;
import org.ops4j.pax.cdi.spi.CdiContainerListener;
import org.ops4j.pax.cdi.spi.CdiContainerType;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = CdiContainerFactory.class )
public class FakeCDIContainerFactory implements CdiContainerFactory {

    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainerFactory#addListener(org.ops4j.pax.cdi.spi.CdiContainerListener)
     */
    @Override
    public void addListener ( CdiContainerListener listener ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainerFactory#createContainer(org.osgi.framework.Bundle, java.util.Collection,
     *      org.ops4j.pax.cdi.spi.CdiContainerType)
     */
    @Override
    public CdiContainer createContainer ( Bundle bundle, Collection<Bundle> arg1, CdiContainerType type ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainerFactory#getContainer(org.osgi.framework.Bundle)
     */
    @Override
    public CdiContainer getContainer ( Bundle bundle ) {
        return new FakeCDIContainer(bundle);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainerFactory#getContainers()
     */
    @Override
    public Collection<CdiContainer> getContainers () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainerFactory#getProviderName()
     */
    @Override
    public String getProviderName () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainerFactory#removeContainer(org.osgi.framework.Bundle)
     */
    @Override
    public void removeContainer ( Bundle arg0 ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.ops4j.pax.cdi.spi.CdiContainerFactory#removeListener(org.ops4j.pax.cdi.spi.CdiContainerListener)
     */
    @Override
    public void removeListener ( CdiContainerListener arg0 ) {
        throw new UnsupportedOperationException();
    }

}
