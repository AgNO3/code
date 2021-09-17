/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2015 by mbechler
 */
package eu.agno3.fileshare.security.internal;


import java.util.Collection;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmType;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.token.RealmTokenToken;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, LoginRealm.class
} )
public class FileshareTokenRealm extends AuthenticatingRealm implements LoginRealm {

    private TokenGenerator tokValidator;


    @Reference
    protected synchronized void setTokenGenerator ( TokenGenerator tv ) {
        this.tokValidator = tv;
    }


    protected synchronized void unsetTokenGenerator ( TokenGenerator tv ) {
        if ( this.tokValidator == tv ) {
            this.tokValidator = tv;
        }
    }


    /**
     * 
     */
    public FileshareTokenRealm () {
        setAuthenticationTokenClass(RealmTokenToken.class);
        setName("FILE"); //$NON-NLS-1$
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
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#supports(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    public boolean supports ( AuthenticationToken token ) {
        return token instanceof RealmTokenToken && this.getName().equals( ( (RealmTokenToken) token ).getRealm());
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken tok ) throws AuthenticationException {

        if ( ! ( tok instanceof RealmTokenToken ) ) {
            return null;
        }

        PrincipalCollection princs = this.tokValidator.validate((RealmTokenToken) tok);
        return new SimpleAuthenticationInfo(princs, tok.getCredentials());
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
        return "token"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getBefore()
     */
    @Override
    public Collection<String> getBefore () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getAfter()
     */
    @Override
    public Collection<String> getAfter () {
        return null;
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
        return new AuthResponse(AuthResponseType.FAIL);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#changePassword(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.principal.UserPrincipal, eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse changePassword ( LoginContext ctx, UserPrincipal up, LoginSession sess ) {
        return new AuthResponse(AuthResponseType.FAIL);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#postauth(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse postauth ( LoginContext ctx, LoginSession sess ) {
        return new AuthResponse(AuthResponseType.FAIL);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#preauth(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse preauth ( LoginContext ctx, LoginSession sess ) {
        return new AuthResponse(AuthResponseType.FAIL);
    }

}
