/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.ldap.filter.FilterBuilder;
import eu.agno3.runtime.ldap.filter.FilterExpression;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class ConfigAdminCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(ConfigAdminCommandProvider.class);

    private BundleContext bundleContext;
    private ConfigurationAdmin configAdmin;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.bundleContext = null;
    }


    @Reference
    protected synchronized void setConfigAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    protected synchronized void unsetConfigAdmin ( ConfigurationAdmin cm ) {
        if ( this.configAdmin == cm ) {
            this.configAdmin = null;
        }
    }


    BundleContext getBundleContext () {
        return this.bundleContext;
    }


    ConfigurationAdmin getConfigurationAdmin () {
        return this.configAdmin;
    }


    Logger getLog () {
        return log;
    }


    protected static Set<String> completePid ( ConfigurationAdmin admin ) {
        Set<String> res = new HashSet<>();

        try {
            for ( Configuration cfg : admin.listConfigurations(null) ) {
                if ( cfg.getFactoryPid() != null ) {
                    res.add(cfg.getFactoryPid());
                }

                res.add(cfg.getPid());
            }
        }
        catch ( Exception e ) {
            log.trace("Error enumerating configurations:", e); //$NON-NLS-1$
            return res;
        }

        return res;
    }

    /**
     * Lists configurations
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "cm", name = "list", description = "List configurations" )
    public class ConfigListCommand implements Action {

        @Option ( name = "-verbose", aliases = "-v" )
        boolean verbose = false;

        @Argument ( index = 0, name = "bundle", required = false )
        @Completion ( BundleCompleter.class )
        String bundleSpec;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws BundleException, IOException, InvalidSyntaxException {
            Configuration[] configs = null;

            if ( this.bundleSpec != null ) {
                Bundle b = BundleCommandUtil.findBundle(getBundleContext(), this.bundleSpec);
                FilterExpression filter = FilterBuilder.get().eq(ConfigurationAdmin.SERVICE_BUNDLELOCATION, b.getLocation());
                configs = getConfigurationAdmin().listConfigurations(filter.toString());
            }
            else {
                configs = getConfigurationAdmin().listConfigurations(null);
            }

            if ( configs == null ) {
                return null;
            }

            for ( Configuration config : configs ) {
                Ansi out = Ansi.ansi();

                if ( config.getFactoryPid() != null ) {
                    out.bold().a("F ").fg(Ansi.Color.BLUE).a(config.getFactoryPid()).boldOff().fg(Ansi.Color.DEFAULT); //$NON-NLS-1$
                    out.a(String.format("(%s)", config.getPid())); //$NON-NLS-1$
                }
                else {
                    out.bold().a("I ").fg(Ansi.Color.BLUE).a(config.getPid()).boldOff().fg(Ansi.Color.DEFAULT); //$NON-NLS-1$
                }

                if ( config.getBundleLocation() != null ) {
                    Bundle b = getBundleContext().getBundle(config.getBundleLocation());

                    out.a(" bound to "); //$NON-NLS-1$
                    if ( b != null ) {
                        BundleCommandUtil.printBundleReference(out, b);
                    }
                    else {
                        out.a(config.getBundleLocation());
                    }
                }

                out.a(System.lineSeparator());

                if ( this.verbose ) {
                    Enumeration<String> propertyKeys = config.getProperties().keys();

                    while ( propertyKeys.hasMoreElements() ) {
                        String key = propertyKeys.nextElement();
                        Object value = config.getProperties().get(key);

                        out.a(" ").bold(); //$NON-NLS-1$
                        out.a(key).a(": ").boldOff(); //$NON-NLS-1$

                        BundleCommandUtil.dumpPropertyValue(out, value);

                        out.a(System.lineSeparator());
                    }
                }

                this.session.getConsole().print(out.toString());
            }

            return null;
        }

    }

    /**
     * Get configuration
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "cm", name = "get", description = "Get configuration" )
    public class ConfigGetCommand implements Action {

        @Argument ( index = 0, name = "pid", required = true )
        @Completion ( PIDCompleter.class )
        private String pid;

        @Argument ( index = 1, name = "property", required = false )
        private String propertyFilter;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws IOException {

            Configuration config = getConfigurationAdmin().getConfiguration(this.pid, null);

            List<String> properties = new ArrayList<>();
            Dictionary<String, Object> props = config.getProperties();

            if ( props == null ) {
                this.session.getConsole().println("not found"); //$NON-NLS-1$
                return null;
            }

            Enumeration<String> availableProperties = props.keys();

            while ( availableProperties.hasMoreElements() ) {
                String prop = availableProperties.nextElement();

                if ( this.propertyFilter == null || prop.startsWith(this.propertyFilter) ) {
                    properties.add(prop);
                }
            }

            for ( String prop : properties ) {
                Ansi out = Ansi.ansi();

                out.a(" ").bold(); //$NON-NLS-1$
                out.a(prop).a(": ").boldOff(); //$NON-NLS-1$
                BundleCommandUtil.dumpPropertyValue(out, props.get(prop));
                out.a(System.lineSeparator());

                this.session.getConsole().print(out);
            }

            return null;
        }

    }

    /**
     * Set configuration
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "cm", name = "set", description = "Set configuration" )
    public class ConfigSetCommand implements Action {

        @Argument ( index = 0, name = "pid", required = true )
        @Completion ( PIDCompleter.class )
        private String pid;

        @Argument ( index = 1, name = "property", required = true )
        private String property;

        @Argument ( index = 2, name = "value", description = "if empty, unset" )
        private String value;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws IOException {
            Configuration config = getConfigurationAdmin().getConfiguration(this.pid, null);

            Dictionary<String, Object> currentConfig = config.getProperties();

            if ( currentConfig == null ) {
                currentConfig = new Hashtable<>();
            }

            if ( this.value == null ) {
                this.session.getConsole().println(String.format("Unset property '%s'", this.property)); //$NON-NLS-1$
                currentConfig.remove(this.property);
            }
            else {
                this.session.getConsole().println(String.format("Set property '%s' to '%s'", this.property, this.value)); //$NON-NLS-1$
                currentConfig.put(this.property, this.value);
            }

            config.update(currentConfig);

            return null;
        }

    }
}
