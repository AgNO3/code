/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.types.uri;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
@Named ( "uriUtil" )
@ApplicationScoped
public class URIUtil {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$


    /**
     * 
     * @param data
     * @return encoded param
     */
    public static String encodeParam ( String data ) {
        try {
            return URLEncoder.encode(data, UTF_8);
        }
        catch ( UnsupportedEncodingException e ) {
            throw new FacesException(e);
        }
    }


    /**
     * 
     * @param data
     * @return decoded param
     */
    public static String decodeParam ( String data ) {
        try {
            return URLDecoder.decode(data, UTF_8);
        }
        catch ( UnsupportedEncodingException e ) {
            throw new FacesException(e);
        }
    }


    /**
     * 
     * @return the context base uri
     */
    public static String getCurrentBaseUri () {
        return getCurrentBaseUri(false);
    }


    /**
     * 
     * @return the context base urim, with a trailing slash
     */
    public static String getCurrentBaseUriWithTrailingSlash () {
        return getCurrentBaseUri(true);
    }


    /**
     * @return
     */
    private static String getCurrentBaseUri ( boolean trailingSlash ) {
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ectx.getRequest();
        StringBuilder sb = new StringBuilder();
        buildContextBase(req, sb);
        sb.append(req.getServletContext().getContextPath());
        if ( sb.charAt(sb.length() - 1) != '/' ) {
            sb.append('/');
        }
        return sb.toString();
    }


    /**
     * 
     * @return the full request uri
     */
    public static String getFullRequestUri () {
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ectx.getRequest();
        StringBuilder sb = new StringBuilder();
        buildContextBase(req, sb);
        sb.append(req.getRequestURI());

        if ( !StringUtils.isBlank(req.getQueryString()) ) {
            sb.append('?').append(req.getQueryString());
        }
        return sb.toString();
    }


    /**
     * 
     * @return the full original request uri
     */
    public static String getSavedFullRequestUri () {
        return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
                .get(URLPostRestoreViewPhaseListener.AGNO3_ORIGINAL_REQUEST_URI);

    }


    /**
     * 
     * @return the full request uri
     */
    public static String getFullRequestUriWithoutConversation () {
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ectx.getRequest();
        StringBuilder sb = new StringBuilder();
        buildContextBase(req, sb);
        sb.append(req.getRequestURI());

        if ( !StringUtils.isBlank(req.getQueryString()) ) {
            String dropCID = dropCID(req.getQueryString());
            if ( !StringUtils.isBlank(dropCID) ) {
                sb.append('?').append(dropCID);
            }
        }
        return sb.toString();
    }


    /**
     * 
     * @return the full original request uri without a cid
     */
    public static String getSavedFullRequestWithoutConversation () {
        String uri = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
                .get(URLPostRestoreViewPhaseListener.AGNO3_ORIGINAL_REQUEST_URI);

        if ( StringUtils.isBlank(uri) ) {
            return uri;
        }

        return dropCID(uri);
    }


    /**
     * @param queryString
     * @return
     */
    private static String dropCID ( String queryString ) {
        int cidPos = queryString.indexOf("&cid="); //$NON-NLS-1$

        if ( cidPos < 0 ) {
            cidPos = queryString.indexOf("?cid="); //$NON-NLS-1$
        }

        if ( cidPos < 0 && queryString.startsWith("cid=") ) { //$NON-NLS-1$
            int cidEnd = queryString.indexOf('&', cidPos + 1);
            if ( cidEnd < 0 ) {
                return StringUtils.EMPTY;
            }

            return queryString.substring(cidEnd + 1);
        }

        if ( cidPos < 0 ) {
            return queryString;
        }

        int cidEnd = queryString.indexOf('&', cidPos + 1);
        if ( cidEnd < 0 ) {
            return queryString.substring(0, cidPos);
        }
        return queryString.substring(0, cidPos).concat(queryString.substring(cidEnd));
    }


    /**
     * @param req
     * @param sb
     */
    private static void buildContextBase ( HttpServletRequest req, StringBuilder sb ) {
        String proto;
        int defPort;
        if ( req.isSecure() ) {
            proto = "https"; //$NON-NLS-1$
            defPort = 443;
        }
        else {
            proto = "http"; //$NON-NLS-1$
            defPort = 80;
        }

        sb.append(proto).append("://"); //$NON-NLS-1$

        String requestedHostName = req.getServerName();
        if ( !StringUtils.isBlank(requestedHostName) ) {
            sb.append(requestedHostName);
        }
        else {
            sb.append(req.getLocalName());
        }

        int port = req.getServerPort();
        if ( port != defPort ) {
            sb.append(':').append(String.valueOf(port));
        }

    }
}
