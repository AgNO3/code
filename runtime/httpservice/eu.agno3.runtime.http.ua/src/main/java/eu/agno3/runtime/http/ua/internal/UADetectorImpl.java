/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.runtime.http.ua.internal;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.http.ua.UACapability;
import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.http.ua.caps.UACapabilities;
import eu.agno3.runtime.http.ua.detector.DetectorFactory;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;


/**
 * @author mbechler
 *
 */
@Component ( service = UADetector.class )
public class UADetectorImpl implements UADetector {

    private static final int CACHE_SIZE = 128;

    private static final Logger log = Logger.getLogger(UADetectorImpl.class);

    private Map<String, ReadableUserAgent> uacache = new LRUMap<>(CACHE_SIZE);

    private UserAgentStringParser parser;


    /**
     * 
     */
    public UADetectorImpl () {}


    /**
     * @param parser
     * 
     */
    public UADetectorImpl ( UserAgentStringParser parser ) {
        this.parser = parser;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.parser = DetectorFactory.getParser();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.ua.UADetector#parse(java.lang.String)
     */
    @Override
    public ReadableUserAgent parse ( String userAgent ) {
        if ( this.parser == null ) {
            return null;
        }

        ReadableUserAgent cached = this.uacache.get(userAgent);

        if ( cached != null ) {
            return cached;
        }

        cached = this.parser.parse(userAgent);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("UA '%s' -> %s", userAgent, cached)); //$NON-NLS-1$
        }

        this.uacache.put(userAgent, cached);
        return cached;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.ua.UADetector#parse(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public ReadableUserAgent parse ( HttpServletRequest req ) {
        String uaHeader = getUA(req);
        if ( StringUtils.isBlank(uaHeader) ) {
            return null;
        }
        return this.parse(uaHeader);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.ua.UADetector#getUA(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getUA ( HttpServletRequest req ) {
        return req.getHeader("User-Agent"); //$NON-NLS-1$ ;
    }


    @Override
    public boolean hasCapability ( UACapability cap, ReadableUserAgent ua, String raw ) {
        if ( ua == null ) {
            return false;
        }

        return UACapabilities.hasCapability(cap, ua, raw);
    }

}
