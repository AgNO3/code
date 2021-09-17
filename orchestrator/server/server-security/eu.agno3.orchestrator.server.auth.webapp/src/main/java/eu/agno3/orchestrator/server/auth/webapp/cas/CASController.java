/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp.cas;


import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationContext;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.RootCasException;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.Response.ResponseType;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.logout.LogoutRequestStatus;
import org.jasig.cas.logout.SingleLogoutService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.validation.Assertion;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.server.auth.cas.ExtendedCASServer;
import eu.agno3.orchestrator.server.auth.cas.ShiroCredential;
import eu.agno3.orchestrator.server.auth.cas.WebApplicationService;
import eu.agno3.runtime.security.web.gui.LoginMessages;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "casController" )
public class CASController {

    private static final String END_CAS_AUTH_FAIL = "</cas:authenticationFailure>"; //$NON-NLS-1$
    private static final String END_CAS_SERVICE_RESP = "</cas:serviceResponse>"; //$NON-NLS-1$
    private static final String START_CAS_AUTH_SUCESS = "<cas:authenticationSuccess>"; //$NON-NLS-1$
    private static final String START_CAS_SERVICE_RESP = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(CASController.class);
    /**
     * 
     */
    private static final String WARN_PARAM = "warn"; //$NON-NLS-1$
    private static final String SERVICE_PARAM = "service"; //$NON-NLS-1$
    private static final String RENEW_PARAM = "renew"; //$NON-NLS-1$

    private static final String INTERNAL_ERROR = "INTERNAL_ERROR"; //$NON-NLS-1$
    private static final String INVALID_REQUEST_ERROR = "INVALID_REQUEST"; //$NON-NLS-1$
    private static final String INVALID_PROXY_CALLBACK_ERROR = "INVALID_PROXY_CALLBACK"; //$NON-NLS-1$

    private static final String CAS_SERVICE_ISSUE_FAILED = "cas.serviceIssueFailed"; //$NON-NLS-1$
    private static final String CAS_REDIRECTION_FAILED = "cas.redirectionFailed"; //$NON-NLS-1$
    private static final String CAS_UNSUP_LOGIN_RESP_TYPE = "cas.unsupportedLoginReponse"; //$NON-NLS-1$
    private static final String CAS_UNSUP_LOGIN_RENEW = "cas.unsupportedRenew"; //$NON-NLS-1$
    private static final String CAS_TGT_ISSUE_FAILED = "cas.tgtIssueFailed"; //$NON-NLS-1$
    private static final String CAS_AUTH_VERF_REQ = "cas.authVerifyRequired"; //$NON-NLS-1$
    private static final String CAS_AUTH_VERF_REQ_DETAIL_FMT = "cas.authVerifyRequiredDetailFmt"; //$NON-NLS-1$

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private ExtendedCASServer cas;

    @Inject
    private CASSession session;


    /**
     * Run the CAS /login workflow
     * 
     * At this point the user has been already authenticated through shiro
     */
    public void loginEndpoint () {

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        if ( log.isDebugEnabled() ) {
            log.debug("Login request"); //$NON-NLS-1$
        }

        String tgt = getTgt(fc);

        SingleLogoutService s = getRequestedService();

        if ( !checkLoginRequest(fc, ec, tgt, s) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Requested ticket for service: " + s); //$NON-NLS-1$
        }

        ServiceTicket ticket = doCreateServiceTicket(fc, tgt, s);

        if ( ticket != null ) {
            doLoginResponse(fc, s, ticket);
        }
    }


    /**
     * @param fc
     * @param tgt
     * @param s
     * @return
     */
    protected ServiceTicket doCreateServiceTicket ( FacesContext fc, String tgt, SingleLogoutService s ) {
        ServiceTicket ticket;
        try {
            ticket = this.cas.grantServiceTicket(tgt, s, doAuthenticate(new Credential[] {
                new ShiroCredential()
            }));
        }
        catch (
            RootCasException |
            UndeclaredThrowableException |
            AuthenticationException e ) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, LoginMessages.get(CAS_SERVICE_ISSUE_FAILED), e.getMessage()));
            log.warn("Failed to issue service ticket", unwrapException(e)); //$NON-NLS-1$
            try {
                if ( this.session.getTgt() != null ) {
                    this.cas.destroyTicketGrantingTicket(this.session.getTgt());
                }
            }
            catch ( UndeclaredThrowableException e2 ) {
                log.warn("Failed to destroy TGT", e2); //$NON-NLS-1$
            }
            this.session.setTgt(null);

            try {
                SecurityUtils.getSubject().logout();
                SecurityUtils.getSubject().getSession().stop();
                FacesContext.getCurrentInstance().getExternalContext().redirect(s.getOriginalUrl());
            }
            catch ( IOException e2 ) {
                log.warn("Failed to redirect back after failure", e2); //$NON-NLS-1$
            }
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Issuing ticket: " + ticket); //$NON-NLS-1$
        }
        return ticket;
    }


    /**
     * @param e
     * @return
     */
    private static Throwable unwrapException ( Exception e ) {
        if ( e instanceof UndeclaredThrowableException && e.getCause() instanceof InvocationTargetException && e.getCause().getCause() != null ) {
            return e.getCause().getCause();
        }
        return e;
    }


    /**
     * @param fc
     * @param s
     * @param ticket
     */
    protected void doLoginResponse ( FacesContext fc, SingleLogoutService s, ServiceTicket ticket ) {
        Response response = s.getResponse(ticket.getId());

        if ( response.getResponseType() == ResponseType.REDIRECT ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Redirecting to " + response.getUrl()); //$NON-NLS-1$
                }
                FacesContext.getCurrentInstance().getExternalContext().redirect(response.getUrl());
            }
            catch ( IOException e ) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, LoginMessages.get(CAS_REDIRECTION_FAILED), e.getMessage()));
                log.warn("Failed to issue redirect to service URI", e); //$NON-NLS-1$
            }
        }
        else {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, LoginMessages.get(CAS_UNSUP_LOGIN_RESP_TYPE), StringUtils.EMPTY));
            log.warn("Unsupported response type " + response.getResponseType()); //$NON-NLS-1$
        }
    }


    /**
     * @param fc
     * @param ec
     * @param tgt
     * @param s
     */
    protected boolean checkLoginRequest ( FacesContext fc, ExternalContext ec, String tgt, SingleLogoutService s ) {
        if ( tgt == null ) {
            return false;
        }

        if ( s == null ) {
            log.debug("No service specified"); //$NON-NLS-1$
            return false;
        }

        if ( ec.getRequestParameterMap().get(RENEW_PARAM) != null ) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, LoginMessages.get(CAS_UNSUP_LOGIN_RENEW), StringUtils.EMPTY));
            return false;
        }

        if ( ec.getRequestParameterMap().get(WARN_PARAM) != null ) {
            fc.addMessage(
                null,
                new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    LoginMessages.get(CAS_AUTH_VERF_REQ),
                    LoginMessages.format(CAS_AUTH_VERF_REQ_DETAIL_FMT, s.getId())));
            return false;
        }

        return true;
    }


    /**
     * @return
     */
    protected SingleLogoutService getRequestedService () {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        return WebApplicationService.createServiceFrom((HttpServletRequest) externalContext.getRequest());
    }


    /**
     * @param fc
     * @return
     */
    protected String getTgt ( FacesContext fc ) {
        if ( !this.session.hasTgt() ) {
            try {
                this.session.setTgt(this.cas.createTicketGrantingTicket(doAuthenticate(new Credential[] {
                    new ShiroCredential()
                })).getId());
            }
            catch (
                AuthenticationException |
                RootCasException e ) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, LoginMessages.get(CAS_TGT_ISSUE_FAILED), e.getMessage()));
                log.warn("Failed to issue ticket granting ticket", e); //$NON-NLS-1$
                return null;
            }
        }

        return this.session.getTgt();
    }


    /**
     * /proxyValidate endpoint
     */
    public void proxyValidateEndpoint () {
        log.debug("ProxyValidate request"); //$NON-NLS-1$
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        SingleLogoutService s = getService(ec);
        String ticket = getTicket(ec);

        if ( !checkProxyValidateParams(fc, ec, s, ticket) ) {
            return;
        }

        String pgtUrl = ec.getRequestParameterMap().get("pgtUrl"); //$NON-NLS-1$

        String proxyIou = null;
        if ( pgtUrl != null ) {
            proxyIou = handleProxyTicketRequest(fc, ec, s, pgtUrl);
            if ( proxyIou == null ) {
                return;
            }
        }

        sendProxyValidateResponse(fc, ec, s, ticket, proxyIou);
    }


    /**
     * @param fc
     * @param ec
     * @param s
     * @param ticket
     * @return
     */
    private boolean checkProxyValidateParams ( FacesContext fc, ExternalContext ec, SingleLogoutService s, String ticket ) {
        if ( s == null || ticket == null ) {
            writeErrorResponse(fc, ec, "Illegal request", INTERNAL_ERROR); //$NON-NLS-1$
            return false;
        }

        if ( ec.getRequestParameterMap().get(RENEW_PARAM) != null ) {
            writeErrorResponse(fc, ec, "renew was requested but is not supported", INTERNAL_ERROR); //$NON-NLS-1$
            return false;
        }
        return true;
    }


    /**
     * @param fc
     * @param ec
     * @param s
     * @param ticket
     * @param proxyIou
     */
    private void sendProxyValidateResponse ( FacesContext fc, ExternalContext ec, SingleLogoutService s, String ticket, String proxyIou ) {
        try {
            Assertion as = this.cas.validateServiceTicket(ticket, s);

            if ( log.isDebugEnabled() ) {
                log.debug("Successfully validated ticket " + as); //$NON-NLS-1$
            }

            writeSuccessResponse(fc, ec, as, proxyIou);
        }
        catch (
            RootCasException |
            UndeclaredThrowableException e ) {
            log.warn("Failed to validate service ticket", e); //$NON-NLS-1$

            if ( e.getCause() instanceof RootCasException ) {
                this.writeProxyErrorResponse(fc, ec, e.getCause().getMessage(), ( (RootCasException) e.getCause() ).getCode());
            }
            else {
                this.writeProxyErrorResponse(fc, ec, e.getCause().getMessage(), INTERNAL_ERROR);
            }
        }
    }


    /**
     * @param fc
     * @param ec
     * @param s
     * @param pgtUrl
     * @param proxyIou
     * @return
     */
    private String handleProxyTicketRequest ( FacesContext fc, ExternalContext ec, SingleLogoutService s, String pgtUrl ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Proxy ticket requested with callback " + pgtUrl); //$NON-NLS-1$
        }
        Credential sCred;
        ProxyGrantingTicket pgt = null;
        try {
            sCred = new HttpBasedServiceCredential(new URL(pgtUrl), null);
        }
        catch ( MalformedURLException e ) {
            log.warn("Illegal proxy callback url", e); //$NON-NLS-1$
            writeErrorResponse(fc, ec, "illegal proxy callback url", INVALID_PROXY_CALLBACK_ERROR); //$NON-NLS-1$
            return null;
        }

        try {
            pgt = this.cas.createProxyGrantingTicket(s.getArtifactId(), doAuthenticate(new Credential[] {
                sCred
            }));
        }
        catch ( RootCasException e ) {
            log.warn("Failed to issue proxy ticket service ticket", e); //$NON-NLS-1$
            writeErrorResponse(fc, ec, e.getMessage(), e.getCode());
        }
        catch ( AuthenticationException e ) {
            log.warn("Invalid service credentials", e); //$NON-NLS-1$
            writeErrorResponse(fc, ec, "invalid service credentials", INVALID_PROXY_CALLBACK_ERROR); //$NON-NLS-1$
        }

        String proxyIou = null;
        if ( pgt != null ) {
            proxyIou = this.doProxyCallback(sCred, pgt);

            if ( StringUtils.isBlank(proxyIou) ) {
                writeErrorResponse(fc, ec, "invalid proxy callback", INVALID_PROXY_CALLBACK_ERROR); //$NON-NLS-1$
                return null;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Returning proxyIou " + proxyIou); //$NON-NLS-1$
            }
        }
        return proxyIou;
    }


    private AuthenticationContext doAuthenticate ( Credential[] credentials ) throws AuthenticationException {
        return new DefaultAuthenticationContext(this.cas.getAuthenticationManager().authenticate(AuthenticationTransaction.wrap(credentials)));
    }


    /**
     * /proxy endpoint
     */
    public void proxyEndpoint () {
        log.debug("proxy Request"); //$NON-NLS-1$

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        Service targetService = getRequestedService();
        if ( targetService == null ) {
            return;
        }

        String pgt = ec.getRequestParameterMap().get("pgt"); //$NON-NLS-1$
        if ( StringUtils.isBlank(pgt) ) {
            writeProxyErrorResponse(fc, ec, "Missing pgt or targetService parameter", INVALID_REQUEST_ERROR); //$NON-NLS-1$
            return;
        }

        ServiceTicket ticket;
        try {
            ticket = this.cas.grantProxyTicket(pgt, targetService);
        }
        catch (
            RootCasException |
            UndeclaredThrowableException e ) {
            log.warn("Failed to issue proxy ticket", e); //$NON-NLS-1$

            if ( e.getCause() instanceof RootCasException ) {
                this.writeProxyErrorResponse(fc, ec, e.getCause().getMessage(), ( (RootCasException) e.getCause() ).getCode());
            }
            else if ( e.getCause() != null ) {
                this.writeProxyErrorResponse(fc, ec, e.getCause().getMessage(), INTERNAL_ERROR);
            }
            else {
                this.writeProxyErrorResponse(fc, ec, e.getMessage(), INTERNAL_ERROR);
            }
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Ticket " + ticket); //$NON-NLS-1$
        }

        this.writeProxySuccessResponse(fc, ec, ticket);
    }


    /**
     * @param sCred
     * @param pgtUrl
     * @param pgt
     * @return
     */
    private String doProxyCallback ( Credential sCred, TicketGrantingTicket pgt ) {
        return this.cas.getProxyHandler().handle(sCred, pgt);
    }


    /**
     * @param ec
     * @return
     */
    protected String getTicket ( ExternalContext ec ) {
        FacesContext fc = FacesContext.getCurrentInstance();
        String ticket = ec.getRequestParameterMap().get("ticket"); //$NON-NLS-1$

        if ( ticket == null ) {
            writeErrorResponse(fc, ec, "ticket parameter is required", INVALID_REQUEST_ERROR); //$NON-NLS-1$
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Found ticket " + ticket); //$NON-NLS-1$
        }
        return ticket;
    }


    /**
     * @param ec
     * @return
     */
    protected SingleLogoutService getService ( ExternalContext ec ) {
        FacesContext fc = FacesContext.getCurrentInstance();
        SingleLogoutService s = WebApplicationService.createServiceFrom((HttpServletRequest) ec.getRequest());

        if ( s == null ) {
            writeErrorResponse(fc, ec, "service parameter is required", INVALID_REQUEST_ERROR); //$NON-NLS-1$
            return null;
        }
        return s;
    }


    /**
     * @param fc
     * @param ec
     * @param as
     * @param proxyIou
     * @param e
     */
    protected void writeSuccessResponse ( FacesContext fc, ExternalContext ec, Assertion as, String proxyIou ) {

        SimplePrincipal p = (SimplePrincipal) as.getPrimaryAuthentication().getPrincipal();

        try ( Writer wr = ec.getResponseOutputWriter() ) {
            wr.append(START_CAS_SERVICE_RESP);
            wr.append(START_CAS_AUTH_SUCESS);
            wr.append("<cas:user>"); //$NON-NLS-1$
            wr.append(p.getId());
            wr.append("</cas:user>"); //$NON-NLS-1$
            wr.append("<cas:attributes>"); //$NON-NLS-1$
            for ( Entry<String, Object> e : p.getAttributes().entrySet() ) {
                wr.append(String.format("<cas:%s>%s</cas:%s>", e.getKey(), e.getValue(), e.getKey())); //$NON-NLS-1$
            }
            wr.append("</cas:attributes>"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(proxyIou) ) {
                wr.append("<cas:proxyGrantingTicket>"); //$NON-NLS-1$
                wr.append(proxyIou);
                wr.append("</cas:proxyGrantingTicket>"); //$NON-NLS-1$
            }
            wr.append("</cas:authenticationSuccess>"); //$NON-NLS-1$
            wr.append(END_CAS_SERVICE_RESP);
            fc.responseComplete();
        }
        catch ( IOException e2 ) {
            log.warn("Failed to send error response", e2); //$NON-NLS-1$
        }
    }


    /**
     * @param fc
     * @param ec
     * @param e
     */
    protected void writeErrorResponse ( FacesContext fc, ExternalContext ec, String msg, String code ) {
        try ( Writer wr = ec.getResponseOutputWriter() ) {
            wr.append(START_CAS_SERVICE_RESP);
            wr.append("<cas:authenticationFailure code=\"" //$NON-NLS-1$
                    + code + "\">"); //$NON-NLS-1$
            wr.append(msg);
            wr.append(END_CAS_AUTH_FAIL);
            wr.append(END_CAS_SERVICE_RESP);
            fc.responseComplete();
        }
        catch ( IOException e2 ) {
            log.warn("Failed to send error response", e2); //$NON-NLS-1$
        }
    }


    protected void writeProxySuccessResponse ( FacesContext fc, ExternalContext ec, ServiceTicket proxyTicket ) {

        try ( Writer wr = ec.getResponseOutputWriter() ) {
            wr.append(START_CAS_SERVICE_RESP);
            wr.append("<cas:proxySuccess>"); //$NON-NLS-1$
            wr.append("<cas:proxyTicket>"); //$NON-NLS-1$
            wr.append(proxyTicket.getId());
            wr.append("</cas:proxyTicket>"); //$NON-NLS-1$
            wr.append("</cas:proxySuccess>"); //$NON-NLS-1$
            wr.append(END_CAS_SERVICE_RESP);
            fc.responseComplete();
        }
        catch ( IOException e2 ) {
            log.warn("Failed to send error response", e2); //$NON-NLS-1$
        }
    }


    protected void writeProxyErrorResponse ( FacesContext fc, ExternalContext ec, String msg, String code ) {
        try ( Writer wr = ec.getResponseOutputWriter() ) {
            wr.append(START_CAS_SERVICE_RESP);
            wr.append("<cas:proxyFailure code=\"" //$NON-NLS-1$
                    + code + "\">"); //$NON-NLS-1$
            wr.append(msg);
            wr.append("</cas:proxyFailure>"); //$NON-NLS-1$
            wr.append(END_CAS_SERVICE_RESP);
            fc.responseComplete();
        }
        catch ( IOException e ) {
            log.warn("Failed to send error response", e); //$NON-NLS-1$
        }
    }


    /**
     * /logout endpoint
     * 
     */
    public void logoutEndpoint () {
        log.debug("Logout request"); //$NON-NLS-1$
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        try {
            List<LogoutRequest> req = Collections.EMPTY_LIST;
            if ( this.session.hasTgt() ) {
                req = this.cas.destroyTicketGrantingTicket(this.session.getTgt());
                this.session.setTgt(null);
            }

            for ( LogoutRequest r : req ) {
                if ( r.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED && log.isDebugEnabled() ) {
                    log.debug("Pending logout request" + r); //$NON-NLS-1$
                }
            }

            String service = ec.getRequestParameterMap().get(SERVICE_PARAM);
            RegisteredService matchService = this.cas.getServicesManager().findServiceBy(getRequestedService());
            if ( matchService != null ) {
                try {
                    ec.redirect(service);
                }
                catch ( IOException e ) {
                    log.warn("Failed to redirect to logout page", e); //$NON-NLS-1$
                }
            }
        }
        finally {
            SecurityUtils.getSubject().logout();
        }
    }
}
