/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate;


import org.hibernate.boot.Metadata;


/**
 * @author mbechler
 * 
 */
public class NOPHibernateOwnershipStrategyFactory implements HibernateOwnershipStrategyFactory {

    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory#createStrategy(org.hibernate.boot.Metadata)
     */
    @Override
    public HibernateOwnershipStrategy createStrategy ( Metadata cfg ) {
        return new NOPHibernateOwnershipStrategy();
    }

}
