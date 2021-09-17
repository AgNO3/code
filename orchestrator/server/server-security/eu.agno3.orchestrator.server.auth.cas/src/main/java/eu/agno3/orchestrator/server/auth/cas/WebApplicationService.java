/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas;


import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jasig.cas.authentication.principal.DefaultResponse;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.SingleLogoutService;
import org.jasig.cas.validation.ValidationResponseType;


/**
 * 
 * Cloned from CAS: Original implementation holds a reference to the Logger
 * 
 * @author mbechler
 *
 */
public class WebApplicationService implements SingleLogoutService {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -716451045731241064L;

    private static final Logger log = Logger.getLogger(WebApplicationService.class);
    private static final Map<String, Object> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<String, Object>());

    private static final String CONST_PARAM_SERVICE = "service"; //$NON-NLS-1$
    private static final String CONST_PARAM_TARGET_SERVICE = "targetService"; //$NON-NLS-1$
    private static final String CONST_PARAM_TICKET = "ticket"; //$NON-NLS-1$
    private static final String CONST_PARAM_METHOD = "method"; //$NON-NLS-1$

    /** The id of the service. */
    private final String id;

    /** The original url provided, used to reconstruct the redirect url. */
    private final String originalUrl;

    private final String artifactId;

    private Principal principal;

    private boolean loggedOutAlready;

    private final Response.ResponseType responseType;


    /**
     * Instantiates a new simple web application service impl.
     *
     * @param id
     *            the id
     */
    public WebApplicationService ( final String id ) {
        this(id, id, null, null);
    }


    /**
     * Instantiates a new simple web application service impl.
     *
     * @param id
     *            the id
     * @param originalUrl
     *            the original url
     * @param artifactId
     *            the artifact id
     * @param responseType
     *            the response type
     */
    private WebApplicationService ( final String id, final String originalUrl, final String artifactId, final Response.ResponseType responseType ) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.artifactId = artifactId;
        this.responseType = responseType;
    }


    @Override
    public final String toString () {
        return this.id;
    }


    @Override
    public final String getId () {
        return this.id;
    }


    @Override
    public final String getArtifactId () {
        return this.artifactId;
    }


    @Override
    public final Map<String, Object> getAttributes () {
        return EMPTY_MAP;
    }


    /**
     * Cleanup the url. Removes jsession ids and query strings.
     *
     * @param url
     *            the url
     * @return sanitized url.
     */
    protected static String cleanupUrl ( final String url ) {
        if ( url == null ) {
            return null;
        }

        final int jsessionPosition = url.indexOf(";jsession"); //$NON-NLS-1$

        if ( jsessionPosition == -1 ) {
            return url;
        }

        final int questionMarkPosition = url.indexOf('?');

        if ( questionMarkPosition < jsessionPosition ) {
            return url.substring(0, url.indexOf(";jsession")); //$NON-NLS-1$
        }

        return url.substring(0, jsessionPosition) + url.substring(questionMarkPosition);
    }


    /**
     * Return the original url provided (as <code>service</code> or <code>targetService</code> request parameter).
     * Used to reconstruct the redirect url.
     *
     * @return the original url provided.
     */
    @Override
    public final String getOriginalUrl () {
        return this.originalUrl;
    }


    @Override
    public boolean equals ( final Object object ) {
        if ( object == null ) {
            return false;
        }

        if ( object instanceof Service ) {
            final Service service = (Service) object;

            return getId().equals(service.getId());
        }

        return false;
    }


    @Override
    public int hashCode () {
        return this.id == null ? 0 : this.id.hashCode();
    }


    protected Principal getPrincipal () {
        return this.principal;
    }


    @Override
    public void setPrincipal ( final Principal principal ) {
        this.principal = principal;
    }


    @Override
    public boolean matches ( final Service service ) {
        try {
            final String thisUrl = URLDecoder.decode(this.id, UTF_8);
            final String serviceUrl = URLDecoder.decode(service.getId(), UTF_8);

            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Decoded urls and comparing [%s] with [%s]", thisUrl, serviceUrl)); //$NON-NLS-1$
            }
            return thisUrl.equalsIgnoreCase(serviceUrl);
        }
        catch ( final Exception e ) {
            log.error(e.getMessage(), e);
        }
        return false;
    }


    /**
     * Return if the service is already logged out.
     *
     * @return if the service is already logged out.
     */
    @Override
    public boolean isLoggedOutAlready () {
        return this.loggedOutAlready;
    }


    /**
     * Set if the service is already logged out.
     *
     * @param loggedOutAlready
     *            if the service is already logged out.
     */
    @Override
    public final void setLoggedOutAlready ( final boolean loggedOutAlready ) {
        this.loggedOutAlready = loggedOutAlready;
    }


    @Override
    public Response getResponse ( final String ticketId ) {
        final Map<String, String> parameters = new HashMap<>();

        if ( !StringUtils.isBlank(ticketId) ) {
            parameters.put(CONST_PARAM_TICKET, ticketId);
        }

        if ( Response.ResponseType.POST == this.responseType ) {
            return DefaultResponse.getPostResponse(getOriginalUrl(), parameters);
        }
        return DefaultResponse.getRedirectResponse(getOriginalUrl(), parameters);
    }


    /**
     * Creates the service from the request.
     *
     * @param request
     *            the request
     * @return the simple web application service impl
     */
    public static SingleLogoutService createServiceFrom ( final HttpServletRequest request ) {
        final String targetService = request.getParameter(CONST_PARAM_TARGET_SERVICE);
        final String service = request.getParameter(CONST_PARAM_SERVICE);
        final String serviceAttribute = (String) request.getAttribute(CONST_PARAM_SERVICE);
        final String method = request.getParameter(CONST_PARAM_METHOD);
        final String serviceToUse;
        if ( !StringUtils.isBlank(targetService) ) {
            serviceToUse = targetService;
        }
        else if ( !StringUtils.isBlank(service) ) {
            serviceToUse = service;
        }
        else {
            serviceToUse = serviceAttribute;
        }

        if ( StringUtils.isBlank(serviceToUse) ) {
            return null;
        }

        final String id = cleanupUrl(serviceToUse);
        final String artifactId = request.getParameter(CONST_PARAM_TICKET);

        return new WebApplicationService(id, serviceToUse, artifactId, "POST".equals(method) ? Response.ResponseType.POST //$NON-NLS-1$
                : Response.ResponseType.REDIRECT);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.principal.WebApplicationService#getFormat()
     */
    @Override
    public ValidationResponseType getFormat () {
        return ValidationResponseType.XML;
    }

}
