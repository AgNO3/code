/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.05.2014 by mbechler
 */
package eu.agno3.runtime.util.osgi;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;


/**
 * @author mbechler
 * 
 */
public final class BundleUtil {

    private static final Logger log = Logger.getLogger(BundleUtil.class);


    private BundleUtil () {

    }


    /**
     * @param bundle
     * @return the bundles imported via RequireBundle
     */
    public static List<Bundle> getRequiredBundles ( Bundle bundle ) {
        BundleWiring w = bundle.adapt(BundleWiring.class);
        List<Bundle> bundles = new ArrayList<>();

        if ( w != null ) {
            List<BundleWire> requirements = w.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);

            for ( BundleWire req : requirements ) {
                BundleRevision reqBundle = req.getProvider();
                if ( log.isDebugEnabled() ) {
                    log.debug("Found required bundle: " + reqBundle.getSymbolicName()); //$NON-NLS-1$
                }
                bundles.add(reqBundle.getBundle());
            }
        }
        return bundles;
    }
}
