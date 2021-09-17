/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;
import org.apache.shiro.util.ByteSource;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.scrypt.SCryptParams;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.crypto.scrypt.SCryptUtil;
import eu.agno3.runtime.security.DefaultCredentialsMatcher;
import eu.agno3.runtime.security.SaltedHash;


/**
 * @author mbechler
 *
 */
@Component ( service = DefaultCredentialsMatcher.class )
public class SCryptCredentialMatcher implements DefaultCredentialsMatcher {

    private static final Logger log = Logger.getLogger(SCryptCredentialMatcher.class);
    private static final Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$
    private static final String SHA1PRNG = "SHA1PRNG"; //$NON-NLS-1$

    private static final SCryptParams DEFAULT_PARAMS = new SCryptParams(1 << 14 - 1, 8, 1);

    private SCryptParams target = DEFAULT_PARAMS;


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.credential.CredentialsMatcher#doCredentialsMatch(org.apache.shiro.authc.AuthenticationToken,
     *      org.apache.shiro.authc.AuthenticationInfo)
     */
    @Override
    public boolean doCredentialsMatch ( AuthenticationToken tok, AuthenticationInfo info ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Checking credentials for " + tok.getPrincipal()); //$NON-NLS-1$
        }

        if ( ! ( info instanceof SaltedAuthenticationInfo ) ) {
            throw new ShiroException("Passed not salted credentials into scrypt"); //$NON-NLS-1$
        }

        SaltedAuthenticationInfo sinfo = (SaltedAuthenticationInfo) info;
        String salt = retrieveStoredSalt(sinfo);
        String credentials = retrieveStoredCredentials(info);

        if ( log.isDebugEnabled() ) {
            log.debug("Stored salt " + salt); //$NON-NLS-1$
            log.debug("Stored credentials " + credentials); //$NON-NLS-1$
        }

        byte[] credentialsBytes = Base64.decodeBase64(credentials);
        byte[] toCheckBytes = encodePasswordToCheck(tok);

        return SCryptUtil.check(toCheckBytes, salt, credentialsBytes);
    }


    private static byte[] encodePasswordToCheck ( AuthenticationToken tok ) {
        String toCheck = new String((char[]) tok.getCredentials());
        return toCheck.getBytes(UTF8);
    }


    /**
     * 
     * @param passwordHash
     * @return the password hash portion as a strign
     */
    protected String encodePasswordHash ( byte[] hash ) {
        return Base64.encodeBase64String(hash);
    }


    private static String retrieveStoredCredentials ( AuthenticationInfo info ) {
        return new String((char[]) info.getCredentials());
    }


    private static String retrieveStoredSalt ( SaltedAuthenticationInfo sinfo ) {
        ByteSource credentialsSalt = sinfo.getCredentialsSalt();
        return new String(credentialsSalt.getBytes(), UTF8);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.DefaultCredentialsMatcher#hashNeedsUpdate(org.apache.shiro.authc.AuthenticationInfo)
     */
    @Override
    public boolean hashNeedsUpdate ( AuthenticationInfo info ) {

        if ( ! ( info instanceof SaltedAuthenticationInfo ) ) {
            throw new ShiroException("Passed not salted credentials into scrypt"); //$NON-NLS-1$
        }

        SaltedAuthenticationInfo sinfo = (SaltedAuthenticationInfo) info;
        SCryptParams p = SCryptUtil.getParamsFromSalt(retrieveStoredSalt(sinfo));
        return !this.target.equals(p);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.DefaultCredentialsMatcher#updateHash(org.apache.shiro.authc.AuthenticationToken,
     *      org.apache.shiro.authc.AuthenticationInfo)
     */
    @Override
    public SaltedHash updateHash ( AuthenticationToken tok, AuthenticationInfo info ) {
        if ( ! ( info instanceof SaltedAuthenticationInfo ) ) {
            throw new ShiroException("Passed not salted credentials into scrypt"); //$NON-NLS-1$
        }
        return this.generatePasswordHash(new String((char[]) tok.getCredentials()));
    }


    /**
     * @param password
     * @return a password hash
     */
    @Override
    public SaltedHash generatePasswordHash ( String password ) {
        try {
            byte[] salt = new byte[32];
            SecureRandom.getInstance(SHA1PRNG).nextBytes(salt);
            SCryptResult r = SCryptUtil.generate(password.getBytes(Charset.forName("UTF-8")), salt, this.target); //$NON-NLS-1$
            return new SaltedHash(this.encodePasswordHash(r.getKey()), r.getSalt());
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new AuthenticationException("Internal error", e); //$NON-NLS-1$
        }

    }

}
