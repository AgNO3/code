/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning.internal;


import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.boot.internal.EnversIntegrator;
import org.hibernate.envers.boot.internal.EnversServiceContributor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    org.hibernate.integrator.spi.Integrator.class, EnversIntegrator.class
} )
public class Integrator extends EnversIntegrator {

    @Reference
    protected synchronized void setEnversService ( EnversServiceContributor e ) {
        // dep only
    }


    protected synchronized void unsetEnversService ( EnversServiceContributor e ) {
        // dep only
    }


    @Reference
    protected synchronized void setTypeContributor ( TypeContributor e ) {
        // dep only
    }


    protected synchronized void unsetTypeContributor ( TypeContributor e ) {
        // dep only
    }


    @Deactivate
    protected synchronized void deactivate () {
        super.disintegrate(null, null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.hibernate.envers.boot.internal.EnversIntegrator#disintegrate(org.hibernate.engine.spi.SessionFactoryImplementor,
     *      org.hibernate.service.spi.SessionFactoryServiceRegistry)
     */
    @Override
    public void disintegrate ( SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry ) {
        // ignore
    }
}