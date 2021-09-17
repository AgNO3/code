/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;

import org.apache.xbean.osgi.bundle.util.ClassDiscoveryFilter;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;

/**
 * @author mbechler
 * 
 */
public class BundleClasspathFilter implements ClassDiscoveryFilter {

    @Override
    public boolean directoryDiscoveryRequired ( String directory ) {
        return true;
    }


    @Override
    public boolean jarFileDiscoveryRequired ( String jarUrl ) {
        return false;
    }


    @Override
    public boolean packageDiscoveryRequired ( String packageName ) {
        return true;
    }


    @Override
    public boolean rangeDiscoveryRequired ( DiscoveryRange discoveryRange ) {
        return discoveryRange.equals(DiscoveryRange.BUNDLE_CLASSPATH) || discoveryRange.equals(DiscoveryRange.FRAGMENT_BUNDLES);
    }
}