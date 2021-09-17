/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.util.List;

import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;


/**
 * @author mbechler
 *
 */
@Component ( service = Completer.class )
public class BundleCompleter implements Completer {

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
        StringsCompleter comp = new StringsCompleter(BundleCommandUtil.completeBundles(this.bundleContext));
        return comp.complete(session, commandLine, candidates);
    }

}
