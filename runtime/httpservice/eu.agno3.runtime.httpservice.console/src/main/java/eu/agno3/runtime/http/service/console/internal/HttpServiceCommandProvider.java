/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.console.internal;


import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.http.service.HttpServiceConfig;
import eu.agno3.runtime.http.service.HttpServiceInfo;
import eu.agno3.runtime.http.service.connector.AbstractConnectorFactory;
import eu.agno3.runtime.http.service.connector.ConnectorFactory;
import eu.agno3.runtime.http.service.filter.FilterInfo;
import eu.agno3.runtime.http.service.handler.ExtendedHandler;
import eu.agno3.runtime.http.service.resource.ResourceDescriptor;
import eu.agno3.runtime.http.service.resource.ResourceInfo;
import eu.agno3.runtime.http.service.servlet.ServletInfo;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class HttpServiceCommandProvider implements CommandProvider {

    private static final String EXPORTED_BY = "  exported by "; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(HttpServiceCommandProvider.class);

    private HttpServiceInfo httpInfo;

    private Map<String, ServletInfo> servletInfo = new ConcurrentHashMap<>();
    private Map<String, FilterInfo> filterInfo = new ConcurrentHashMap<>();
    private Map<String, ResourceInfo> resourceInfo = new ConcurrentHashMap<>();

    private ConfigurationAdmin configAdmin;


    @Reference
    protected synchronized void setHttpServiceInfo ( HttpServiceInfo hsi ) {
        this.httpInfo = hsi;
    }


    protected synchronized void unsetHttpServiceInfo ( HttpServiceInfo hsi ) {
        if ( this.httpInfo == hsi ) {
            this.httpInfo = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected void setServletInfo ( ServletInfo si ) {
        this.servletInfo.put(si.getContextName(), si);
    }


    protected void unsetServletInfo ( ServletInfo si ) {
        this.servletInfo.remove(si.getContextName());
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected void setFilterInfo ( FilterInfo fi ) {
        this.filterInfo.put(fi.getContextName(), fi);
    }


    protected void unsetFilterInfo ( FilterInfo fi ) {
        this.filterInfo.remove(fi.getContextName());
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected void setResourceInfo ( ResourceInfo ri ) {
        this.resourceInfo.put(ri.getContextName(), ri);
    }


    protected void unsetResourceInfo ( ResourceInfo ri ) {
        this.resourceInfo.remove(ri.getContextName());
    }


    @Reference
    protected synchronized void setConfigurationAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    protected synchronized void unsetConfigurationAdmin ( ConfigurationAdmin cm ) {
        if ( this.configAdmin == cm ) {
            this.configAdmin = null;
        }
    }


    synchronized HttpServiceInfo getHttpServiceInfo () {
        return this.httpInfo;
    }


    Map<String, ServletInfo> getServletInfo () {
        return this.servletInfo;
    }


    Map<String, FilterInfo> getFilterInfo () {
        return this.filterInfo;
    }


    Map<String, ResourceInfo> getResourceInfo () {
        return this.resourceInfo;
    }


    /**
     * @return the configAdmin
     */
    synchronized ConfigurationAdmin getConfigAdmin () {
        return this.configAdmin;
    }


    Logger getLog () {
        return log;
    }


    /**
     * @param out
     * @param s
     */
    void printExportingBundle ( Ansi out, Class<?> clazz ) {
        Bundle exportingBundle = FrameworkUtil.getBundle(clazz);
        if ( exportingBundle != null ) {
            out.a(EXPORTED_BY).fg(Ansi.Color.BLUE).a(exportingBundle.getSymbolicName()).fg(Ansi.Color.DEFAULT).a(formatBundleId(exportingBundle))
                    .a(System.lineSeparator());
        }
    }


    /**
     * @param exportingBundle
     * @return
     */
    protected String formatBundleId ( Bundle exportingBundle ) {
        return String.format("[%s]", exportingBundle.getBundleId()); //$NON-NLS-1$
    }


    /**
     * @param out
     * @param fh
     */
    void printHolderName ( Ansi out, Holder<?> fh ) {
        if ( fh.getDisplayName() != null ) {
            out.bold().a(fh.getDisplayName()).boldOff().a(String.format(" (%s)", fh.getName())).a(System.lineSeparator()); //$NON-NLS-1$
        }
        else {
            out.bold().a(fh.getName()).boldOff().a(System.lineSeparator());
        }
    }

    /**
     * List registered connectors
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "http", name = "connectors", description = "List connectors" )
    public class ConnectorsCommand implements Action {

        /**
         * 
         */
        private static final String NO_CONFIGURATION_FOUND = "No configuration found"; //$NON-NLS-1$
        @Option ( name = "--verbose", aliases = "-v", description = "Show verbose output" )
        private boolean verbose = false;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws IOException, InvalidSyntaxException {
            List<ConnectorFactory> factories = getHttpServiceInfo().getConnectorFactories();

            for ( ConnectorFactory factory : factories ) {

                Ansi out = Ansi.ansi();

                out.bold().a(String.format(
                    "%-15s", //$NON-NLS-1$
                    factory.getConnectorName())).boldOff().a(" "); //$NON-NLS-1$
                out.a(factory.getClass().getName());

                out.a(System.lineSeparator());

                printExportingBundle(out, factory.getClass());

                out.a(System.lineSeparator());

                if ( factory instanceof AbstractConnectorFactory ) {
                    AbstractConnectorFactory absFactory = (AbstractConnectorFactory) factory;

                    out.a("  listening on ") //$NON-NLS-1$
                            .bold();

                    for ( InetAddress bindAddr : absFactory.getBindAddresses() ) {
                        out.a(String.format("%s:%d", bindAddr.getHostName(), absFactory.getPort())).newline(); //$NON-NLS-1$
                    }

                    out.boldOff();
                    out.a(System.lineSeparator());
                }

                this.session.getConsole().print(out.toString());

                if ( this.verbose ) {
                    dumpVerboseDetails(this.session, factory);

                }

            }

            return null;
        }


        /**
         * @param ci
         * @param factory
         * @throws IOException
         * @throws InvalidSyntaxException
         */
        private void dumpVerboseDetails ( Session ci, ConnectorFactory factory ) throws IOException, InvalidSyntaxException {
            Ansi vout = Ansi.ansi();

            String pid = factory.getConfigurationPID();
            FilterBuilder fb = FilterBuilder.get();
            FilterExpression filter = fb
                    .and(fb.eq(ConfigurationAdmin.SERVICE_FACTORYPID, pid), fb.eq(HttpServiceConfig.CONNECTOR_NAME, factory.getConnectorName()));

            if ( getLog().isDebugEnabled() ) {
                getLog().debug(String.format("Searching for connector %s using factory PID %s", factory.getConnectorName(), pid)); //$NON-NLS-1$
            }

            Configuration[] configs = getConfigAdmin().listConfigurations(filter.toString());
            if ( configs != null && configs.length > 0 ) {
                for ( Configuration config : configs ) {
                    printConfig(vout, config);
                }
            }
            else {
                vout.a("   ").bold().a(NO_CONFIGURATION_FOUND).boldOff(); //$NON-NLS-1$
            }

            ci.getConsole().print(vout.toString());
        }


        /**
         * @param vout
         * @param config
         */
        private void printConfig ( Ansi vout, Configuration config ) {
            Dictionary<String, Object> configProps = config.getProperties();

            Enumeration<String> props = configProps.keys();

            while ( props.hasMoreElements() ) {
                String propKey = props.nextElement();

                vout.a("   ") //$NON-NLS-1$
                        .bold().a(propKey).a(": ").boldOff(); //$NON-NLS-1$
                vout.a(configProps.get(propKey));
                vout.a(System.lineSeparator());
            }
        }
    }

    /**
     * List context handlers
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "http", name = "handlers", description = "List context handlers" )
    public class HandlersCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            List<ContextHandler> handlers = getHttpServiceInfo().getContextHandlers();

            for ( ContextHandler handler : handlers ) {
                Ansi out = Ansi.ansi();

                if ( handler instanceof ExtendedHandler ) {
                    ExtendedHandler eh = (ExtendedHandler) handler;
                    out.bold().a(eh.getContextName()).boldOff().a(" ").a(handler.getClass().getName()); //$NON-NLS-1$
                }
                else if ( handler.getDisplayName() != null ) {
                    out.bold().a(handler.getDisplayName()).boldOff().a(" ").a(handler.getClass().getName()); //$NON-NLS-1$
                }
                else {
                    out.bold().a(handler.getClass().getName()).boldOff();
                }
                out.a(System.lineSeparator());

                out.bold().a("  Context path: ").boldOff().a(handler.getContextPath()).a(System.lineSeparator()); //$NON-NLS-1$
                if ( handler.getVirtualHosts() != null ) {
                    out.bold().a("  Bound to vhosts:").boldOff().a(System.lineSeparator()); //$NON-NLS-1$
                    for ( String virtualHost : handler.getVirtualHosts() ) {
                        out.a("  ").fg(Ansi.Color.GREEN).a(virtualHost).fg(Ansi.Color.DEFAULT).a(System.lineSeparator()); //$NON-NLS-1$
                    }
                }
                else {
                    out.bold().a("  Bound to all vhosts").boldOff().a(System.lineSeparator()); //$NON-NLS-1$
                }

                this.session.getConsole().print(out.toString());
            }

            return null;
        }
    }

    /**
     * List registered servlets
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "http", name = "servlets", description = "List registered servlets" )
    public class ServletsCommand implements Action {

        @Argument ( index = 0, name = "scope", required = false )
        String scope;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws ServletException {

            if ( this.scope != null ) {
                if ( getServletInfo().containsKey(this.scope) ) {
                    this.printScope(this.scope, getServletInfo().get(this.scope), this.session.getConsole());
                }
            }
            else {
                for ( Entry<String, ServletInfo> e : getServletInfo().entrySet() ) {
                    this.printScope(e.getKey(), e.getValue(), this.session.getConsole());
                }
            }

            return null;
        }


        private void printScope ( String inScope, ServletInfo info, PrintStream cout ) throws ServletException {
            Ansi out = Ansi.ansi();

            out.bold().a(inScope).a(":").a(System.lineSeparator()); //$NON-NLS-1$

            for ( ServletHolder sh : info.getServlets() ) {
                Servlet s = sh.getServlet();
                ServletMapping servletMapping = info.getServletMapping(s);

                printHolderName(out, sh);

                out.a("   implemented by ").fg(Ansi.Color.BLUE).a(s.getClass().getName()).fg(Ansi.Color.DEFAULT).a(System.lineSeparator()); //$NON-NLS-1$

                printExportingBundle(out, s.getClass());

                out.a("   mapped to").a(System.lineSeparator()); //$NON-NLS-1$
                for ( String pathSpec : servletMapping.getPathSpecs() ) {
                    out.a("   ").fg(Ansi.Color.GREEN).a(pathSpec).fg(Ansi.Color.DEFAULT).a(System.lineSeparator()); //$NON-NLS-1$
                }

            }
            cout.print(out.toString());
        }
    }

    /**
     * List registered filters
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "http", name = "filters", description = "List registered filters" )
    public class FiltersCommand implements Action {

        @Argument ( index = 0, name = "scope", required = false )
        String scope;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            if ( this.scope != null ) {
                if ( getFilterInfo().containsKey(this.scope) ) {
                    this.printScope(this.scope, getFilterInfo().get(this.scope), this.session.getConsole());
                }
            }
            else {
                for ( Entry<String, FilterInfo> e : getFilterInfo().entrySet() ) {
                    this.printScope(e.getKey(), e.getValue(), this.session.getConsole());
                }
            }

            return null;
        }


        private void printScope ( String inScope, FilterInfo info, PrintStream cout ) {
            Ansi out = Ansi.ansi();

            out.bold().a(inScope).a(":").a(System.lineSeparator()); //$NON-NLS-1$

            for ( FilterHolder fh : info.getFilters() ) {
                Filter f = fh.getFilter();
                FilterMapping filterMapping = info.getFilterMapping(f);

                printHolderName(out, fh);

                out.a("  implemented by ").fg(Ansi.Color.BLUE).a(f.getClass().getName()).fg(Ansi.Color.DEFAULT).a(System.lineSeparator()); //$NON-NLS-1$

                Bundle exportingBundle = FrameworkUtil.getBundle(f.getClass());
                out.a(EXPORTED_BY).fg(Ansi.Color.BLUE).a(exportingBundle.getSymbolicName()).fg(Ansi.Color.DEFAULT).a(formatBundleId(exportingBundle))
                        .a(System.lineSeparator());

                out.a("  mapped to").a(System.lineSeparator()); //$NON-NLS-1$
                if ( filterMapping.getPathSpecs() != null ) {
                    for ( String pathSpec : filterMapping.getPathSpecs() ) {
                        out.a("  ").fg(Ansi.Color.GREEN).a(pathSpec).fg(Ansi.Color.DEFAULT).a(System.lineSeparator()); //$NON-NLS-1$
                    }
                }
            }

            cout.print(out.toString());
        }

    }

    /**
     * List registered resources
     * 
     * @author mbechler
     */
    @Command ( scope = "http", name = "resources", description = "List registered resources" )
    public class ResourcesCommand implements Action {

        @Argument ( index = 0, name = "scope", required = false )
        String scope;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {

            if ( this.scope != null ) {
                if ( getResourceInfo().containsKey(this.scope) ) {
                    this.printScope(this.scope, getResourceInfo().get(this.scope), this.session.getConsole());
                }
            }
            else {

                for ( Entry<String, ResourceInfo> e : getResourceInfo().entrySet() ) {
                    this.printScope(e.getKey(), e.getValue(), this.session.getConsole());
                }
            }

            return null;
        }


        private void printScope ( String inScope, ResourceInfo info, PrintStream cout ) {
            Ansi out = Ansi.ansi();

            out.bold().a(inScope).a(":").a(System.lineSeparator()); //$NON-NLS-1$

            for ( ResourceDescriptor rd : info.getResource() ) {
                Bundle exportingBundle = rd.getBundle();

                out.a(" ").bold().a(rd.getPath()).boldOff(). //$NON-NLS-1$
                        a(" -> ").fg(Ansi.Color.BLUE).a(exportingBundle.getSymbolicName()).fg(Ansi.Color.DEFAULT) //$NON-NLS-1$
                        .a(formatBundleId(exportingBundle)).a(":").a(rd.getResourceBase()). //$NON-NLS-1$
                        a(" prio ").a(rd.getPriority()).a(System.lineSeparator());//$NON-NLS-1$

            }

            cout.print(out.toString());
        }
    }

}
