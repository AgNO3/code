/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 4, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.runtime.security.cas.client.CasAuthConfiguration;


/**
 * @author mbechler
 *
 */
public class CustomPassThruAuthenticationFilter extends PassThruAuthenticationFilter {

    private CasAuthConfiguration authConfig;


    /**
     * @param authConfig
     */
    public CustomPassThruAuthenticationFilter ( CasAuthConfiguration authConfig ) {
        this.authConfig = authConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.AccessControlFilter#getLoginUrl()
     */
    @Override
    public String getLoginUrl () {
        URLCodec codec = new URLCodec();
        try {
            HttpServletRequest req = WebUtils.getHttpRequest(SecurityUtils.getSubject());
            return this.authConfig.getAuthServerBase(req.getServerName()) + "login?service=" + //$NON-NLS-1$
                    codec.encode(this.authConfig.getLocalService());
        }
        catch ( EncoderException e ) {
            throw new RuntimeException(e);
        }
    }
}
