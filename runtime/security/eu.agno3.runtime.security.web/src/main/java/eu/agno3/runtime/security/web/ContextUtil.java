/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.runtime.security.web;


import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.web.login.WebLoginConfig;
import eu.agno3.runtime.security.web.login.WebLoginContextImpl;


/**
 * @author mbechler
 *
 */
public final class ContextUtil {

    /**
     * 
     */
    private ContextUtil () {}


    /**
     * @param req
     * @return the requested host name
     */
    public static String getRequestedHostName ( HttpServletRequest req ) {
        String host = req.getHeader("Host"); //$NON-NLS-1$
        if ( StringUtils.isBlank(host) ) {
            return host;
        }
        int portPos = host.indexOf(':');
        if ( portPos >= 0 ) {
            host = host.substring(0, portPos);
        }
        return host;
    }


    /**
     * @param config
     * @param req
     * @param httpAuth
     *            whether this is http authentication
     * @return a login context
     */
    public static LoginContext makeLoginContext ( WebLoginConfig config, HttpServletRequest req, boolean httpAuth ) {
        return makeLoginContext(config, req, httpAuth, Locale.ROOT);
    }


    /**
     * @param config
     * @param req
     * @param httpAuth
     *            whether this is http authentication
     * @param l
     *            user locale
     * @return a login context
     */
    public static LoginContext makeLoginContext ( WebLoginConfig config, HttpServletRequest req, boolean httpAuth, Locale l ) {
        String userAgent = req.getHeader("User-Agent"); //$NON-NLS-1$
        String remoteAddr = req.getRemoteAddr();
        boolean secureTransport = req.isSecure();
        String localHostname = getRequestedHostName(req);
        if ( StringUtils.isBlank(localHostname) ) {
            localHostname = req.getLocalAddr();
        }

        WebLoginContextImpl wctx = new WebLoginContextImpl(
            config,
            userAgent,
            remoteAddr,
            localHostname,
            req.getLocalAddr(),
            req.getServletContext().getContextPath(),
            req.getServerPort(),
            secureTransport,
            httpAuth);

        return wctx;
    }

}
