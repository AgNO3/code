/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate;


import liquibase.snapshot.DatabaseSnapshot;

import org.hibernate.boot.Metadata;


/**
 * @author mbechler
 * 
 */
public interface HibernateSnapshotGenerator {

    /**
     * @param pu
     * @return a snapshot of the given hibernate persistence unit
     * @throws HibernateIndexingException
     */
    DatabaseSnapshot snapshot ( String pu ) throws HibernateIndexingException;


    /**
     * @param cfg
     * @param ownershipStrategyFactory
     * @return a snapshot of the given hibernate persistence unit
     * @throws HibernateIndexingException
     */
    DatabaseSnapshot snapshot ( Metadata cfg, HibernateOwnershipStrategyFactory ownershipStrategyFactory ) throws HibernateIndexingException;

}
