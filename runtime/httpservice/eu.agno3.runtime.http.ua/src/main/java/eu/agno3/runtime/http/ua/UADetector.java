/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.runtime.http.ua;


import javax.servlet.http.HttpServletRequest;

import net.sf.uadetector.ReadableUserAgent;


/**
 * @author mbechler
 *
 */
public interface UADetector {

    /**
     * @param userAgent
     * @return the parsed user agent string
     */
    public abstract ReadableUserAgent parse ( String userAgent );


    /**
     * @param req
     * @return the parsed request user agent
     */
    public abstract ReadableUserAgent parse ( HttpServletRequest req );


    /**
     * @param req
     * @return the user agent
     */
    String getUA ( HttpServletRequest req );


    /**
     * @param cap
     * @param ua
     * @param raw
     * @return whether the user agent has the given capability
     */
    boolean hasCapability ( UACapability cap, ReadableUserAgent ua, String raw );

}