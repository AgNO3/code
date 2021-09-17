/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.04.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;


/**
 * @author mbechler
 *
 */
public class ListenerIntegrator implements Integrator {

    /**
     * 
     */
    private final PersistenceUnitDescriptor puDesc;
    private DynamicHibernatePersistenceProvider provider;


    /**
     * @param puDesc
     * @param provider
     */
    public ListenerIntegrator ( PersistenceUnitDescriptor puDesc, DynamicHibernatePersistenceProvider provider ) {
        this.puDesc = puDesc;
        this.provider = provider;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.integrator.spi.Integrator#integrate(org.hibernate.boot.Metadata,
     *      org.hibernate.engine.spi.SessionFactoryImplementor, org.hibernate.service.spi.SessionFactoryServiceRegistry)
     */
    @Override
    public void integrate ( Metadata meta, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry ) {
        this.provider.generatedConfiguation(this.puDesc, meta);
    }


    @Override
    public void disintegrate ( SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry ) {

    }

}