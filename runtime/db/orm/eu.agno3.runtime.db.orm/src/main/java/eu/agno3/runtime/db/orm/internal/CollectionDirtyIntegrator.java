/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.08.2016 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;


/**
 * @author mbechler
 *
 */
public class CollectionDirtyIntegrator implements Integrator {

    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.integrator.spi.Integrator#disintegrate(org.hibernate.engine.spi.SessionFactoryImplementor,
     *      org.hibernate.service.spi.SessionFactoryServiceRegistry)
     */
    @Override
    public void disintegrate ( SessionFactoryImplementor imp, SessionFactoryServiceRegistry sr ) {}


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.integrator.spi.Integrator#integrate(org.hibernate.boot.Metadata,
     *      org.hibernate.engine.spi.SessionFactoryImplementor, org.hibernate.service.spi.SessionFactoryServiceRegistry)
     */
    @Override
    public void integrate ( Metadata m, SessionFactoryImplementor imp, SessionFactoryServiceRegistry sr ) {
        EventListenerRegistry listenerRegistry = sr.getService(EventListenerRegistry.class);
        listenerRegistry.setListeners(EventType.MERGE, new CollectionCleanEventListener());
    }
}
