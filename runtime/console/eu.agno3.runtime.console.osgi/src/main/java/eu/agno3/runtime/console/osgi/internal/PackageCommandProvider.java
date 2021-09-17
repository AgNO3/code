/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
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
import org.eclipse.osgi.report.resolution.ResolutionReport;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Namespace;
import org.osgi.resource.Resource;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.ldap.filter.FilterParserException;
import eu.agno3.runtime.ldap.filter.FilterSyntaxException;
import eu.agno3.runtime.ldap.filter.FilterType;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class PackageCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(PackageCommandProvider.class);
    private ComponentContext componentContext;

    private ConstraintUtil commandUtil;

    private static final String DYNAMIC_RESOLUTION = "dynamic"; //$NON-NLS-1$


    @Reference
    protected synchronized void setConstraintUtil ( ConstraintUtil util ) {
        this.commandUtil = util;
    }


    protected synchronized void unsetConstraintUtil ( ConstraintUtil util ) {
        if ( this.commandUtil == util ) {
            this.commandUtil = null;
        }
    }


    @Activate
    void activate ( ComponentContext context ) {
        this.componentContext = context;
    }


    static Logger getLog () {
        return log;
    }


    synchronized ConstraintUtil getConstraintUtil () {
        return this.commandUtil;
    }


    ComponentContext getComponentContext () {
        return this.componentContext;
    }

    /**
     * @author mbechler
     * 
     */
    @Command ( scope = "packages", name = "list", description = "List packages imported/exported by a bundle" )
    public class PackageListCommandProvider implements Action {

        private static final String OSGI_IDENTITY = "osgi.identity"; //$NON-NLS-1$

        /**
         * 
         */

        @Option ( name = "-imports", aliases = "-i", required = false, description = "Show imports" )
        boolean listImports = false;

        @Option ( name = "-exports", aliases = "-e", required = false, description = "Show exports" )
        boolean listExports = false;

        @Option ( name = "-unsat", aliases = "-u", required = false, description = "Show unsatisfied imports" )
        boolean listUnsat = false;

        @Option ( name = "-verbose", aliases = "-v", required = false, description = "Be verbose" )
        boolean verbose = false;

        @Argument ( index = 0, name = "bundle", required = true )
        @Completion ( BundleCompleter.class )
        private String bundleSpec;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () throws BundleException, FilterParserException {

            if ( !this.listImports && !this.listExports && !this.listUnsat ) {
                this.listExports = true;
                this.listImports = true;
            }

            Bundle b = BundleCommandUtil.findBundle(getComponentContext().getBundleContext(), this.bundleSpec);

            BundleWiring wiring = b.adapt(BundleWiring.class);
            if ( wiring != null ) {
                if ( this.listExports ) {
                    printExports(this.session, wiring);
                }

                if ( this.listImports ) {
                    printImports(this.session, wiring);
                }
            }
            else {
                this.session.getConsole().print(Ansi.ansi().a("Bundle could not be resolved").newline().toString()); //$NON-NLS-1$
            }

            try {
                printResolverStatus(this.session, b);
            }
            catch ( Exception e ) {
                getLog().error("Failed to get resolver status.", e); //$NON-NLS-1$
            }

            return null;
        }


        /**
         * @param ci
         * @param b
         */
        private void printResolverStatus ( Session ci, Bundle b ) {
            ResolutionReport report = getResolutionReport(new Bundle[] {
                b
            });

            Resource r = getBundleResource(b, report);

            if ( r != null ) {
                ci.getConsole().println(report.getResolutionReportMessage(r));
            }
        }


        private Resource getBundleResource ( Bundle b, ResolutionReport report ) {
            for ( Entry<Resource, List<ResolutionReport.Entry>> r : report.getEntries().entrySet() ) {
                for ( Capability c : r.getKey().getCapabilities(OSGI_IDENTITY) ) {
                    if ( b.getSymbolicName().equals(c.getAttributes().get(OSGI_IDENTITY)) ) {
                        return r.getKey();
                    }
                }
            }
            return null;
        }


        private ResolutionReport getResolutionReport ( Bundle[] bundles ) {
            DiagReportListener reportListener = new DiagReportListener(bundles);
            ServiceRegistration<ResolverHookFactory> hookReg = DsUtil
                    .registerSafe(getComponentContext(), ResolverHookFactory.class, reportListener, null);
            try {
                Bundle systemBundle = getComponentContext().getBundleContext().getBundle(Constants.SYSTEM_BUNDLE_LOCATION);
                FrameworkWiring frameworkWiring = systemBundle.adapt(FrameworkWiring.class);
                frameworkWiring.resolveBundles(Arrays.asList(bundles));
                return reportListener.getReport();
            }
            finally {
                DsUtil.unregisterSafe(getComponentContext(), hookReg);
            }
        }


        /**
         * @param out
         * @param unsat
         */
        void printResolutionState ( Ansi out, BundleRequirement req ) {
            String resolution = req.getDirectives().get(Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE);

            if ( resolution == null ) {
                resolution = Constants.RESOLUTION_MANDATORY;
            }

            switch ( resolution ) {
            case DYNAMIC_RESOLUTION:
                out.fg(Ansi.Color.BLUE);
                break;
            case Constants.RESOLUTION_MANDATORY:
                out.fg(Ansi.Color.RED);
                break;
            case Constants.RESOLUTION_OPTIONAL:
                out.fg(Ansi.Color.GREEN);
                break;
            default:
                break;
            }

            out.bold().a(String.format("%10s", resolution.toUpperCase())).boldOff(); //$NON-NLS-1$

            out.fg(Ansi.Color.DEFAULT);
        }


        /**
         * @param ci
         * @param wiring
         * @throws FilterParserException
         */
        private void printImports ( Session ci, BundleWiring wiring ) throws FilterParserException {
            List<BundleRequirement> imports = wiring.getRequirements(BundleRevision.PACKAGE_NAMESPACE);

            ci.getConsole().println(Ansi.ansi().bold().a("Imports:").boldOff()); //$NON-NLS-1$

            for ( BundleRequirement imp : imports ) {
                Ansi out = Ansi.ansi();
                String filter = imp.getDirectives().get(Namespace.REQUIREMENT_FILTER_DIRECTIVE);
                printResolutionState(out, imp);
                out.a(" "); //$NON-NLS-1$

                try {
                    String packageName = getConstraintUtil()
                            .extractSimpleFilterAttribute(filter, PackageNamespace.PACKAGE_NAMESPACE, FilterType.EQUALS);

                    out.a(packageName).a(" "); //$NON-NLS-1$

                    VersionRange vRange = getConstraintUtil().extractVersionRange(filter);

                    if ( !vRange.getLeft().equals(Version.emptyVersion) || vRange.getRight() != null ) {
                        out.bold().a(vRange).boldOff();
                    }

                }
                catch (
                    IllegalArgumentException |
                    FilterSyntaxException e ) {

                    getLog().debug("Failed to determine version range:", e); //$NON-NLS-1$
                }
                ci.getConsole().println(out.toString());

            }
        }


        /**
         * @param ci
         * @param wiring
         */
        private void printExports ( Session ci, BundleWiring wiring ) {
            List<BundleCapability> exports = wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);

            ci.getConsole().println(Ansi.ansi().bold().a("Exports:").boldOff()); //$NON-NLS-1$

            for ( BundleCapability export : exports ) {
                Ansi out = Ansi.ansi();

                out.bold().a(export.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE)).boldOff();

                if ( export.getAttributes().containsKey(Constants.VERSION_ATTRIBUTE)
                        && !export.getAttributes().get(Constants.VERSION_ATTRIBUTE).equals(Version.emptyVersion) ) {
                    out.a("-").bold().a(export.getAttributes().get(Constants.VERSION_ATTRIBUTE)).boldOff(); //$NON-NLS-1$
                }
                out.a(" "); //$NON-NLS-1$

                for ( Entry<String, String> attr : export.getDirectives().entrySet() ) {
                    if ( attr.getKey().equals(Constants.USES_DIRECTIVE) ) {
                        continue;
                    }
                    out.a(String.format("%s=%s;", attr.getKey(), attr.getValue().toString())); //$NON-NLS-1$
                }
                out.a(System.lineSeparator());

                if ( this.verbose && export.getDirectives().containsKey(Constants.USES_DIRECTIVE) ) {
                    printUses(export, out);
                }

                ci.getConsole().print(out.toString());
            }
        }


        /**
         * @param export
         * @param out
         */
        private void printUses ( BundleCapability export, Ansi out ) {
            String uses = export.getDirectives().get(Constants.USES_DIRECTIVE);
            try ( Scanner s = new Scanner(uses) ) {

                s.useDelimiter(","); //$NON-NLS-1$

                while ( s.hasNext() ) {
                    String use = s.next();

                    out.fg(Ansi.Color.CYAN).a(" uses ").fg(Ansi.Color.DEFAULT); //$NON-NLS-1$
                    out.a(use);
                    out.a(System.lineSeparator());
                }
            }
            catch ( Exception e ) {
                getLog().warn("Failed to collect export uses:", e); //$NON-NLS-1$
            }
        }

    }

    /**
     * Show package exports
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "packages", name = "package", description = "Show package exports" )
    public class PackageCommand implements Action {

        @Argument ( index = 0, name = "package", description = "Package specification, prefix match", required = false )
        @Completion ( PackageCompleter.class )
        String packagePrefix = StringUtils.EMPTY;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            SortedMap<String, List<BundleCapability>> allExports = collectCapabilities();

            for ( Entry<String, List<BundleCapability>> entry : allExports.entrySet() ) {

                Ansi out = Ansi.ansi();

                out.bold().fg(Ansi.Color.BLUE).a(entry.getKey()).boldOff().fg(Ansi.Color.DEFAULT).a(System.lineSeparator());

                for ( BundleCapability cap : entry.getValue() ) {
                    printCapability(out, cap);
                }

                this.session.getConsole().print(out.toString());
            }

            return null;
        }


        /**
         * @param out
         * @param cap
         */
        private void printCapability ( Ansi out, BundleCapability cap ) {
            if ( cap.getAttributes().containsKey(Constants.VERSION_ATTRIBUTE)
                    && !cap.getAttributes().get(Constants.VERSION_ATTRIBUTE).equals(Version.emptyVersion) ) {
                out.a(" version "); //$NON-NLS-1$
                out.bold().a(cap.getAttributes().get(Constants.VERSION_ATTRIBUTE)).boldOff();
            }
            else {
                out.bold().a(" unversioned").boldOff(); //$NON-NLS-1$
            }

            out.a(" export by "); //$NON-NLS-1$
            BundleCommandUtil.printBundleReference(out, cap.getResource().getBundle());
            out.a(System.lineSeparator());
        }


        /**
         * @return
         */
        private SortedMap<String, List<BundleCapability>> collectCapabilities () {
            Bundle[] bundles = getComponentContext().getBundleContext().getBundles();

            SortedMap<String, List<BundleCapability>> allExports = new TreeMap<>();

            for ( Bundle b : bundles ) {

                BundleWiring wiring = b.adapt(BundleWiring.class);

                if ( wiring == null ) {
                    continue;
                }

                List<BundleCapability> exports = wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);

                for ( BundleCapability imp : exports ) {
                    String packageName = (String) imp.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);

                    if ( !packageName.startsWith(this.packagePrefix) ) {
                        continue;
                    }

                    if ( !allExports.containsKey(packageName) ) {
                        allExports.put(packageName, new ArrayList<BundleCapability>());
                    }

                    allExports.get(packageName).add(imp);
                }

            }
            return allExports;
        }

    }

    static class DiagReportListener implements ResolverHookFactory {

        private final Collection<BundleRevision> targetTriggers = new ArrayList<>();

        volatile ResolutionReport report = null;


        DiagReportListener ( Bundle[] bundles ) {
            for ( Bundle bundle : bundles ) {
                BundleRevision revision = bundle.adapt(BundleRevision.class);
                if ( revision != null && revision.getWiring() == null ) {
                    this.targetTriggers.add(revision);
                }
            }

        }


        @Override
        public ResolverHook begin ( Collection<BundleRevision> triggers ) {
            if ( triggers.containsAll(this.targetTriggers) ) {
                return new DiagResolverHook();
            }
            return null;
        }


        ResolutionReport getReport () {
            return this.report;
        }

        class DiagResolverHook implements ResolverHook, ResolutionReport.Listener {

            @Override
            public void handleResolutionReport ( ResolutionReport r ) {
                DiagReportListener.this.report = r;
            }


            @Override
            public void filterResolvable ( Collection<BundleRevision> candidates ) {
                // nothing
            }


            @Override
            public void filterSingletonCollisions ( BundleCapability singleton, Collection<BundleCapability> collisionCandidates ) {
                // nothing
            }


            @Override
            public void filterMatches ( BundleRequirement requirement, Collection<BundleCapability> candidates ) {
                // nothing
            }


            @Override
            public void end () {
                // nothing
            }

        }

    }
}
