/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.gui.init;


import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmManager;
import eu.agno3.runtime.security.web.ContextUtil;
import eu.agno3.runtime.security.web.gui.LoginSessionBean;
import eu.agno3.runtime.security.web.login.CustomWebAuthAuthenticationException;
import eu.agno3.runtime.security.web.login.CustomWebAuthRealm;
import eu.agno3.runtime.security.web.login.WebLoginConfig;


/**
 * @author mbechler
 *
 */
public class DelegatingAuthFilter extends AuthenticatingFilter {

    /**
     * 
     */
    private static final String SUCCESS_CODE = "success"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String REALM_PREFIX = "realm/"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DelegatingAuthFilter.class);

    @Inject
    private WebLoginConfig config;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private LoginRealmManager realmManager;

    @Inject
    private LoginSessionBean loginSession;


    /**
     * 
     */
    public DelegatingAuthFilter () {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.authc.AuthenticatingFilter#createToken(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected AuthenticationToken createToken ( ServletRequest req, ServletResponse resp ) throws Exception {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     *
     * @see org.apache.shiro.web.filter.AccessControlFilter#onAccessDenied(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse)
     */
    @Override
    protected boolean onAccessDenied ( ServletRequest req, ServletResponse resp ) throws Exception {
        log.debug("onAccessDenied"); //$NON-NLS-1$

        try ( AuditContext<LoginEventBuilder> audit = this.realmManager.getAuditContext() ) {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            String parts[] = splitRequestPath(httpReq, WebUtils.getPathWithinApplication(httpReq));
            CustomWebAuthRealm realm = getRealmFromRequest(parts);
            String returnId = getReturnIdFromRequest(parts);
            LoginContext ctx = ContextUtil.makeLoginContext(this.config, httpReq, true);
            audit.builder().context(ctx);
            audit.builder().realm(realm);
            if ( realm == null ) {
                return false;
            }

            try {
                AuthResponse ar = realm.doExternalAuthentication(httpReq, httpResp, audit);

                if ( ar != null && ar.getType() == AuthResponseType.COMPLETE ) {
                    if ( ar.getAuthInfo() != null ) {
                        this.loginSession.storeAuthInfo(realm, ar.getAuthInfo());
                    }
                    audit.builder().success();
                    return redirectStatus(httpReq, httpResp, realm, SUCCESS_CODE, returnId);
                }
                else if ( ar == null || ar.getType() != AuthResponseType.CONTINUE ) {
                    throw new AuthenticationException("Invalid response from realm " + ( ar != null ? ar.getType() : null )); //$NON-NLS-1$
                }
            }
            catch ( CustomWebAuthAuthenticationException e ) {
                log.debug("Authentication failed", e); //$NON-NLS-1$
                audit.builder().status(e.getCode());
                return redirectStatus(httpReq, httpResp, realm, e.getCode(), returnId);
            }
            catch ( Exception e ) {
                log.warn("Exception in authenticator", e); //$NON-NLS-1$
                audit.builder().fail(AuditStatus.INTERNAL);
                return redirectStatus(httpReq, httpResp, realm, "general", returnId); //$NON-NLS-1$
            }

            audit.builder().ignore();
        }

        return false;
    }


    /**
     * @param httpReq
     * @param httpResp
     * @param lrm
     * @param code
     * @return
     * @throws IOException
     */
    private boolean redirectStatus ( HttpServletRequest httpReq, HttpServletResponse httpResp, CustomWebAuthRealm lrm, String code, String returnId )
            throws IOException {
        String target = String.format(
            "%s%sindex.xhtml?realm=%s&authReturn=%s&authReturnId=%s", //$NON-NLS-1$
            httpReq.getContextPath(),
            this.config.getAuthBasePath(),
            lrm.getId(),
            code,
            returnId);

        httpResp.sendRedirect(httpResp.encodeRedirectURL(target));
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.filter.AccessControlFilter#isAccessAllowed(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, java.lang.Object)
     */
    @Override
    protected boolean isAccessAllowed ( ServletRequest req, ServletResponse resp, Object obj ) {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        String reqUri = WebUtils.getPathWithinApplication(httpReq);
        if ( !reqUri.startsWith(this.config.getAuthBasePath()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Outside auth base " + reqUri); //$NON-NLS-1$
            }
            return false;
        }

        String parts[] = splitRequestPath(httpReq, reqUri);

        LoginRealm lrm = getRealmFromRequest(parts);
        if ( lrm == null ) {
            return true;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Disallowing access for realm " + lrm.getId()); //$NON-NLS-1$
        }
        return false;
    }


    private String[] splitRequestPath ( HttpServletRequest req, String path ) {
        String reqPath = path;
        if ( !reqPath.startsWith(this.config.getAuthBasePath()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not within authentication base " + path); //$NON-NLS-1$
            }
            return null;
        }

        reqPath = reqPath.substring(this.config.getAuthBasePath().length());
        if ( !reqPath.startsWith(REALM_PREFIX) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not realm prefix " + path); //$NON-NLS-1$
            }
            return null;
        }

        reqPath = reqPath.substring(REALM_PREFIX.length());
        int nextSlash = reqPath.indexOf('/');

        String realm;
        if ( nextSlash < 0 ) {
            return new String[] {
                reqPath, null, null
            };
        }

        realm = reqPath.substring(0, nextSlash);
        int secondSlash = reqPath.indexOf('/', nextSlash + 1);
        if ( secondSlash < 0 ) {
            return new String[] {
                realm, reqPath.substring(nextSlash + 1), null
            };
        }

        return new String[] {
            realm, reqPath.substring(nextSlash + 1, secondSlash), reqPath.substring(secondSlash + 1)
        };
    }


    /**
     * @param httpReq
     * @return
     */
    private static String getReturnIdFromRequest ( String[] parts ) {
        if ( parts == null || parts[ 1 ] == null ) {
            return null;
        }

        return parts[ 1 ];
    }


    /**
     * @param reqUri
     * @return
     */
    private CustomWebAuthRealm getRealmFromRequest ( String[] parts ) {

        if ( parts == null || parts[ 0 ] == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Realm not specified"); //$NON-NLS-1$
            }
            return null;
        }

        LoginRealm lrm = this.realmManager.getRealm(parts[ 0 ]);
        if ( ! ( lrm instanceof CustomWebAuthRealm ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Realm not found: " + parts[ 0 ]); //$NON-NLS-1$
            }
            return null;
        }
        return (CustomWebAuthRealm) lrm;
    }

}
