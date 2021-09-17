/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.filter;


import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * @author mbechler
 *
 */
public class NoURLSesssionResponseWrapper extends HttpServletResponseWrapper implements ServletResponse {

    /**
     * @param response
     */
    public NoURLSesssionResponseWrapper ( HttpServletResponse response ) {
        super(response);
    }


    @Override
    public String encodeRedirectUrl ( String url ) {
        return url;
    }


    @Override
    public String encodeRedirectURL ( String url ) {
        return url;
    }


    @Override
    public String encodeUrl ( String url ) {
        return url;
    }


    @Override
    public String encodeURL ( String url ) {
        return url;
    }

}
