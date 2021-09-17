/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.08.2013 by mbechler
 */
package eu.agno3.runtime.db.console;


import java.util.Collection;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.db.AdministrativeDataSource;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class DatabaseCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(DatabaseCommandProvider.class);
    private ComponentContext componentContext;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.componentContext = context;
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.componentContext = null;
    }


    /**
     * @return the componentContext
     */
    ComponentContext getComponentContext () {
        return this.componentContext;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    AdministrativeDataSource getAdminDataSource ( String dataSource ) {
        FilterExpression filter = FilterBuilder.get().eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dataSource);
        try {
            Collection<ServiceReference<AdministrativeDataSource>> adminDs = getComponentContext().getBundleContext().getServiceReferences(
                AdministrativeDataSource.class,
                filter.toString());
            return getComponentContext().getBundleContext().getService(adminDs.iterator().next());
        }
        catch (
            InvalidSyntaxException |
            NoSuchElementException e ) {
            log.debug("Failed to locate admin datasource:", e); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Lists available datasources
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "db", name = "datasources", description = "List available datasources" )
    public class DataSourcesCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws InvalidSyntaxException {

            BundleContext ctx = getComponentContext().getBundleContext();

            MultiValuedMap<String, String> dataSources = new HashSetValuedHashMap<>();
            Collection<ServiceReference<DataSource>> refs = ctx.getServiceReferences(DataSource.class, null);
            String driverClassName = null;

            for ( ServiceReference<DataSource> ref : refs ) {
                driverClassName = handleDataSource(this.session, dataSources, driverClassName, ref);
            }

            if ( dataSources.isEmpty() ) {
                this.session.getConsole().println("No DataSources available"); //$NON-NLS-1$
                return null;
            }

            for ( String dataSourceName : dataSources.keySet() ) {
                dumpDataSource(this.session, ctx, dataSources, driverClassName, dataSourceName);
            }

            return null;
        }


        /**
         * @param ci
         * @param dataSources
         * @param driverClassName
         * @param ref
         * @return
         */
        private String handleDataSource ( Session ci, MultiValuedMap<String, String> dataSources, String driverClassName,
                ServiceReference<DataSource> ref ) {
            String dataSourceName = (String) ref.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);
            String dataSourceUser = (String) ref.getProperty(DataSourceFactory.JDBC_USER);
            dataSources.put(dataSourceName, dataSourceUser);

            String refDriverClassName = (String) ref.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);

            if ( driverClassName == null && refDriverClassName != null ) {
                return refDriverClassName;
            }
            else if ( driverClassName != null && refDriverClassName != null ) {
                if ( !driverClassName.equals(refDriverClassName) ) {
                    ci.getConsole().println("WARN: different drivers for same datasource, following might not be accurate"); //$NON-NLS-1$
                }
            }
            else {
                ci.getConsole().println("WARN: no driver class name set for datasource"); //$NON-NLS-1$
            }

            return driverClassName;
        }


        /**
         * @param ci
         * @param ctx
         * @param dataSources
         * @param driverClassName
         * @param dataSourceName
         * @throws InvalidSyntaxException
         */
        private void dumpDataSource ( Session ci, BundleContext ctx, MultiValuedMap<String, String> dataSources, String driverClassName,
                String dataSourceName ) throws InvalidSyntaxException {
            Ansi out = Ansi.ansi();
            out.a("DataSource: ").bold().a(dataSourceName).boldOff() //$NON-NLS-1$
                    .a(" backed by driver ").a(driverClassName).newline(); //$NON-NLS-1$

            if ( getAdminDataSource(dataSourceName) != null ) {
                out.a("  AdministrativeDataSource available").newline(); //$NON-NLS-1$
            }
            else {
                out.a("  AdministrativeDataSource NOT available").newline(); //$NON-NLS-1$
            }

            out.newline();

            for ( String user : dataSources.get(dataSourceName) ) {
                dumpDataSourceUser(ctx, dataSourceName, out, user);
            }

            ci.getConsole().print(out.toString());
        }


        /**
         * @param ctx
         * @param dataSourceName
         * @param out
         * @param user
         * @throws InvalidSyntaxException
         */
        private void dumpDataSourceUser ( BundleContext ctx, String dataSourceName, Ansi out, String user ) throws InvalidSyntaxException {
            FilterBuilder fb = FilterBuilder.get();
            FilterExpression filter = fb.and(fb.eq(DataSourceFactory.JDBC_DATASOURCE_NAME, dataSourceName), fb.eq(DataSourceFactory.JDBC_USER, user));
            Collection<ServiceReference<DataSource>> dsRefs = ctx.getServiceReferences(DataSource.class, filter.toString());

            out.a(String.format("  %d instance(s) available for user ", dsRefs.size())) //$NON-NLS-1$
                    .bold().a(user).boldOff().a(":").newline(); //$NON-NLS-1$

            for ( ServiceReference<DataSource> ref : dsRefs ) {
                DataSource instance = ctx.getService(ref);

                out.a("    ") //$NON-NLS-1$
                        .fg(Ansi.Color.BLUE).a("type") //$NON-NLS-1$
                        .fg(Ansi.Color.DEFAULT).a(": ") //$NON-NLS-1$ 
                        .a(instance.getClass().getName()).newline();

                for ( String property : ref.getPropertyKeys() ) {
                    dumpProperty(out, ref, property);
                }
            }

            out.newline();
        }


        /**
         * @param out
         * @param ref
         * @param property
         */
        private void dumpProperty ( Ansi out, ServiceReference<DataSource> ref, String property ) {
            Object val = ref.getProperty(property);

            if ( val instanceof Object[] ) {
                out.a("    ") //$NON-NLS-1$
                        .fg(Ansi.Color.BLUE).a(property).fg(Ansi.Color.DEFAULT).a(": ") //$NON-NLS-1$
                        .a(StringUtils.join((Object[]) val, ",")).newline(); //$NON-NLS-1$
            }
            else {
                out.a("    ").fg(Ansi.Color.BLUE).a(property).fg(Ansi.Color.DEFAULT).a(": ").a(ref.getProperty(property)).newline(); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
    }
}
