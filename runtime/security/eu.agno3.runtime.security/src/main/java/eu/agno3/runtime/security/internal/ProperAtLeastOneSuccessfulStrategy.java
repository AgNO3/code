/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2015 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.realm.Realm;

import eu.agno3.runtime.security.password.PasswordPolicyException;


/**
 * @author mbechler
 *
 */
public class ProperAtLeastOneSuccessfulStrategy extends AtLeastOneSuccessfulStrategy implements AuthenticationStrategy {

    private List<AuthenticationException> exceptions = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.pam.AbstractAuthenticationStrategy#afterAttempt(org.apache.shiro.realm.Realm,
     *      org.apache.shiro.authc.AuthenticationToken, org.apache.shiro.authc.AuthenticationInfo,
     *      org.apache.shiro.authc.AuthenticationInfo, java.lang.Throwable)
     */
    @Override
    public AuthenticationInfo afterAttempt ( Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo,
            AuthenticationInfo aggregateInfo, Throwable t ) throws AuthenticationException {

        AuthenticationException e = null;
        if ( t != null && t.getCause() instanceof InvocationTargetException && t.getCause().getCause() instanceof AuthenticationException ) {
            e = (AuthenticationException) t.getCause().getCause();
        }
        else if ( t instanceof AuthenticationException ) {
            e = (AuthenticationException) t;
        }

        if ( e != null ) {
            this.exceptions.add(e);
        }

        return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy#afterAllAttempts(org.apache.shiro.authc.AuthenticationToken,
     *      org.apache.shiro.authc.AuthenticationInfo)
     */
    @Override
    public AuthenticationInfo afterAllAttempts ( AuthenticationToken token, AuthenticationInfo aggregate ) throws AuthenticationException {

        if ( aggregate == null || aggregate.getPrincipals() == null || aggregate.getPrincipals().isEmpty() ) {
            throwBestAuthenticationException();
            throw new UnknownAccountException("No realm authenticated the token"); //$NON-NLS-1$
        }

        return aggregate;
    }


    /**
     * 
     */
    private void throwBestAuthenticationException () {
        // password change exceptions come first
        throwIfFound(ExpiredCredentialsException.class);
        throwIfFound(PasswordPolicyException.class);

        throwIfFound(DisabledAccountException.class);
        throwIfFound(ExcessiveAttemptsException.class);

        throwIfFound(CredentialsException.class);
        throwIfFound(AccountException.class);
    }


    /**
     * @param class1
     */
    private <T extends AuthenticationException> void throwIfFound ( Class<T> exType ) {
        for ( AuthenticationException e : this.exceptions ) {
            if ( exType.isAssignableFrom(e.getClass()) ) {
                throw e;
            }
        }
    }

}
