/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 10, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.route;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.info.network.RouteEntry;


/**
 * @author mbechler
 *
 */
public class RouteInfoProvider {

    private static final Logger log = Logger.getLogger(RouteInfoProvider.class);

    private V4RouteParser v4parser = new V4RouteParser();
    private V6RouteParser v6parser = new V6RouteParser();


    /**
     * @return active routes
     */
    public List<RouteEntry> getRoutes () {
        List<RouteEntry> routes = new ArrayList<>();

        try {
            routes.addAll(this.v4parser.parse());
        }
        catch ( IOException e ) {
            log.warn("Failed to get V4 routes", e); //$NON-NLS-1$
        }
        try {
            routes.addAll(this.v6parser.parse());
        }
        catch ( IOException e ) {
            log.warn("Failed to get V6 routes", e); //$NON-NLS-1$
        }

        Collections.sort(routes, new RouteEntryComparator());

        return routes;
    }
}
