/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.console;


import java.io.PrintStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.db.schema.ChangeFileProvider;
import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;
import eu.agno3.runtime.db.schema.SchemaManager;
import eu.agno3.runtime.db.schema.SchemaRegistration;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;

import liquibase.changelog.ChangeLogIterator;
import liquibase.exception.LiquibaseException;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class SchemaCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(SchemaCommandProvider.class);

    private ComponentContext context;
    private ChangeFileProvider changeFileProvider;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.context = ctx;
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.context = null;
    }


    @Reference
    protected synchronized void setChangeFileProvider ( ChangeFileProvider provider ) {
        this.changeFileProvider = provider;
    }


    protected synchronized void unsetChangeFileProvider ( ChangeFileProvider provider ) {
        if ( this.changeFileProvider == provider ) {
            this.changeFileProvider = null;
        }
    }


    synchronized ChangeFileProvider getChangeFileProvider () {
        return this.changeFileProvider;
    }


    ComponentContext getComponentContext () {
        return this.context;
    }


    SchemaManager getSchemaManagerFor ( String dataSource ) throws InvalidSyntaxException {
        FilterExpression filter = FilterBuilder.get().eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dataSource);
        Collection<ServiceReference<SchemaManager>> sm = getComponentContext().getBundleContext()
                .getServiceReferences(SchemaManager.class, filter.toString());
        return getComponentContext().getBundleContext().getService(sm.iterator().next());
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }

    /**
     * Show registered changelog contributions
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "db", name = "changelogs", description = "Show registered changelogs" )
    public class ChangeLogsCommand implements Action {

        @Argument ( index = 0, name = "dataSource", required = false, description = "DataSource to show changelogs for" )
        @Completion ( DataSourceCompleter.class )
        private String dataSource;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws InvalidSyntaxException {

            if ( this.dataSource != null ) {
                this.showDs(this.session.getConsole(), this.dataSource);
            }
            else {
                Set<String> schemaManagedDs = new HashSet<>();
                Collection<ServiceReference<SchemaManagedDataSource>> dsRefs = getComponentContext().getBundleContext()
                        .getServiceReferences(SchemaManagedDataSource.class, null);

                for ( ServiceReference<SchemaManagedDataSource> ref : dsRefs ) {
                    schemaManagedDs.add((String) ref.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME));
                }

                for ( String managedDs : schemaManagedDs ) {
                    this.showDs(this.session.getConsole(), managedDs);
                    this.session.getConsole().println();
                }
            }

            return null;
        }


        private void showDs ( PrintStream s, String dsName ) {
            s.print(Ansi.ansi().a("DataSource ") //$NON-NLS-1$
                    .bold().a(dsName).boldOff().a(":").newline()); //$NON-NLS-1$

            SortedMap<URL, SchemaRegistration> regs = getChangeFileProvider().getChangeFiles(dsName, false);

            if ( regs.isEmpty() ) {
                s.println("  No registrations"); //$NON-NLS-1$
                return;
            }

            for ( Entry<URL, SchemaRegistration> reg : regs.entrySet() ) {
                Ansi out = Ansi.ansi();
                Bundle b = reg.getValue().getBundle();

                out.a("  Bundle ").bold().a(b.getSymbolicName()).boldOff(). //$NON-NLS-1$
                        a(String.format(" [%d]", b.getBundleId())) //$NON-NLS-1$
                        .a(" registers ").a(reg.getKey().toString()); //$NON-NLS-1$

                s.println(out.toString());
            }

        }

    }

    /**
     * Show schema change sets for a datasource
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "db", name = "changesets", description = "Show changesets" )
    public class ChangeSetsCommand implements Action {

        @Option ( name = "-a", aliases = "--all", description = "Also show applied changesets" )
        boolean allChanges = false;

        @Argument ( index = 0, name = "dataSource", required = true, description = "DataSource to show changesets for" )
        @Completion ( DataSourceCompleter.class )
        private String dataSource;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws LiquibaseException, SchemaException, SQLException, InvalidSyntaxException {

            SchemaManager sm = getSchemaManagerFor(this.dataSource);

            ChangeLogIterator chi;
            if ( this.allChanges ) {
                chi = sm.getAllChanges();
            }
            else {
                chi = sm.getUnappliedChanges();
            }

            final XMLChangeLogSerializer xmlSerializer = new XMLChangeLogSerializer();

            chi.run(new ChangeSetConsoleOutputVisitor(xmlSerializer, this.session), null);

            return null;
        }

    }

    /**
     * Updates the database schema according to the changelogs
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "db", name = "updateSchema", description = "Update database schema" )
    public class UpdateCommand implements Action {

        @Argument ( index = 0, name = "dataSource", required = true, description = "DataSource to update" )
        @Completion ( DataSourceCompleter.class )
        private String dataSource;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws SchemaException, InvalidSyntaxException {
            SchemaManager sm = getSchemaManagerFor(this.dataSource);
            sm.ensureUpToDate();
            return null;
        }

    }

    /**
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "db", name = "setSchemaApplied", description = "Set database schema changes as applied" )
    public class SetAppliedCommand implements Action {

        @Argument ( index = 0, name = "dataSource", required = true, description = "DataSource to update" )
        @Completion ( DataSourceCompleter.class )
        private String dataSource;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws SchemaException, InvalidSyntaxException {
            SchemaManager sm = getSchemaManagerFor(this.dataSource);
            sm.setChangeLogApplied();
            return null;
        }

    }
}
