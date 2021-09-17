/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.runtime.db.schema.console;


import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jdbc.DataSourceFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = Completer.class )
public class DataSourceCompleter implements Completer {

    private static final Logger log = Logger.getLogger(DataSourceCompleter.class);
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
        StringsCompleter comp = new StringsCompleter(getDatasources());
        return comp.complete(session, commandLine, candidates);
    }


    /**
     * @return
     */
    Set<String> getDatasources () {
        SortedSet<String> res = new TreeSet<>();

        try {
            Collection<ServiceReference<DataSource>> dataSources = this.bundleContext.getServiceReferences(DataSource.class, null);

            for ( ServiceReference<DataSource> ds : dataSources ) {
                res.add((String) ds.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME));
            }
        }
        catch ( InvalidSyntaxException e ) {
            log.error("Illegal filter:", e); //$NON-NLS-1$
        }

        return res;
    }

}
