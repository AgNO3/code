/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;


/**
 * @author mbechler
 *
 */
@Component ( service = Completer.class )
public class PackageCompleter implements Completer {

    private BundleContext bundleContext;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.bundleContext = ctx.getBundleContext();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.bundleContext = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Completer#complete(org.apache.karaf.shell.api.console.Session,
     *      org.apache.karaf.shell.api.console.CommandLine, java.util.List)
     */
    @Override
    public int complete ( Session session, CommandLine commandLine, List<String> candidates ) {
        StringsCompleter comp = new StringsCompleter(completePackages(this.bundleContext));
        return comp.complete(session, commandLine, candidates);
    }


    protected static Set<String> completePackages ( BundleContext context ) {
        Set<String> res = new HashSet<>();
        Bundle[] bundles = context.getBundles();

        for ( Bundle b : bundles ) {
            BundleWiring wiring = b.adapt(BundleWiring.class);
            if ( wiring == null ) {
                continue;
            }
            List<BundleCapability> exports = wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);

            for ( BundleCapability export : exports ) {
                String packageName = export.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE).toString();
                res.add(packageName);
            }
        }

        return res;
    }
}
