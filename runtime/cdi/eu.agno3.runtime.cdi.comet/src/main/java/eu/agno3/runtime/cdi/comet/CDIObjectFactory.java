/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.runtime.cdi.comet;


import java.util.Iterator;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.log4j.Logger;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.inject.AtmosphereProducers;
import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 *
 */
public class CDIObjectFactory implements AtmosphereObjectFactory<Object> {

    private static final Logger log = Logger.getLogger(CDIObjectFactory.class);

    private BeanManager bm;


    /**
     * @param bm
     * 
     */
    public CDIObjectFactory ( BeanManager bm ) {
        this.bm = bm;
    }


    @Override
    @SuppressWarnings ( {
        "unchecked", "null"
    } )
    public @NonNull <T, U extends T> U newClassInstance ( Class<T> classType, Class<U> classToInstantiate ) throws InstantiationException,
            IllegalAccessException {
        CreationalContext<U> cc = null;

        if ( classToInstantiate == null ) {
            throw new InstantiationException("null"); //$NON-NLS-1$
        }

        try {
            final Iterator<Bean<?>> i = this.bm.getBeans(classToInstantiate).iterator();
            if ( !i.hasNext() ) {
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Unable to find %s. Creating the object directly.", classToInstantiate.getName())); //$NON-NLS-1$
                }
                return classToInstantiate.newInstance();
            }
            Bean<U> bean = (Bean<U>) i.next();
            cc = this.bm.createCreationalContext(bean);
            return (U) this.bm.getReference(bean, classToInstantiate, cc);
        }
        catch ( Exception e ) {
            log.error(String.format("Unable to construct %s. Creating the object directly.", classToInstantiate.getName()), e); //$NON-NLS-1$
            return classToInstantiate.newInstance();
        }
        finally {
            if ( cc != null ) {
                cc.release();
            }
        }
    }


    @Override
    public void configure ( AtmosphereConfig config ) {
        try {
            AtmosphereProducers p = newClassInstance(AtmosphereProducers.class, AtmosphereProducers.class);
            p.configure(config);
        }
        catch ( Exception e ) {
            log.error("Failed to configure atmosphere CDI injection", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.atmosphere.cpr.AtmosphereObjectFactory#allowInjectionOf(java.lang.Object)
     */
    @Override
    public AtmosphereObjectFactory allowInjectionOf ( Object obj ) {
        return this;
    }

}
