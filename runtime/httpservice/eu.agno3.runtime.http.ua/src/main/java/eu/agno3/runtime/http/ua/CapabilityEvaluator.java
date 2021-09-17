/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.runtime.http.ua;


import net.sf.uadetector.ReadableUserAgent;


/**
 * @author mbechler
 *
 */
public interface CapabilityEvaluator {

    /**
     * @param ua
     * @param raw
     * @return whether the capability is supported
     */
    boolean hasCapability ( ReadableUserAgent ua, String raw );

}