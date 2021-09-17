/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 17, 2017 by mbechler
 */
package eu.agno3.runtime.http.service.internal.proxy;


import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.jetty.util.URIUtil;


/**
 * @author mbechler
 *
 */
public class ProxyHttpResponseWrapper extends HttpServletResponseWrapper {

    private HttpServletRequest request;


    /**
     * @param response
     * @param request
     */
    public ProxyHttpResponseWrapper ( HttpServletResponse response, HttpServletRequest request ) {
        super(response);
        this.request = request;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
     */
    @Override
    public void sendRedirect ( String location ) throws IOException {
        if ( URIUtil.hasScheme(location) ) {
            super.sendRedirect(location);
            return;
        }

        try {
            URI mod = new URI(
                this.request.isSecure() ? "https" : //$NON-NLS-1$
                        "http", //$NON-NLS-1$
                null,
                URLEncoder.encode(IDN.toASCII(this.request.getServerName()), "UTF-8"), //$NON-NLS-1$
                ( this.request.isSecure() && this.request.getServerPort() != 443 )
                        || ( !this.request.isSecure() && this.request.getServerPort() != 80 ) ? this.request.getServerPort() : -1,
                location,
                null,
                null);
            super.sendRedirect(mod.toASCIIString());
        }
        catch ( URISyntaxException e ) {
            throw new IOException("Failed to construct redirect URI", e); //$NON-NLS-1$
        }
    }

}
