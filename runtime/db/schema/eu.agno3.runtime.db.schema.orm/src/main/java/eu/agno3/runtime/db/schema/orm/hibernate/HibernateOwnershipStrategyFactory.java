/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate;


import org.hibernate.boot.Metadata;


/**
 * @author mbechler
 * 
 */
public interface HibernateOwnershipStrategyFactory {

    /**
     * Initialize the ownership strategy
     * 
     * @param cfg
     * @return an ownership strategy implementation
     */
    HibernateOwnershipStrategy createStrategy ( Metadata cfg );

}
