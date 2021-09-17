/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.internal.hibernate.ownership;


import org.hibernate.boot.Metadata;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategy;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    HibernateOwnershipStrategyFactory.class
} )
public class HibernateOwnershipStrategyFactoryImpl implements HibernateOwnershipStrategyFactory {

    private ComponentContext context;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.context = ctx;
    }


    @Deactivate
    void deactivate ( ComponentContext ctx ) {
        this.context = null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory#createStrategy(org.hibernate.boot.Metadata)
     */
    @Override
    public HibernateOwnershipStrategy createStrategy ( Metadata cfg ) {

        if ( this.context == null ) {
            throw new IllegalStateException("Factory not active"); //$NON-NLS-1$
        }

        return new HibernateOwnershipStrategyImpl(cfg, this.context);
    }

}
