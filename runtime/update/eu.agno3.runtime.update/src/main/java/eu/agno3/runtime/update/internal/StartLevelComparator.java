/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2014 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.Serializable;
import java.util.Comparator;

import org.osgi.framework.Bundle;
import org.osgi.framework.startlevel.BundleStartLevel;


/**
 * @author mbechler
 *
 */
public class StartLevelComparator implements Comparator<Bundle>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5009655043073320518L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( Bundle a, Bundle b ) {
        BundleStartLevel aLevel = a.adapt(BundleStartLevel.class);
        BundleStartLevel bLevel = b.adapt(BundleStartLevel.class);

        if ( aLevel == null && bLevel == null ) {
            return 0;
        }
        else if ( aLevel == null ) {
            return 1;
        }
        else if ( bLevel == null ) {
            return -1;
        }

        return -1 * Integer.compare(aLevel.getStartLevel(), bLevel.getStartLevel());
    }

}
