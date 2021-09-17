/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.terms.internal;


import java.util.Collection;
import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmType;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.login.TermsLoginChallenge;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.security.terms.TermsService;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, LoginRealm.class
}, configurationPid = TermsRealm.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TermsRealm extends AuthenticatingRealm implements LoginRealm {

    private static final Logger log = Logger.getLogger(TermsRealm.class);

    /**
     * 
     */
    public static final String PID = "auth.terms"; //$NON-NLS-1$

    private TermsService termsService;

    private Collection<String> before;
    private Collection<String> after;


    /**
     * 
     */
    public TermsRealm () {
        setName("TERMS"); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        setName((String) ctx.getProperties().get("instanceId")); //$NON-NLS-1$
        parseConfig(ctx.getProperties());
        this.init();
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isPrimary()
     */
    @Override
    public boolean isPrimary () {
        return false;
    }


    /**
     * @param properties
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.before = ConfigUtil.parseStringSet(cfg, "before", null); //$NON-NLS-1$
        this.after = ConfigUtil.parseStringSet(cfg, "after", null); //$NON-NLS-1$
    }


    @Reference
    protected synchronized void setTermsService ( TermsService ts ) {
        this.termsService = ts;
    }


    protected synchronized void unsetTermsService ( TermsService ts ) {
        if ( this.termsService == ts ) {
            this.termsService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getId()
     */
    @Override
    public String getId () {
        return getName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getAuthType()
     */
    @Override
    public LoginRealmType getAuthType () {
        return LoginRealmType.CUSTOM;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#supportPasswordChange()
     */
    @Override
    public boolean supportPasswordChange () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getType()
     */
    @Override
    public String getType () {
        return "terms"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getBefore()
     */
    @Override
    public Collection<String> getBefore () {
        return this.before;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getAfter()
     */
    @Override
    public Collection<String> getAfter () {
        return this.after;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isApplicable(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public boolean isApplicable ( LoginContext ctx ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#authenticate(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse authenticate ( LoginContext ctx, LoginSession sess ) {
        return new AuthResponse(AuthResponseType.COMPLETE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#changePassword(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.principal.UserPrincipal, eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse changePassword ( LoginContext ctx, UserPrincipal up, LoginSession sess ) {
        return new AuthResponse(AuthResponseType.COMPLETE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#postauth(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse postauth ( LoginContext ctx, LoginSession sess ) {
        boolean allAccepted = true;

        AuthenticationInfo ai = sess.getMergedAuthInfo();
        UserPrincipal up = ai != null ? ai.getPrincipals().oneByType(UserPrincipal.class) : null;
        if ( up == null ) {
            // wait until we have a user authenticated (should have as we are in postauth)
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        for ( TermsDefinition term : buildRequiredTerms(up) ) {
            String tid = term.getId();
            TermsLoginChallenge challenge = sess.getChallenge(TermsLoginChallenge.class, tid);
            if ( challenge == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Prompting for term " + tid); //$NON-NLS-1$
                }
                sess.addChallenge(new TermsLoginChallenge(tid));
                allAccepted = false;
            }
            else if ( challenge.isComplete() ) {
                continue;
            }
            else if ( !challenge.isPrompted() || challenge.getResponse() == null || !challenge.getResponse() ) {
                allAccepted = false;
                challenge.reset();
            }
            else {
                this.termsService.markAccepted(up, tid);
                challenge.markComplete();
            }
        }

        if ( !allAccepted ) {
            return new AuthResponse(AuthResponseType.BREAK);
        }
        return new AuthResponse(AuthResponseType.COMPLETE);
    }


    /**
     * @param up
     * @return
     */
    private Collection<TermsDefinition> buildRequiredTerms ( UserPrincipal up ) {
        return this.termsService.getRequiredTerms(up);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#preauth(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse preauth ( LoginContext ctx, LoginSession sess ) {
        return new AuthResponse(AuthResponseType.COMPLETE);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken arg0 ) throws AuthenticationException {
        return null;
    }

}
