/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.04.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;

import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.log4j.Logger;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.boot.spi.AdditionalJaxbMappingProducer;
import org.hibernate.boot.spi.MetadataContributor;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "deprecation" )
public class DynamicClassLoaderService extends ClassLoaderServiceImpl implements ClassLoaderService {

    /**
     * 
     */
    private static final long serialVersionUID = 5860100200805365122L;
    private static final Logger log = Logger.getLogger(DynamicClassLoaderService.class);
    private PersistenceUnitInfo info;
    private DynamicEntityManagerFactoryProxy emf;


    /**
     * @param info
     * @param emf
     */
    public DynamicClassLoaderService ( PersistenceUnitInfo info, DynamicEntityManagerFactoryProxy emf ) {
        this.info = info;
        this.emf = emf;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.service.spi.Stoppable#stop()
     */
    @Override
    public void stop () {}


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.registry.classloading.spi.ClassLoaderService#classForName(java.lang.String)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> Class<T> classForName ( String className ) {
        try {
            return (Class<T>) this.info.getClassLoader().loadClass(className);
        }
        catch ( ClassNotFoundException e ) {
            throw new ClassLoadingException("Could not load class", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.registry.classloading.spi.ClassLoaderService#locateResource(java.lang.String)
     */
    @Override
    public URL locateResource ( String name ) {
        return this.info.getClassLoader().getResource(name);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.registry.classloading.spi.ClassLoaderService#locateResourceStream(java.lang.String)
     */
    @Override
    public InputStream locateResourceStream ( String name ) {
        return this.info.getClassLoader().getResourceAsStream(name);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.registry.classloading.spi.ClassLoaderService#locateResources(java.lang.String)
     */
    @Override
    public List<URL> locateResources ( String name ) {
        try {
            return Collections.list(this.info.getClassLoader().getResources(name));
        }
        catch ( IOException e ) {
            log.debug("locateResources", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.registry.classloading.spi.ClassLoaderService#loadJavaServices(java.lang.Class)
     */
    @SuppressWarnings ( {
        "unchecked"
    } )
    @Override
    public <S> LinkedHashSet<S> loadJavaServices ( Class<S> serviceContract ) {
        LinkedHashSet<S> res = new LinkedHashSet<>();
        for ( S service : ServiceLoader.load(serviceContract, this.info.getClassLoader()) ) {
            res.add(service);
        }

        if ( MetadataContributor.class.isAssignableFrom(serviceContract) || TypeContributor.class.isAssignableFrom(serviceContract) ) {
            res.add((S) this.emf);
        }

        if ( AdditionalJaxbMappingProducer.class.isAssignableFrom(serviceContract) ) {
            res.addAll((Collection<? extends S>) this.emf.getAdditionalMappingProducers());
        }

        return res;
    }
}
