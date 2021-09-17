/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
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
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class ServiceCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(ServiceCommandProvider.class);
    private BundleContext bundleContext;
    private static final String IMPLEMENTATION_UNKNOWN = " Implementation unknown"; //$NON-NLS-1$


    /**
     * @return the bundleContext
     */
    BundleContext getBundleContext () {
        return this.bundleContext;
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.bundleContext = null;
    }


    /**
     * @param out
     * @param ref
     */
    protected void dumpImplementation ( Ansi out, ServiceReference<?> ref ) {
        try {
            Object implementation = getBundleContext().getService(ref);
            if ( implementation != null && implementation.getClass() != null ) {
                out.a(", Class: ").bold().a(implementation.getClass().getName()).boldOff(); //$NON-NLS-1$
            }
            else {
                out.a(IMPLEMENTATION_UNKNOWN);
            }
        }
        catch ( Exception e ) {
            getLog().trace("Failed to lookup implementation:", e); //$NON-NLS-1$
            out.a(IMPLEMENTATION_UNKNOWN);
        }
    }


    static Logger getLog () {
        return log;
    }

    /**
     * List exported services
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "services", name = "list", description = "List exported services" )
    public class ServiceCommand implements Action {

        @Option ( name = "-verbose", aliases = "-v" )
        private boolean verbose = false;

        @Option ( name = "-class", aliases = "-c" )
        private String clazz = null;

        @Option ( name = "-filter", aliases = "-f" )
        private String filter = null;

        @Argument ( index = 0, name = "class", description = "Service interface, prefix matched", required = false )
        @Completion ( ServiceCompleter.class )
        private String serviceClass = StringUtils.EMPTY;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws InvalidSyntaxException {
            SortedMap<String, List<ServiceReference<?>>> services = collectServices();

            for ( Entry<String, List<ServiceReference<?>>> service : services.entrySet() ) {
                Ansi out = Ansi.ansi();

                out.bold().fg(Ansi.Color.BLUE).a(service.getKey()).fg(Ansi.Color.DEFAULT).boldOff().a(System.lineSeparator());

                for ( ServiceReference<?> ref : service.getValue() ) {
                    printService(out, ref);
                }

                this.session.getConsole().print(out.toString());
            }

            return null;
        }


        /**
         * @param out
         * @param ref
         */
        private void printService ( Ansi out, ServiceReference<?> ref ) {
            Bundle exportingBundle = ref.getBundle();

            BundleCommandUtil.printBundleReference(out, exportingBundle);

            if ( ref.getProperty(Constants.SERVICE_ID) != null ) {
                out.a(String.format(", ID %d", ref.getProperty(Constants.SERVICE_ID))); //$NON-NLS-1$
            }

            dumpImplementation(out, ref);

            if ( this.verbose && ref.getPropertyKeys() != null ) {
                printProperties(out, ref);
            }
            else {
                out.a(System.lineSeparator());
            }

            if ( ref.getUsingBundles() != null && ref.getUsingBundles().length > 0 ) {
                for ( Bundle usingBundle : ref.getUsingBundles() ) {
                    out.a("  used by "); //$NON-NLS-1$
                    BundleCommandUtil.printBundleReference(out, usingBundle);
                    out.a(System.lineSeparator());
                }
            }
        }


        /**
         * @param out
         * @param ref
         */
        private void printProperties ( Ansi out, ServiceReference<?> ref ) {
            out.a(", Properties:").a(System.lineSeparator()); //$NON-NLS-1$

            for ( String property : ref.getPropertyKeys() ) {

                if ( property.equals(Constants.SERVICE_ID) || property.equals(Constants.OBJECTCLASS) ) {
                    continue;
                }

                out.a("   ").bold().a(property); //$NON-NLS-1$ 
                out.a("=").boldOff().a(ref.getProperty(property).toString()).a(System.lineSeparator()); //$NON-NLS-1$
            }
        }


        /**
         * @return
         * @throws InvalidSyntaxException
         */
        private SortedMap<String, List<ServiceReference<?>>> collectServices () throws InvalidSyntaxException {
            ServiceReference<?>[] allReferences = getBundleContext().getAllServiceReferences(this.clazz, this.filter);

            SortedMap<String, List<ServiceReference<?>>> services = new TreeMap<>();

            for ( ServiceReference<?> s : allReferences ) {
                String[] objectClassProps = (String[]) s.getProperty(Constants.OBJECTCLASS);

                if ( objectClassProps == null ) {
                    continue;
                }

                for ( String objectClass : objectClassProps ) {

                    if ( !objectClass.startsWith(this.serviceClass) ) {
                        continue;
                    }

                    if ( !services.containsKey(objectClass) ) {
                        services.put(objectClass, new ArrayList<ServiceReference<?>>());
                    }

                    services.get(objectClass).add(s);
                }

            }
            return services;
        }

    }

    /**
     * Get information about services im-/exported by a bundle
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "services", name = "info", description = "Get information about services im-/exported by a bundle" )
    public class ServiceInfoCommand implements Action {

        /**
         * 
         */

        @Argument ( index = 0, name = "bundle", required = true )
        @Completion ( BundleCompleter.class )
        String bundle;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws BundleException {
            Bundle b = BundleCommandUtil.findBundle(getBundleContext(), this.bundle);

            Ansi out = Ansi.ansi();

            if ( b.getRegisteredServices() != null ) {
                printExportedServices(b, out);
            }

            ServiceReference<?>[] inUse = b.getServicesInUse();
            if ( inUse != null ) {
                printServiceImports(out, inUse);
            }

            this.session.getConsole().print(out.toString());

            return null;
        }


        /**
         * @param out
         * @param inUse
         */
        private void printServiceImports ( Ansi out, ServiceReference<?>[] inUse ) {
            out.bold().a("Imports:").boldOff().a(System.lineSeparator()); //$NON-NLS-1$
            for ( ServiceReference<?> ref : inUse ) {
                String[] objectClasses = (String[]) ref.getProperty(Constants.OBJECTCLASS);

                for ( String objectClass : objectClasses ) {
                    out.a(" ").bold().fg(Ansi.Color.BLUE).a(objectClass).fg(Ansi.Color.DEFAULT).boldOff().a(System.lineSeparator()); //$NON-NLS-1$
                }

                out.a("  from "); //$NON-NLS-1$
                BundleCommandUtil.printBundleReference(out, ref.getBundle());

                dumpImplementation(out, ref);
                out.a(System.lineSeparator());
            }
        }


        /**
         * @param b
         * @param out
         */
        private void printExportedServices ( Bundle b, Ansi out ) {
            out.bold().a("Exports:").boldOff().a(System.lineSeparator()); //$NON-NLS-1$
            for ( ServiceReference<?> ref : b.getRegisteredServices() ) {

                for ( String objectClass : (String[]) ref.getProperty(Constants.OBJECTCLASS) ) {
                    out.a(" ").bold().fg(Ansi.Color.BLUE).a(objectClass).fg(Ansi.Color.DEFAULT).boldOff() //$NON-NLS-1$
                            .a(System.lineSeparator());
                }

                for ( String property : ref.getPropertyKeys() ) {

                    if ( property.equals(Constants.OBJECTCLASS) ) {
                        continue;
                    }

                    out.a("  ").bold().a(property) //$NON-NLS-1$ 
                            .a("=").boldOff().a(ref.getProperty(property).toString()).a(System.lineSeparator());//$NON-NLS-1$
                }
            }
        }

    }
}
