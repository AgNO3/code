/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.07.2015 by mbechler
 */
package eu.agno3.runtime.http.ua.caps;


import net.sf.uadetector.ReadableUserAgent;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.http.ua.CapabilityEvaluator;


/**
 * @author mbechler
 *
 */
public class NoMultiAuthCapabilityEvaluator implements CapabilityEvaluator {

    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.ua.CapabilityEvaluator#hasCapability(net.sf.uadetector.ReadableUserAgent,
     *      java.lang.String)
     */
    @Override
    public boolean hasCapability ( ReadableUserAgent ua, String raw ) {
        if ( !StringUtils.isBlank(raw) && ( raw.contains("ownCloud-android") || //$NON-NLS-1$
                raw.contains("iOS-ownCloud") ) ) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

}
