/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.ua;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import net.sf.uadetector.ReadableUserAgent;

import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.http.ua.UACapability;


/**
 * @author mbechler
 *
 */
@SessionScoped
@Named ( "userAgentBean" )
public class UserAgentBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4875532023181701000L;

    private ReadableUserAgent userAgent;
    private boolean userAgentLoaded;

    private Map<UACapability, Boolean> cachedCaps = new HashMap<>();

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @return the parsed user agent
     */
    public ReadableUserAgent getUserAgent () {
        if ( !this.userAgentLoaded ) {
            this.userAgentLoaded = true;
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            this.userAgent = this.fsp.getUserAgentDetector().parse((HttpServletRequest) externalContext.getRequest());
        }
        return this.userAgent;
    }


    /**
     * @return raw user agent string
     */
    public String getRawUserAgent () {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        return this.fsp.getUserAgentDetector().getUA((HttpServletRequest) externalContext.getRequest());
    }


    /**
     * @param cap
     * @return whether the user's UA has the given capability
     */
    public boolean hasCapability ( UACapability cap ) {
        Boolean cached = this.cachedCaps.get(cap);
        if ( cached != null ) {
            return cached;
        }

        ReadableUserAgent ua = this.getUserAgent();
        boolean haveCap = this.fsp.getUserAgentDetector().hasCapability(cap, ua, getRawUserAgent());
        this.cachedCaps.put(cap, haveCap);
        return haveCap;
    }
}
