/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.audit;


import java.io.Serializable;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.MailRateLimitingException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.model.audit.BaseFileshareEvent;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.runtime.eventlog.AuditEventBuilder;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.eventlog.EventLogger;
import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.eventlog.NullFuture;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 * @param <TEv>
 * @param <T>
 *
 */
public abstract class AbstractFileshareAuditBuilder <TEv extends BaseFileshareEvent, T extends AbstractFileshareAuditBuilder<TEv, T>>
        implements AuditEventBuilder<T> {

    /**
     * 
     */
    private static final String ANON_AUTH_TYPE = "ANON"; //$NON-NLS-1$
    private static final String PREAUTH_STREAM = "preauth"; //$NON-NLS-1$

    protected EventLogger logger;
    protected TEv event;
    private boolean ignored;
    private boolean policyAccepted;


    /**
     * @param logger
     * 
     */
    public AbstractFileshareAuditBuilder ( EventLogger logger ) {
        this.logger = logger;
        this.event = makeEvent();
        this.event.setSeverity(EventSeverity.AUDIT);
        this.event.setTimestamp(DateTime.now());
    }


    /**
     * @return
     */
    protected abstract TEv makeEvent ();


    /**
     * 
     * @param key
     * @param val
     * @return this
     */
    public T property ( String key, Serializable val ) {
        this.event.getProperties().put(key, val);
        return self();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventBuilder#severity(eu.agno3.runtime.eventlog.EventSeverity)
     */
    @Override
    public T severity ( EventSeverity severity ) {
        this.event.setSeverity(severity);
        return self();
    }


    /**
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    protected T self () {
        return (T) this;
    }


    /**
     * @return this
     * 
     */
    public T ignore () {
        this.ignored = true;
        return self();
    }


    /**
     * 
     * @param acs
     * @return this
     */
    public T access ( AccessControlService acs ) {

        if ( acs.isUserAuthenticated() ) {
            this.event.setAuthType("USER"); //$NON-NLS-1$
        }
        else if ( acs.isTokenAuth() ) {
            this.event.setAuthType("TOKEN"); //$NON-NLS-1$
        }
        else {
            this.event.setAuthType(ANON_AUTH_TYPE);
        }

        HttpServletRequest httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        if ( httpRequest != null ) {
            this.event.setRemoteAddr(httpRequest.getRemoteAddr());
            Object proxied = httpRequest.getAttribute("proxied-via"); //$NON-NLS-1$
            if ( proxied instanceof String && !StringUtils.isBlank((String) proxied) ) {
                this.event.setProxiedVia((String) proxied);
            }
        }

        try {
            if ( acs.isUserAuthenticated() ) {
                UserPrincipal up = acs.getCurrentUserPrincipal();
                if ( up == null ) {
                    return self();
                }

                this.event.setPrincipal(up);
            }
        }
        catch ( AuthenticationException e ) {
            this.severity(EventSeverity.ERROR);
            this.fail(AuditStatus.UNAUTHENTICATED);
            throw new org.apache.shiro.authc.AuthenticationException(e);
        }

        return self();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventBuilder#log()
     */
    @Override
    public Future<Object> log () {
        if ( this.ignored ) {
            return new NullFuture();
        }

        if ( !this.policyAccepted && ( this.event.getAuthType() == null || ANON_AUTH_TYPE.equals(this.event.getAuthType()) ) ) {
            return this.logger.log(PREAUTH_STREAM, this.event);
        }
        return this.logger.log(this.event);
    }


    /**
     * @param e
     */
    public void fail ( FileshareException e ) {
        if ( e instanceof AuthenticationException ) {
            fail(AuditStatus.UNAUTHENTICATED);
        }
        else if ( e instanceof AccessDeniedException ) {
            fail(AuditStatus.UNAUTHORIZED);
        }
        else if ( e instanceof PolicyNotFulfilledException ) {
            fail(AuditStatus.UNAUTHORIZED);
        }
        else if ( e instanceof MailRateLimitingException ) {
            this.event.setStatus("RATE_LIMIT"); //$NON-NLS-1$
            this.event.setSeverity(EventSeverity.WARNING);
        }
        else {
            fail(AuditStatus.VALIDATION);
        }
    }


    /**
     * @return self
     */
    public T policyAccepted () {
        this.policyAccepted = true;
        return this.self();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditEventBuilder#fail(eu.agno3.runtime.eventlog.AuditStatus)
     */
    @Override
    public T fail ( AuditStatus reason ) {
        this.event.setStatus(reason.name());
        this.event.setSeverity(EventSeverity.WARNING);
        return self();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditEventBuilder#action(java.lang.String)
     */
    @Override
    public T action ( String action ) {
        this.event.setAction(action);
        return self();
    }

}