/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.runtime.logging.console.internal;


import java.util.List;

import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.logging.LogConfigurationService;


/**
 * @author mbechler
 *
 */
@Component ( service = Completer.class )
public class LogScopeCompleter implements Completer {

    private LogConfigurationService logConfig;


    @Reference
    protected synchronized void setLogConfig ( LogConfigurationService lcs ) {
        this.logConfig = lcs;
    }


    protected synchronized void unsetLogConfig ( LogConfigurationService lcs ) {
        if ( this.logConfig == lcs ) {
            this.logConfig = null;
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
        StringsCompleter comp = new StringsCompleter(this.logConfig.listOverrides().keySet());
        return comp.complete(session, commandLine, candidates);
    }

}
