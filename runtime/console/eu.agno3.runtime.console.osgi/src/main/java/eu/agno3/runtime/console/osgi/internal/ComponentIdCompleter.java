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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;


/**
 * @author mbechler
 *
 */
@org.osgi.service.component.annotations.Component ( service = Completer.class )
public class ComponentIdCompleter implements Completer {

    private ServiceComponentRuntime scrService;


    @Reference
    protected synchronized void setScrService ( ServiceComponentRuntime service ) {
        this.scrService = service;
    }


    protected synchronized void unsetScrService ( ServiceComponentRuntime service ) {
        if ( this.scrService == service ) {
            this.scrService = null;
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
        StringsCompleter comp = new StringsCompleter(completeComponentIds(this.scrService));
        return comp.complete(session, commandLine, candidates);
    }


    protected Set<String> completeComponentIds ( ServiceComponentRuntime s ) {
        Set<String> res = new HashSet<>();

        for ( ComponentDescriptionDTO c : s.getComponentDescriptionDTOs() ) {
            res.add(c.name);
        }

        return res;
    }
}
