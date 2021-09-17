/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2014 by mbechler
 */
package eu.agno3.runtime.update;


import java.util.Collection;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;


/**
 * @author mbechler
 *
 */
public interface BundleUpdater {

    /**
     * @param bundles
     * @throws BundleException
     */
    void updateBundles ( Collection<Bundle> bundles ) throws BundleException;

}
