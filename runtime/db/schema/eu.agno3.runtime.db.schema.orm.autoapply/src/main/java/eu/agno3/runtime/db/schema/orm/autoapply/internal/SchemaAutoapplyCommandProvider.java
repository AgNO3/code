/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.autoapply.internal;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.hibernate.boot.Metadata;
import org.hibernate.mapping.PersistentClass;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;
import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationRegistry;
import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.diff.SchemaDiffException;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateIndexingException;
import eu.agno3.runtime.db.schema.orm.hibernate.ModularChangeSetGenerator;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;
import eu.agno3.runtime.util.classloading.CompositeClassLoader;

import liquibase.diff.DiffResult;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;


/**
 * @author mbechler
 *
 */
@Component ( service = CommandProvider.class )
public class SchemaAutoapplyCommandProvider implements CommandProvider {

    /**
     * 
     */
    private static final String PU_PROPERTY = "persistenceUnit"; //$NON-NLS-1$

    private SchemaAutoApplicator applicator;
    private HibernateConfigurationRegistry ormConfReg;
    private ComponentContext context;
    private ModularChangeSetGenerator modularChangeGen;


    @Reference
    protected synchronized void setSchemaAutoApplicator ( SchemaAutoApplicator app ) {
        this.applicator = app;
    }


    protected synchronized void unsetSchemaAutoApplicator ( SchemaAutoApplicator app ) {
        if ( this.applicator == app ) {
            this.applicator = null;
        }
    }


    @Reference
    protected synchronized void setModularChangeGen ( ModularChangeSetGenerator gen ) {
        this.modularChangeGen = gen;
    }


    protected synchronized void unsetModularChangeGen ( ModularChangeSetGenerator gen ) {
        if ( this.modularChangeGen == gen ) {
            this.modularChangeGen = null;
        }
    }


    @Reference
    protected synchronized void setHibernateConfigRegistry ( HibernateConfigurationRegistry reg ) {
        this.ormConfReg = reg;
    }


    protected synchronized void unsetHibernateConfigRegistry ( HibernateConfigurationRegistry reg ) {
        if ( this.ormConfReg == reg ) {
            this.ormConfReg = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.context = ctx;
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.context = null;
    }


    /**
     * @return the applicator
     */
    SchemaAutoApplicator getApplicator () {
        return this.applicator;
    }


    ComponentContext getComponentContext () {
        return this.context;
    }


    /**
     * @return the ormConfReg
     */
    HibernateConfigurationRegistry getOrmConfReg () {
        return this.ormConfReg;
    }


    /**
     * @return the modularChangeGen
     */
    public ModularChangeSetGenerator getModularChangeGen () {
        return this.modularChangeGen;
    }


    /**
     * @param pu
     * @return
     * @throws InvalidSyntaxException
     */
    PersistenceUnitDescriptor getPersistenceUnitDescriptor ( String pu ) throws InvalidSyntaxException {
        FilterBuilder fb = FilterBuilder.get();

        FilterExpression puFilter = fb.eq(PU_PROPERTY, pu);

        Collection<ServiceReference<PersistenceUnitDescriptor>> puDescs = this.context.getBundleContext()
                .getServiceReferences(PersistenceUnitDescriptor.class, puFilter.toString());

        if ( puDescs.isEmpty() ) {
            throw new IllegalStateException("Persistence unit descriptor not found for " + pu); //$NON-NLS-1$
        }

        ServiceReference<PersistenceUnitDescriptor> puDescRef = puDescs.iterator().next();
        PersistenceUnitDescriptor puDesc = this.context.getBundleContext().getService(puDescRef);

        if ( puDesc == null ) {
            throw new IllegalArgumentException("Cannot locate PersistenceUnitDescriptor service"); //$NON-NLS-1$
        }

        return puDesc;
    }

    /**
     * Show schema change sets for a datasource
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "orm", name = "changesets", description = "Show changesets against current changelogs" )
    public class ChangeSetsCommand implements Action {

        @Argument ( index = 0, name = "persistenceUnit", required = true, description = "PU to show changesets for" )
        private String pu;

        @Argument ( index = 1, name = "filter", required = false, description = "Filter changeset from bundle" )
        private String filter;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws LiquibaseException, SchemaException, SQLException, InvalidSyntaxException, ParserConfigurationException,
                IOException, HibernateIndexingException, SchemaDiffException {
            Metadata cfg = getOrmConfReg().getMetadata(this.pu);
            PersistenceUnitDescriptor puInfo = getPersistenceUnitDescriptor(this.pu);

            Collection<PersistentClass> classMappings = cfg.getEntityBindings();
            Set<ClassLoader> classloaders = new HashSet<>();
            for ( PersistentClass cl : classMappings ) {
                Class<?> clz = cl.getMappedClass();
                if ( clz != null ) {
                    classloaders.add(clz.getClassLoader());
                }
            }
            CompositeClassLoader comp = new CompositeClassLoader(classloaders);
            ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

            try ( NoClosePrintStream out = new NoClosePrintStream(this.session.getConsole()) ) {
                Thread.currentThread().setContextClassLoader(comp);
                DatabaseSnapshot snap = getApplicator().makeHibernateSnapshot(cfg);
                String dataSourceName = puInfo.getDataSourceName();

                DiffResult r = getApplicator().getSchemaDiffService().diffToCurrentChangeSet(dataSourceName, snap);

                Map<String, DiffResult> modular = getModularChangeGen().splitDiff(r);

                boolean changed = false;

                DiffOutputControl control = new DiffOutputControl(true, true, true, r.getCompareControl().getSchemaComparisons());
                for ( Entry<String, DiffResult> diffEntry : modular.entrySet() ) {
                    if ( this.filter != null && !this.filter.equals(diffEntry.getKey()) ) {
                        continue;
                    }

                    DiffResult diff = new NoRemoveDiffWrapper(diffEntry.getValue());
                    DiffToChangeLog dtc = new DiffToChangeLog(diff, control);

                    if ( isEmpty(diff.getChangedObjects()) && diff.getMissingObjects().isEmpty() && diff.getUnexpectedObjects().isEmpty() ) {
                        continue;
                    }

                    this.session.getConsole().println("Changes for " + diffEntry.getKey()); //$NON-NLS-1$

                    final XMLChangeLogSerializer xmlSerializer = new XMLChangeLogSerializer();

                    dtc.print(out, xmlSerializer);
                    changed = true;
                }

                if ( !changed ) {
                    this.session.getConsole().println("All current"); //$NON-NLS-1$
                }

                return null;
            }
            finally {
                Thread.currentThread().setContextClassLoader(oldTCCL);
            }
        }


        /**
         * @param changedObjects
         * @return
         */
        private boolean isEmpty ( Map<DatabaseObject, ObjectDifferences> changedObjects ) {
            for ( ObjectDifferences diff : changedObjects.values() ) {
                for ( Difference difference : diff.getDifferences() ) {
                    if ( !"owner".equals(difference.getField()) ) { //$NON-NLS-1$
                        return false;
                    }
                }
            }
            return true;
        }

    }
}
