/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2015 by mbechler
 */
package eu.agno3.runtime.security.event;


import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.joda.time.DateTime;

import eu.agno3.runtime.eventlog.AuditEventBuilder;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.eventlog.EventLogger;
import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.eventlog.NullFuture;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.password.PasswordChangePolicyException;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class LoginEventBuilder implements AuditEventBuilder<LoginEventBuilder> {

    private static final String SUCCESS_STATUS = "SUCCESS"; //$NON-NLS-1$
    private static final String PREAUTH_STREAM = "preauth"; //$NON-NLS-1$

    private LoginEvent event;
    private EventLogger eventLogger;
    private boolean ignore;
    private LoginContext context;


    /**
     * @param eventLogger
     * 
     */
    public LoginEventBuilder ( EventLogger eventLogger ) {
        this.eventLogger = eventLogger;
        this.event = new LoginEvent();
        this.event.setTimestamp(DateTime.now());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventBuilder#severity(eu.agno3.runtime.eventlog.EventSeverity)
     */
    @Override
    public LoginEventBuilder severity ( EventSeverity severity ) {
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventBuilder#log()
     */
    @Override
    public Future<Object> log () {
        if ( this.ignore ) {
            return new NullFuture();
        }

        String stream = SUCCESS_STATUS.equals(this.event.getStatus()) ? EventLogger.DEFAULT_STREAM : PREAUTH_STREAM;

        if ( this.context != null ) {
            Map<? extends String, ? extends Serializable> ctxProps = this.context.getProperties();
            // TODO: redact
            this.event.getProperties().putAll(ctxProps);
        }
        return this.eventLogger.log(stream, this.event);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditEventBuilder#fail(eu.agno3.runtime.eventlog.AuditStatus)
     */
    @Override
    public LoginEventBuilder fail ( AuditStatus reason ) {
        this.event.setStatus(reason.name());
        return this;
    }


    /**
     * 
     * @param e
     * @return this
     */
    public LoginEventBuilder fail ( AuthenticationException e ) {
        if ( e instanceof ExpiredCredentialsException ) {
            this.status("EXPIRED_CREDS"); //$NON-NLS-1$
        }
        else if ( e instanceof UnknownAccountException || e instanceof CredentialsException ) {
            this.status("CREDENTIALS"); //$NON-NLS-1$
        }
        else if ( e instanceof DisabledAccountException ) {
            this.status("DISABLED"); //$NON-NLS-1$
        }
        else if ( e instanceof PasswordChangePolicyException ) {
            this.status("PW_CHANGE_POLICY"); //$NON-NLS-1$
        }
        else if ( e instanceof PasswordPolicyException ) {
            this.status("PW_POLICY"); //$NON-NLS-1$
        }
        else if ( e instanceof UnsupportedTokenException ) {
            this.status("UNSUPPORTED"); //$NON-NLS-1$
        }
        else {
            this.status("UNKNOWN"); //$NON-NLS-1$
        }
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditEventBuilder#action(java.lang.String)
     */
    @Override
    public LoginEventBuilder action ( String action ) {
        return this;
    }


    /**
     * 
     * @param status
     * @return this
     */
    public LoginEventBuilder status ( String status ) {
        this.event.setStatus(status);
        return this;
    }


    /**
     * 
     */
    public void ignore () {
        this.ignore = true;
    }


    /**
     * @param loginContext
     * @return this
     */
    public LoginEventBuilder context ( LoginContext loginContext ) {
        // this needs to be delayed until log so we can properly determine whether/how to redact it's entries
        this.context = loginContext;
        return this;
    }


    /**
     * @param r
     * @return this
     */
    public LoginEventBuilder realm ( LoginRealm r ) {
        if ( r != null ) {
            this.event.setAuthType(r.getAuthType().name());
            this.event.setLoginRealm(r.getId());
        }
        return this;
    }


    /**
     * @return this
     * 
     */
    public LoginEventBuilder success () {
        return this.status(SUCCESS_STATUS); // $NON-NLS-1$
    }


    /**
     * @param princ
     * @return this
     */
    public LoginEventBuilder principal ( UserPrincipal princ ) {
        this.event.setUserPrincipal(princ);
        return this;
    }
}
