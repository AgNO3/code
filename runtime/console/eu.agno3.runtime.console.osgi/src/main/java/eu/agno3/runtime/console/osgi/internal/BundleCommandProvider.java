/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

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
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.update.BundleUpdater;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class BundleCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(BundleCommandProvider.class);

    private BundleContext bundleContext;
    private LazyBundleTracker lazyBundles;

    private BundleUpdater bundleUpdater;


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
        this.lazyBundles = new LazyBundleTracker();
        this.bundleContext.addBundleListener(this.lazyBundles);
        this.lazyBundles.init(this.bundleContext);
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        context.getBundleContext().removeBundleListener(this.lazyBundles);
    }


    @Reference
    protected synchronized void setBundleUpdater ( BundleUpdater upd ) {
        this.bundleUpdater = upd;
    }


    protected synchronized void unsetBundleUpdater ( BundleUpdater upd ) {
        if ( this.bundleUpdater == upd ) {
            this.bundleUpdater = null;
        }
    }


    static Logger getLog () {
        return log;
    }


    synchronized BundleContext getBundleContext () {
        return this.bundleContext;
    }


    synchronized LazyBundleTracker getLazyBundleTracker () {
        return this.lazyBundles;
    }


    /**
     * @return the bundleUpdater
     */
    synchronized BundleUpdater getBundleUpdater () {
        return this.bundleUpdater;
    }

    /**
     * List installed bundles
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "list", description = "List installed bundles" )
    public class ListBundlesCommand implements Action {

        @Option ( aliases = "-v", name = "-verbose" )
        boolean verbose = false;

        @Option ( aliases = "-s", name = "-sigs" )
        boolean sigs = false;

        @Option ( name = "-unsigned" )
        boolean unsigned = false;

        @Argument ( index = 0, name = "filter", required = false )
        @Completion ( BundleCompleter.class )
        String filter = null;

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

            Bundle[] installedBundles = getBundleContext().getBundles();

            for ( Bundle b : installedBundles ) {
                if ( this.filter != null && !BundleCommandUtil.filterMatch(b, this.filter) ) {
                    continue;
                }

                Map<X509Certificate, List<X509Certificate>> signers = Collections.EMPTY_MAP;
                try {

                    signers = b.getSignerCertificates(Bundle.SIGNERS_TRUSTED);
                }
                catch ( Exception e ) {
                    getLog().error("Failed to get bundle signer for " + b.getSymbolicName(), e); //$NON-NLS-1$
                }

                if ( this.unsigned && signers != null && !signers.isEmpty() ) {
                    continue;
                }

                BundleCommandUtil.printBundleFormatted(this.session.getConsole(), b, this.verbose, this.sigs, getLazyBundleTracker());
            }

            return null;
        }
    }

    protected abstract class AbstractPerBundleCommand implements Action {

        @Argument ( index = 0, name = "bundles", multiValued = true, required = true )
        @Completion ( BundleCompleter.class )
        protected List<String> bundles;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () throws BundleException {

            for ( String bundleSpec : this.bundles ) {
                Bundle b = BundleCommandUtil.findBundle(getBundleContext(), bundleSpec);
                this.executePerBundle(b, this.session);
            }

            return null;
        }


        /**
         * 
         * @return list of bundles
         */
        public List<String> complete () {
            return BundleCommandUtil.completeBundles(getBundleContext());
        }


        protected abstract void executePerBundle ( Bundle b, Session ci );
    }

    /**
     * Update bundle via bundle updater
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "update", description = "Update bundles (bundle updater)" )
    public class UpdateBundlesCommand implements Action {

        @Argument ( index = 0, name = "bundles", multiValued = true, required = true )
        @Completion ( BundleCompleter.class )
        protected List<String> bundles = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () throws BundleException {

            List<Bundle> realBundles = new ArrayList<>();
            for ( String bundleSpec : this.bundles ) {
                realBundles.add(BundleCommandUtil.findBundle(getBundleContext(), bundleSpec));
            }

            try {
                getBundleUpdater().updateBundles(realBundles);
            }
            catch ( BundleException e ) {
                this.session.getConsole().println("Failed to update bundles: " + e.getMessage()); //$NON-NLS-1$
                getLog().error(String.format("Failed to update bundles " + this.bundles), e); //$NON-NLS-1$
            }
            return null;
        }

    }

    /**
     * Update bundle
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "updateNative", description = "Update bundles (osgi native)" )
    public class UpdateBundlesNativeCommand extends AbstractPerBundleCommand {

        /**
         * {@inheritDoc}
         * 
         * @see eu.agno3.runtime.console.osgi.internal.BundleCommandProvider.AbstractPerBundleCommand#executePerBundle(org.osgi.framework.Bundle)
         */
        @Override
        protected void executePerBundle ( Bundle b, Session ci ) {
            try {
                getLog().info(String.format("Updating bundle %s-%s", b.getSymbolicName(), BundleCommandUtil.formatVersion(b.getVersion()))); //$NON-NLS-1$
                b.update();
            }
            catch ( BundleException e ) {
                getLog().error(
                    String.format("Failed to update bundle %s-%s:", b.getSymbolicName(), BundleCommandUtil.formatVersion(b.getVersion())), //$NON-NLS-1$
                    e);
            }
        }

    }

    /**
     * Activate bundle
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "start", description = "Activate bundle" )
    public class StartCommand extends AbstractPerBundleCommand {

        /**
         * {@inheritDoc}
         * 
         * @see eu.agno3.runtime.console.osgi.internal.BundleCommandProvider.AbstractPerBundleCommand#executePerBundle(org.osgi.framework.Bundle)
         */
        @Override
        protected void executePerBundle ( Bundle b, Session ci ) {
            getLog().info("Starting bundle " + b.getSymbolicName()); //$NON-NLS-1$
            try {
                b.start();
            }
            catch ( Exception e ) {
                getLog().error(String.format("Failed to start bundle '%s':", b.getSymbolicName()), e); //$NON-NLS-1$
            }
        }

    }

    /**
     * Deactivate bundle
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "stop", description = "Deactivate bundle" )
    public class StopCommand extends AbstractPerBundleCommand {

        /**
         * {@inheritDoc}
         * 
         * @see eu.agno3.runtime.console.osgi.internal.BundleCommandProvider.AbstractPerBundleCommand#executePerBundle(org.osgi.framework.Bundle)
         */
        @Override
        protected void executePerBundle ( Bundle b, Session ci ) {
            getLog().info("Stopping bundle " + b.getSymbolicName()); //$NON-NLS-1$
            try {
                b.stop();
            }
            catch ( Exception e ) {
                getLog().error(String.format("Failed to stop bundle '%s':", b.getSymbolicName()), e); //$NON-NLS-1$
            }
        }

    }

    /**
     * Uninstall bundle
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "uninstall", description = "Uninstall bundle" )
    public class UninstallCommand extends AbstractPerBundleCommand {

        /**
         * {@inheritDoc}
         * 
         * @see eu.agno3.runtime.console.osgi.internal.BundleCommandProvider.AbstractPerBundleCommand#executePerBundle(org.osgi.framework.Bundle)
         */
        @Override
        protected void executePerBundle ( Bundle b, Session ci ) {
            getLog().info("Uninstalling bundle " + b.getSymbolicName()); //$NON-NLS-1$
            try {
                b.uninstall();
            }
            catch ( Exception e ) {
                getLog().error(String.format("Failed to uninstall bundle '%s':", b.getSymbolicName()), e); //$NON-NLS-1$
            }
        }

    }

    /**
     * Refresh bundle
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "refreshNative", description = "Refresh bundle (osgi native)" )
    public class RefreshNativeCommand implements Action {

        @Option ( name = "all", description = "Refresh all bundles" )
        private boolean all;

        @Argument ( index = 0, name = "bundles", multiValued = true, required = false )
        @Completion ( BundleCompleter.class )
        private List<String> bundles = null;


        @Override
        public Object execute () throws BundleException {

            Bundle systemBundle = getBundleContext().getBundle(0);
            FrameworkWiring wiring = systemBundle.adapt(FrameworkWiring.class);

            if ( this.bundles == null || this.all ) {
                getLog().info("Refreshing all bundles"); //$NON-NLS-1$
                wiring.refreshBundles(Arrays.asList(getBundleContext().getBundles()));
            }
            else {
                List<Bundle> toRefresh = new ArrayList<>();

                for ( String bundleSpec : this.bundles ) {
                    Bundle b = BundleCommandUtil.findBundle(getBundleContext(), bundleSpec);
                    getLog().debug("Refresh bundle: " + b.getSymbolicName()); //$NON-NLS-1$
                    toRefresh.add(b);
                }

                wiring.refreshBundles(toRefresh);
            }

            return null;
        }

    }

    /**
     * Print bundle manifest
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bundles", name = "manifest", description = "Show bundle manifest" )
    public class ManifestCommand extends AbstractPerBundleCommand {

        /**
         * {@inheritDoc}
         * 
         * @see eu.agno3.runtime.console.osgi.internal.BundleCommandProvider.AbstractPerBundleCommand#executePerBundle(org.osgi.framework.Bundle)
         */
        @Override
        protected void executePerBundle ( Bundle b, Session ci ) {
            Enumeration<String> headers = b.getHeaders().keys();

            Ansi out = Ansi.ansi();

            while ( headers.hasMoreElements() ) {
                String header = headers.nextElement();
                String value = b.getHeaders().get(header);

                out.bold().a(String.format("%s: ", header)).boldOff(); //$NON-NLS-1$
                out.a(value);
                out.a(System.lineSeparator());
            }

            ci.getConsole().print(out.toString());
        }

    }

}
