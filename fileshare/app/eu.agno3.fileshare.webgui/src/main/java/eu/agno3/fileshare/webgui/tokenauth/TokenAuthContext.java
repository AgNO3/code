/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.tokenauth;


import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.security.ratelimit.LoginRateLimiter;


/**
 * @author mbechler
 *
 */
@Named ( "tokenAuthContext" )
@ViewScoped
public class TokenAuthContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5775002280247536260L;

    private static final Logger log = Logger.getLogger(TokenAuthContext.class);

    private UUID grantId;

    private String password;

    private Integer throttleDelay;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @return the grantId
     */
    public UUID getGrantId () {
        return this.grantId;
    }


    /**
     * @param grantId
     *            the grantId to set
     */
    public void setGrantId ( UUID grantId ) {
        this.grantId = grantId;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.password = password;
    }


    /**
     * @param seconds
     * 
     */
    public void setThrottleDelay ( Integer seconds ) {
        this.throttleDelay = seconds;
    }


    /**
     * @return the throttleDelay
     */
    public Integer getThrottleDelay () {
        return this.throttleDelay;
    }


    /**
     * 
     * @return null
     */
    public String doLogin () {
        if ( this.throttleDelay != null || this.grantId == null ) {
            return null;
        }

        log.debug("Performing token login"); //$NON-NLS-1$

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String sourceAddress = getSourceAddress(externalContext);
        Session s = SecurityUtils.getSubject().getSession(true);

        TokenGrant tg = new TokenGrant();
        tg.setId(this.grantId);

        LoginRateLimiter ratelimiter = this.fsp.getLoginRateLimiter();

        int remainThrottle = ratelimiter.getNextLoginDelay(tg, sourceAddress);
        if ( remainThrottle > 0 ) {
            this.setThrottleDelay(remainThrottle);
            return null;
        }

        try {
            this.fsp.getShareService().authToken(this.grantId, this.getPassword());
            ratelimiter.recordSuccessAttempt(tg, sourceAddress);
            s.setAttribute("grantpw_" + this.grantId, this.getPassword()); //$NON-NLS-1$
            SavedRequest savedRequest = WebUtils.getSavedRequest((ServletRequest) externalContext.getRequest());
            externalContext.redirect(savedRequest.getRequestUrl());
        }
        catch ( AuthenticationException e ) {
            int newThrottleDelay = ratelimiter.recordFailAttempt(tg, sourceAddress);
            if ( newThrottleDelay > 0 ) {
                this.setThrottleDelay(newThrottleDelay);
            }
            else {
                this.setThrottleDelay(null);
            }
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_FATAL, FileshareMessages.get(FileshareMessages.WRONG_GRANT_PASSWORD), StringUtils.EMPTY));
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
        }
        catch ( IOException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param externalContext
     * @return
     */
    private static String getSourceAddress ( ExternalContext ec ) {
        HttpServletRequest req = (HttpServletRequest) ec.getRequest();
        return req.getRemoteAddr();
    }


    /**
     * 
     * 
     */
    public void throttleComplete () {
        log.debug("Unsetting throttle"); //$NON-NLS-1$
        this.setThrottleDelay(null);
    }
}
