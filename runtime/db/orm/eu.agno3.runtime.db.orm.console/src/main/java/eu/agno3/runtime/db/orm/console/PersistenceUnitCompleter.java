/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.console;


import java.util.List;

import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.orm.DynamicPersistenceProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = Completer.class )
public class PersistenceUnitCompleter implements Completer {

    private DynamicPersistenceProvider persistenceProvider;


    @Reference
    protected synchronized void setDynamicPersistenceProvider ( DynamicPersistenceProvider prov ) {
        this.persistenceProvider = prov;
    }


    protected synchronized void unsetDynamicPersistenceProvider ( DynamicPersistenceProvider prov ) {
        if ( this.persistenceProvider == prov ) {
            this.persistenceProvider = null;
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
        StringsCompleter comp = new StringsCompleter(this.persistenceProvider.getPersistenceUnits());
        return comp.complete(session, commandLine, candidates);
    }

}
