/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.util.Collection;

import javax.persistence.EntityManagerFactory;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.structure.DatabaseObject;

import org.hibernate.boot.Metadata;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationRegistry;
import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateIndexingException;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateSnapshotGenerator;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    SnapshotGenerator.class, HibernateSnapshotGenerator.class
} )
public class HibernateSnapshotGeneratorImpl implements SnapshotGenerator, HibernateSnapshotGenerator {

    private ComponentContext context;
    private HibernateConfigurationRegistry configRegistry;
    private HibernateOwnershipStrategyFactory ownershipStrategyFactory;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.context = ctx;
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.context = null;
    }


    @Reference
    protected synchronized void setConfigRegistry ( HibernateConfigurationRegistry reg ) {
        this.configRegistry = reg;
    }


    protected synchronized void unsetConfigRegistry ( HibernateConfigurationRegistry reg ) {
        if ( this.configRegistry == reg ) {
            this.configRegistry = null;
        }
    }


    @Reference
    protected synchronized void setOwnershipStrategyFactory ( HibernateOwnershipStrategyFactory hosf ) {
        this.ownershipStrategyFactory = hosf;
    }


    protected synchronized void unsetOwnershipStrategyFactory ( HibernateOwnershipStrategyFactory hosf ) {
        if ( this.ownershipStrategyFactory == hosf ) {
            this.ownershipStrategyFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.snapshot.SnapshotGenerator#addsTo()
     */
    @Override
    public Class<? extends DatabaseObject>[] addsTo () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.snapshot.SnapshotGenerator#replaces()
     */
    @Override
    public Class<? extends SnapshotGenerator>[] replaces () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.snapshot.SnapshotGenerator#getPriority(java.lang.Class, liquibase.database.Database)
     */
    @Override
    public int getPriority ( Class<? extends DatabaseObject> arg0, Database arg1 ) {
        if ( ! ( arg1 instanceof HibernateDatabase ) ) {
            return -1;
        }

        return Integer.MAX_VALUE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.snapshot.SnapshotGenerator#snapshot(liquibase.structure.DatabaseObject,
     *      liquibase.snapshot.DatabaseSnapshot, liquibase.snapshot.SnapshotGeneratorChain)
     */
    @Override
    public <T extends DatabaseObject> T snapshot ( T example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain ) throws DatabaseException,
            InvalidExampleException {

        Database db = snapshot.getDatabase();

        if ( ! ( db instanceof HibernateDatabase ) ) {
            throw new IllegalArgumentException("Hibernate snapshot generator can only handle hibernate databases"); //$NON-NLS-1$
        }

        return example;
    }


    @Override
    public DatabaseSnapshot snapshot ( String pu ) throws HibernateIndexingException {
        Metadata cfg = null;
        if ( !this.configRegistry.hasMetadata(pu) ) {
            try {
                FilterExpression filter = FilterBuilder.get().eq("persistenceUnit", pu); //$NON-NLS-1$
                Collection<ServiceReference<EntityManagerFactory>> refs = this.context.getBundleContext().getServiceReferences(
                    EntityManagerFactory.class,
                    filter.toString());

                if ( refs.isEmpty() ) {
                    throw new SchemaException("Failed to obtain EntityManagerFactory for persistence unit " + pu); //$NON-NLS-1$
                }

                ServiceReference<EntityManagerFactory> ref = refs.iterator().next();
                EntityManagerFactory emf = this.context.getBundleContext().getService(ref);
                emf.createEntityManager();

                cfg = this.configRegistry.getMetadata(pu);
            }
            catch (
                SchemaException |
                InvalidSyntaxException e ) {
                throw new HibernateIndexingException("Failed to refresh configuration:", e); //$NON-NLS-1$
            }
        }
        else {
            cfg = this.configRegistry.getMetadata(pu);
        }

        if ( cfg == null ) {
            throw new HibernateIndexingException("Hibernate Configuration is null"); //$NON-NLS-1$
        }

        return snapshot(cfg, this.ownershipStrategyFactory);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.schema.orm.hibernate.HibernateSnapshotGenerator#snapshot(org.hibernate.boot.Metadata,
     *      eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory)
     */
    @Override
    public DatabaseSnapshot snapshot ( Metadata cfg, HibernateOwnershipStrategyFactory ownershipStrategy ) throws HibernateIndexingException {
        HibernateDatabase db = new HibernateDatabase(cfg, ownershipStrategy);

        try {
            HibernateDatabaseSnapshot snap = new HibernateDatabaseSnapshot(db);

            snap.includeAll();

            return snap;
        }
        catch ( LiquibaseException e ) {
            throw new HibernateIndexingException("Failed to setup hibernate snapshot:", e); //$NON-NLS-1$
        }
    }
}
