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
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 *
 */
@Component ( service = Completer.class )
public class PIDCompleter implements Completer {

    private ConfigurationAdmin configAdmin;


    @Reference
    protected synchronized void setConfigAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    protected synchronized void unsetConfigAdmin ( ConfigurationAdmin cm ) {
        if ( this.configAdmin == cm ) {
            this.configAdmin = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Completer#complete(org.apache.karaf.shell.api.console.Session,
     *      org.apache.karaf.shell.api.console.CommandLine, java.util.List)
     */
    @Override
    public int complete ( Session session, CommandLine commandLine, List<String> candidates ) {
        StringsCompleter comp = new StringsCompleter(ConfigAdminCommandProvider.completePid(this.configAdmin));
        return comp.complete(session, commandLine, candidates);
    }
}
