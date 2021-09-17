/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login.token;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

import eu.agno3.runtime.security.token.RealmTokenToken;


/**
 * @author mbechler
 *
 */
public class URLTokenAuthenticationFilter extends AuthenticatingFilter {

    /**
     * 
     */
    private static final String TOKEN = "token"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(URLTokenAuthenticationFilter.class);

    private static final long MAX_TOKEN_SIZE = 8192;

    private TokenGenerator tokValidator;


    /**
     * @param validator
     *            Token validator
     * 
     */
    public URLTokenAuthenticationFilter ( TokenGenerator validator ) {
        this.tokValidator = validator;
        setName("urlTokenFilter"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServletException
     * @throws IOException
     *
     * @see org.apache.shiro.web.filter.authc.AuthenticatingFilter#createToken(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected RealmTokenToken createToken ( ServletRequest req, ServletResponse resp ) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        // simple getParameter will break MultiPartConfig
        String tokenParam = parseToken(httpReq);

        if ( StringUtils.isBlank(tokenParam) ) {
            return null;
        }

        tokenParam = tokenParam.trim();
        if ( log.isDebugEnabled() ) {
            log.debug("Found token " + tokenParam); //$NON-NLS-1$
        }

        return RealmTokenToken.createFromString(tokenParam);
    }


    /**
     * @param httpReq
     * @return
     * @throws ServletException
     * @throws IOException
     */
    private static String parseToken ( HttpServletRequest httpReq ) throws IOException, ServletException {

        if ( "POST".equals(httpReq.getMethod()) ) { //$NON-NLS-1$
            log.debug("Is POST request"); //$NON-NLS-1$
            String contentType = httpReq.getHeader("Content-Type"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(contentType) && contentType.startsWith("application/x-www-form-urlencoded") ) { //$NON-NLS-1$
                log.debug("Is form encoded request"); //$NON-NLS-1$
                String parameter = httpReq.getParameter(TOKEN);
                if ( !StringUtils.isBlank(parameter) ) {
                    log.debug("Found token in form data"); //$NON-NLS-1$
                    try {
                        return decodeToken(parameter);
                    }
                    catch ( UnsupportedEncodingException e ) {
                        log.warn("Failed to decode POST token parameter", e); //$NON-NLS-1$
                        return null;
                    }
                }

                log.debug("No token found in form data"); //$NON-NLS-1$
            }
            else if ( !StringUtils.isBlank(contentType) && contentType.startsWith("multipart/form-data") ) { //$NON-NLS-1$
                log.debug("Multipart Form data"); //$NON-NLS-1$
                for ( Part p : httpReq.getParts() ) {
                    if ( !StringUtils.isBlank(p.getName()) && TOKEN.equals(p.getName()) ) {

                        if ( p.getSize() > MAX_TOKEN_SIZE ) {
                            throw new IOException("Token size exceeds limit"); //$NON-NLS-1$
                        }

                        byte[] tokenBytes = new byte[(int) p.getSize()];
                        int read = 0;

                        while ( read < p.getSize() ) {
                            read += p.getInputStream().read(tokenBytes, read, (int) ( p.getSize() - read ));
                        }
                        p.getInputStream().reset();
                        String token = new String(tokenBytes, Charset.forName("US-ASCII")); //$NON-NLS-1$

                        log.debug("Found token in MIME data"); //$NON-NLS-1$
                        return decodeToken(token);
                    }
                }

            }
            else {
                String headerToken = httpReq.getHeader("X-Auth-Token"); //$NON-NLS-1$
                if ( !StringUtils.isBlank(headerToken) ) {
                    log.debug("Found token header"); //$NON-NLS-1$
                    return decodeToken(headerToken.trim());
                }

                log.debug("No token found in data"); //$NON-NLS-1$
            }
        }

        log.debug("Extract from request parameters"); //$NON-NLS-1$
        String[] paramDecls = StringUtils.split(httpReq.getQueryString(), '&');
        if ( paramDecls == null ) {
            return null;
        }

        for ( String param : paramDecls ) {
            if ( param.startsWith("token=") ) { //$NON-NLS-1$
                try {
                    String token = param.substring(6);
                    if ( log.isDebugEnabled() ) {
                        log.debug("Found token in URL params", token); //$NON-NLS-1$
                    }
                    return decodeToken(token);
                }
                catch (
                    UnsupportedEncodingException |
                    IllegalArgumentException e ) {
                    log.warn("Failed to decode token parameter", e); //$NON-NLS-1$
                    return null;
                }
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Did not find any token in URL params " + httpReq.getQueryString()); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String decodeToken ( String param ) throws UnsupportedEncodingException {
        return URLDecoder.decode(param, "US-ASCII"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * @throws ServletException
     *
     * @see org.apache.shiro.web.filter.AccessControlFilter#onAccessDenied(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected boolean onAccessDenied ( ServletRequest req, ServletResponse resp ) throws IOException, ServletException {
        log.debug("onAccessDenied"); //$NON-NLS-1$
        AuthenticationToken tok = this.createToken(req, resp);
        if ( tok != null ) {
            try {
                SecurityUtils.getSubject().login(tok);
            }
            catch ( AuthenticationException e ) {
                return handleAuthException(e, (HttpServletResponse) resp);
            }
            catch ( UndeclaredThrowableException e ) {
                if ( e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof AuthenticationException ) {
                    return handleAuthException((AuthenticationException) e.getCause().getCause(), (HttpServletResponse) resp);
                }
                throw e;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.servlet.AdviceFilter#doFilterInternal(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws ServletException, IOException {
        RealmTokenToken tok = this.createToken(req, resp);
        if ( this.tokValidator != null && SecurityUtils.getSubject().isAuthenticated() && tok != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Already authenticated but request token found: " + SecurityUtils.getSubject().getPrincipals()); //$NON-NLS-1$
            }
            try {
                PrincipalCollection principals = this.tokValidator.validate(tok);
                if ( principals != null ) {
                    req.setAttribute("crypto.token", principals); //$NON-NLS-1$
                    doRunWithAdditionalPrincipals(req, resp, chain, principals);
                    return;
                }
            }
            catch ( AuthenticationException e ) {
                if ( !handleAuthException(e, (HttpServletResponse) resp) ) {
                    return;
                }
            }
            catch ( UndeclaredThrowableException e ) {
                if ( e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof AuthenticationException ) {
                    if ( !handleAuthException((AuthenticationException) e.getCause().getCause(), (HttpServletResponse) resp) ) {
                        return;
                    }
                }
                throw e;
            }
        }
        else {
            log.debug("Not adding token principals"); //$NON-NLS-1$
        }
        super.doFilterInternal(req, resp, chain);
    }


    /**
     * @param req
     * @param resp
     * @param chain
     * @param principals
     * @throws ServletException
     * @throws IOException
     */
    private void doRunWithAdditionalPrincipals ( ServletRequest req, ServletResponse resp, FilterChain chain, PrincipalCollection principals )
            throws ServletException, IOException {

        PrincipalCollection col = SecurityUtils.getSubject().getPrincipals();
        SimplePrincipalCollection augmented = new SimplePrincipalCollection();

        // filter out other token principals
        for ( String realm : col.getRealmNames() ) {
            for ( Object princ : col.fromRealm(realm) ) {
                if ( ! ( princ instanceof TokenPrincipal ) ) {
                    augmented.add(princ, realm);
                }
            }
        }

        augmented.addAll(principals);
        if ( log.isDebugEnabled() ) {
            log.debug("Running with principals " + augmented); //$NON-NLS-1$
        }
        SecurityUtils.getSubject().runAs(augmented);
        super.doFilterInternal(req, resp, chain);

    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.servlet.AdviceFilter#afterCompletion(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, java.lang.Exception)
     */
    @Override
    public void afterCompletion ( ServletRequest request, ServletResponse response, Exception exception ) throws Exception {
        super.afterCompletion(request, response, exception);
        if ( SecurityUtils.getSubject().isRunAs() ) {
            log.debug("Returning to original credentials"); //$NON-NLS-1$
            SecurityUtils.getSubject().releaseRunAs();
        }
    }


    /**
     * @param e
     * @return
     * @throws IOException
     */
    private static boolean handleAuthException ( AuthenticationException e, HttpServletResponse resp ) throws IOException {
        if ( e instanceof UnsupportedTokenException ) {
            log.debug("URL token authentication is not available", e); //$NON-NLS-1$
            return true;
        }
        else if ( e instanceof ExpiredCredentialsException ) {
            log.debug("The supplied token is expired", e); //$NON-NLS-1$
            resp.sendError(403, "The supplied token is expired"); //$NON-NLS-1$
            return false;
        }
        else if ( e instanceof IncorrectCredentialsException || e instanceof UnknownAccountException ) {
            log.debug("The supplied token is invalid", e); //$NON-NLS-1$
            resp.sendError(403, "The supplied token is invalid"); //$NON-NLS-1$
            return false;
        }

        throw e;
    }
}
