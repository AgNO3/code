/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2015 by mbechler
 */
package eu.agno3.runtime.security.krb.internal;


import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.MutablePrincipalCollection;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.i18n.ResourceBundleService;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.ADUserInfo;
import eu.agno3.runtime.net.ad.ADUserInfoImpl;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.ad.ntlm.NTLMAcceptor;
import eu.agno3.runtime.net.ad.ntlm.NTLMContext;
import eu.agno3.runtime.net.ad.ntlm.NTLMException;
import eu.agno3.runtime.net.krb5.AuthDataEntry;
import eu.agno3.runtime.net.krb5.GSSUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.Krb5SubjectUtil;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.krb.ActiveDirectoryPrincipal;
import eu.agno3.runtime.security.krb.ActiveDirectoryRealmAuthToken;
import eu.agno3.runtime.security.krb.ActiveDirectoryRealmNTLMAuthToken;
import eu.agno3.runtime.security.krb.KerberosPrincipal;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.principal.factors.CertificateFactor;
import eu.agno3.runtime.security.principal.factors.PasswordFactor;
import eu.agno3.runtime.security.token.FallbackRealmUserPasswordChangeToken;
import eu.agno3.runtime.security.token.FallbackRealmUserPasswordToken;
import eu.agno3.runtime.security.web.login.CustomWebAuthAuthenticationException;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.sid.SID;

import jcifs.pac.PACDecodingException;
import jcifs.pac.kerberos.KerberosAuthData;
import jcifs.pac.kerberos.KerberosPacAuthData;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, LoginRealm.class, AuthorizingRealm.class
}, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "auth.ad" )
public class ActiveDirectoryWebAuthRealm extends SPNEGOWebAuthRealm {

    static final String NTLM_MECH = "NTLM"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ActiveDirectoryWebAuthRealm.class);
    private static final SID ANONYMOUS_SID = SID.fromString("S-1-5-7"); //$NON-NLS-1$

    private NTLMAcceptor ntlmAcceptor;
    private ADRealm adRealm;

    private boolean sendNTLMChallenge = false;
    private boolean acceptNTLMFallback = true;
    private boolean disablePACValidation;
    private boolean disablePACs;

    private boolean rejectNonADPrincipals;

    private Set<String> acceptDomainSids;
    private Set<String> rejectDomainSids;

    private Set<String> requiredSids;
    private Set<String> rejectSids;

    private Map<String, Set<String>> sidRoles;

    private boolean acceptOnlyLocal;
    private boolean requireDomainUserGroup;


    @Reference ( updated = "updatedKerberosRealm" )
    protected synchronized void setKerberosRealm ( ADRealm adrm ) {
        super.setKerberosRealm(adrm);
        this.adRealm = adrm;
    }


    protected synchronized void unsetKerberosRealm ( ADRealm adrm ) {
        super.unsetKerberosRealm(adrm);
        if ( this.adRealm == adrm ) {
            this.adRealm = null;
        }
    }


    protected synchronized void updatedKerberosRealm ( ADRealm rlm ) {
        super.updatedKerberosRealm(rlm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#setUserMapper(eu.agno3.runtime.security.UserMapper)
     */
    @Override
    @Reference
    protected synchronized void setUserMapper ( UserMapper um ) {
        super.setUserMapper(um);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#unsetUserMapper(eu.agno3.runtime.security.UserMapper)
     */
    @Override
    protected synchronized void unsetUserMapper ( UserMapper um ) {
        super.unsetUserMapper(um);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#setSecureRandomProvider(eu.agno3.runtime.crypto.random.SecureRandomProvider)
     */
    @Override
    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        super.setSecureRandomProvider(srp);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#unsetSecureRandomProvider(eu.agno3.runtime.crypto.random.SecureRandomProvider)
     */
    @Override
    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        super.unsetSecureRandomProvider(srp);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#setResourceBundleService(eu.agno3.runtime.i18n.ResourceBundleService)
     */
    @Override
    @Reference
    protected synchronized void setResourceBundleService ( ResourceBundleService rbs ) {
        super.setResourceBundleService(rbs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#unsetResourceBundleService(eu.agno3.runtime.i18n.ResourceBundleService)
     */
    @Override
    protected synchronized void unsetResourceBundleService ( ResourceBundleService rbs ) {
        super.unsetResourceBundleService(rbs);
    }


    @Activate
    @Override
    protected synchronized void activate ( ComponentContext ctx ) {
        if ( this.adRealm == null ) {
            return;
        }
        super.activate(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#modified(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        super.modified(ctx);
    }


    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        super.deactivate(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#getDefaultKeytabId()
     */
    @Override
    protected String getDefaultKeytabId () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#parseConfig(java.util.Dictionary)
     */
    @Override
    protected void parseConfig ( Dictionary<String, Object> properties ) {
        super.parseConfig(properties);

        this.acceptOnlyLocal = ConfigUtil.parseBoolean(properties, "acceptOnlyLocalDomains", false); //$NON-NLS-1$
        this.rejectNonADPrincipals = ConfigUtil.parseBoolean(properties, "rejectNonAD", false); //$NON-NLS-1$
        this.sendNTLMChallenge = ConfigUtil.parseBoolean(properties, "sendNTLMChallenge", false); //$NON-NLS-1$
        this.acceptNTLMFallback = ConfigUtil.parseBoolean(properties, "acceptNTLMFallback", true); //$NON-NLS-1$
        this.disablePACs = ConfigUtil.parseBoolean(properties, "disablePACs", false); //$NON-NLS-1$
        this.disablePACValidation = ConfigUtil.parseBoolean(properties, "disablePACValidation", false); //$NON-NLS-1$

        this.requireDomainUserGroup = ConfigUtil.parseBoolean(properties, "requireDomainUserGroup", true); //$NON-NLS-1$

        this.acceptDomainSids = ConfigUtil.parseStringSet(properties, "acceptDomains", null); //$NON-NLS-1$
        this.rejectDomainSids = ConfigUtil.parseStringSet(properties, "rejectDomains", null); //$NON-NLS-1$

        this.requiredSids = ConfigUtil.parseStringSet(properties, "requireSid", null); //$NON-NLS-1$
        this.rejectSids = ConfigUtil.parseStringSet(properties, "rejectSids", null); //$NON-NLS-1$

        Map<String, Set<String>> sidRolesAttr = new HashMap<>();
        for ( Entry<String, List<String>> e : ConfigUtil.parseStringMultiMap(properties, "sidRoles", Collections.EMPTY_MAP).entrySet() ) { //$NON-NLS-1$
            sidRolesAttr.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        this.sidRoles = sidRolesAttr;
    }


    @Override
    protected javax.security.auth.kerberos.KerberosPrincipal setupEarly ( ComponentContext ctx ) {
        try {
            this.adRealm.ensureJoined();
        }
        catch ( ADException e ) {
            log.error("AD domain not accessible " + this.getRealm().getKrbRealm(), e); //$NON-NLS-1$
            return null;
        }

        return this.adRealm.makeServicePrincipal(this.getService());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#setupLate(org.osgi.service.component.ComponentContext)
     */
    @Override
    protected void setupLate ( ComponentContext ctx ) {
        if ( !this.acceptNTLMFallback ) {
            return;
        }
        try {
            this.ntlmAcceptor = new NTLMAcceptor(this.getSecureRandom(), this.adRealm);
            log.debug("NTLM init ok"); //$NON-NLS-1$
        }
        catch (
            NTLMException |
            ADException e ) {
            log.error("Failed to initialize NTLM", e); //$NON-NLS-1$
        }
    }


    @Override
    protected javax.security.auth.Subject getSubject ( javax.security.auth.kerberos.KerberosPrincipal servicePrincipal ) throws KerberosException {

        if ( this.getKeyTabId() != null ) {
            return super.getSubject(servicePrincipal);
        }

        try {
            this.adRealm.ensureHostSPN(servicePrincipal);
        }
        catch ( ADException e ) {
            log.error("Failed to register host SPN", e); //$NON-NLS-1$
        }

        try {

            return Krb5SubjectUtil.getAcceptorSubject(servicePrincipal, this.adRealm.getMachinePasswords(), this.adRealm.getSalts(servicePrincipal));
        }
        catch ( ADException e ) {
            throw new KerberosException(
                "No service principal configured and the machine password could not be obtained: " + this.getRealm().getKrbRealm(), //$NON-NLS-1$
                e);
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#supports(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    public boolean supports ( org.apache.shiro.authc.AuthenticationToken token ) {
        return super.supports(token) || ( token instanceof ActiveDirectoryRealmNTLMAuthToken
                && this.getName().equals( ( (ActiveDirectoryRealmNTLMAuthToken) token ).getRealm()) );
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken tok ) throws AuthenticationException {

        if ( tok instanceof FallbackRealmUserPasswordToken || tok instanceof FallbackRealmUserPasswordChangeToken ) {
            return doFallbackAuthentication(tok);
        }

        try {
            if ( tok instanceof ActiveDirectoryRealmAuthToken ) {
                return doGetKerberosInfo((ActiveDirectoryRealmAuthToken) tok);
            }
            else if ( this.acceptNTLMFallback && tok instanceof ActiveDirectoryRealmNTLMAuthToken ) {
                return doGetNTLMAuthInfo((ActiveDirectoryRealmNTLMAuthToken) tok);
            }
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( CustomWebAuthAuthenticationException e ) {
            throw new AccountException(e);
        }

        throw new UnsupportedTokenException();
    }


    /**
     * @param tok
     * @return
     * @throws UserLicenseLimitExceededException
     */
    private AuthenticationInfo doGetNTLMAuthInfo ( ActiveDirectoryRealmNTLMAuthToken tok ) throws UserLicenseLimitExceededException {
        ADUserInfo info = tok.getInfo();
        if ( info == null ) {
            throw new AuthenticationException();
        }
        checkADUser(info);
        String domainName = tok.getInfo().getDomainName();
        UserPrincipal mappedUser = this.userMapper.getMappedUser(info.getAccountName(), domainName, null);
        MutablePrincipalCollection col = new SimplePrincipalCollection(mappedUser, getName());
        col.add(new ActiveDirectoryPrincipal(tok.getInfo()), getName());
        return new SimpleAuthenticationInfo(col, info);
    }


    /**
     * @param tok
     * @return
     * @throws UserLicenseLimitExceededException
     */
    private AuthenticationInfo doGetKerberosInfo ( ActiveDirectoryRealmAuthToken tok ) throws UserLicenseLimitExceededException {
        javax.security.auth.kerberos.KerberosPrincipal principal = tok.getPrincipal();

        if ( this.rejectNonADPrincipals && tok.getAdUserInfo() == null ) {
            throw new CustomWebAuthAuthenticationException(
                "rejectnonad", //$NON-NLS-1$
                "Rejecting non active directory users"); //$NON-NLS-1$
        }

        MutablePrincipalCollection col = getPureKerberosPrincipalCollection(principal);
        if ( tok.getAdUserInfo() != null ) {
            checkADUser(tok.getAdUserInfo());
            col.addAll(addAuthFactors(tok.getAdUserInfo()), getName());
            col.add(new ActiveDirectoryPrincipal(tok.getAdUserInfo()), getName());
        }
        else {
            col.addAll(getAuthFactor(), getName());
        }
        return new SimpleAuthenticationInfo(col, principal);
    }


    /**
     * @param userPrincipal
     * @param ctx
     * @param credentials
     * @return
     * @throws GSSException
     * @throws IOException
     * @throws UserLicenseLimitExceededException
     */
    @Override
    protected AuthenticationInfo haveFallbackContext ( javax.security.auth.kerberos.KerberosPrincipal userPrincipal, GSSContext ctx,
            Object credentials ) throws IOException, GSSException, UserLicenseLimitExceededException {

        ADUserInfo adUserInfo = validatePAC(ctx);

        if ( this.rejectNonADPrincipals && adUserInfo == null ) {
            throw new CustomWebAuthAuthenticationException(
                "rejectnonad", //$NON-NLS-1$
                "Rejecting non active directory users"); //$NON-NLS-1$
        }

        MutablePrincipalCollection col = getPureKerberosPrincipalCollection(userPrincipal);
        if ( adUserInfo != null ) {
            checkADUser(adUserInfo);
            col.addAll(addAuthFactors(adUserInfo), getName());
            col.add(new ActiveDirectoryPrincipal(adUserInfo), getName());
        }
        else {
            col.addAll(getAuthFactor(), getName());
        }
        return new SimpleAuthenticationInfo(col, credentials);
    }


    /**
     * @param adUserInfo
     * @return
     */
    private Collection<AuthFactor> addAuthFactors ( ADUserInfo adUserInfo ) {
        Set<AuthFactor> factors = new HashSet<>();

        if ( this.getRealm().getAuthFactors() <= 1 ) {
            factors.add(new PasswordFactor(null, new Duration(adUserInfo.getPwLastChange(), DateTime.now())));
            if ( adUserInfo.isSmartCardLoginRequired() ) {
                factors.add(new CertificateFactor(-1, true, false));
            }
        }
        else {
            factors.addAll(getAuthFactor());
        }

        return factors;
    }


    /**
     * @param adUserInfo
     */
    private void checkADUser ( ADUserInfo adUserInfo ) {
        if ( ANONYMOUS_SID.equals(adUserInfo.getUserSid()) ) {
            throw new DisabledAccountException("Anonymous login not permitted"); //$NON-NLS-1$
        }
        checkDomain(adUserInfo);
        checkUser(adUserInfo);
    }


    /**
     * @param adUserInfo
     */
    private void checkUser ( ADUserInfo adUserInfo ) {

        if ( this.rejectSids == null && this.requiredSids == null && !this.requireDomainUserGroup ) {
            return;
        }

        List<SID> allMemberships = new LinkedList<>();
        allMemberships.add(adUserInfo.getUserSid());
        allMemberships.add(adUserInfo.getPrimaryGroupSid());
        allMemberships.addAll(adUserInfo.getGroupSids());

        if ( this.requireDomainUserGroup ) {
            boolean found = false;
            for ( SID memberSid : allMemberships ) {
                if ( memberSid.getRid() == 513 ) {
                    found = true;
                    break;
                }
            }

            if ( !found ) {
                throw new CustomWebAuthAuthenticationException(
                    "adUserNotAccepted", //$NON-NLS-1$
                    "The user is not accepted"); //$NON-NLS-1$
            }
        }

        if ( this.rejectSids != null ) {
            for ( SID memberSid : allMemberships ) {
                if ( this.rejectSids.contains(memberSid.toString()) ) {
                    throw new CustomWebAuthAuthenticationException(
                        "adUserRejected", //$NON-NLS-1$
                        "The user is rejected"); //$NON-NLS-1$
                }
            }
        }

        if ( this.requiredSids != null ) {
            boolean foundAny = false;
            for ( SID memberSid : allMemberships ) {
                if ( this.requiredSids.contains(memberSid.toString()) ) {
                    foundAny = true;
                    break;
                }
            }
            if ( !foundAny ) {
                throw new CustomWebAuthAuthenticationException(
                    "adUserNotAccepted", //$NON-NLS-1$
                    "The user is not accepted"); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param adUserInfo
     */
    private void checkDomain ( ADUserInfo adUserInfo ) {
        String domainSid = adUserInfo.getUserSid().getParent().toString();
        if ( this.acceptOnlyLocal ) {
            if ( !adUserInfo.getUserSid().getParent().equals(this.adRealm.getDomainSid()) ) {
                throw new CustomWebAuthAuthenticationException(
                    "adDomainNotAccepted", //$NON-NLS-1$
                    "The domain is not accepted (non-local)"); //$NON-NLS-1$
            }
        }

        if ( this.rejectDomainSids != null ) {
            if ( this.rejectDomainSids.contains(domainSid) ) {
                throw new CustomWebAuthAuthenticationException(
                    "adDomainRejected", //$NON-NLS-1$
                    "The domain is rejected (SID)"); //$NON-NLS-1$
            }
        }

        if ( this.acceptDomainSids != null ) {
            if ( !this.acceptDomainSids.contains(domainSid) ) {
                throw new CustomWebAuthAuthenticationException(
                    "adDomainNotAccepted", //$NON-NLS-1$
                    "The domain is not accepted (SID)"); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo ( PrincipalCollection princs ) {
        Set<String> roles = new HashSet<>();
        KerberosPrincipal kerberosPrincipal = princs.oneByType(KerberosPrincipal.class);
        if ( kerberosPrincipal != null ) {
            roles.addAll(getPrincipalRoles(kerberosPrincipal.getKerberosPrincipal().getName()));
        }

        ActiveDirectoryPrincipal adPrincipal = princs.oneByType(ActiveDirectoryPrincipal.class);
        if ( adPrincipal != null && adPrincipal.getAdUserInfo() != null ) {
            roles.addAll(getActiveDirectoryRoles(adPrincipal.getAdUserInfo()));
        }

        if ( kerberosPrincipal == null && adPrincipal == null ) {
            return null;
        }
        return new SimpleAuthorizationInfo(roles);
    }


    /**
     * @param adUserInfo
     * @return
     */
    private Set<String> getActiveDirectoryRoles ( ADUserInfo adUserInfo ) {
        Set<String> roles = new HashSet<>();
        List<SID> allSids = new LinkedList<>();
        allSids.add(adUserInfo.getUserSid());
        allSids.add(adUserInfo.getPrimaryGroupSid());
        allSids.addAll(adUserInfo.getGroupSids());

        if ( log.isDebugEnabled() ) {
            log.debug("Found SIDs " + allSids); //$NON-NLS-1$
        }

        for ( SID memberSid : allSids ) {
            Set<String> sidAssignedRoles = this.sidRoles.get(memberSid.toString());
            if ( sidAssignedRoles != null ) {
                roles.addAll(sidAssignedRoles);
            }
        }
        return roles;
    }


    /**
     * @param ctx
     * @throws IOException
     * @throws GSSException
     */
    @Override
    protected AuthenticationInfo contextEstablished ( GSSContext ctx ) throws IOException, GSSException {
        ADUserInfo adUserInfo = null;
        if ( this.adRealm != null ) {
            adUserInfo = validatePAC(ctx);
        }

        try {
            log.debug("Logging in (GSSAPI)"); //$NON-NLS-1$
            return doGetAuthenticationInfo(new ActiveDirectoryRealmAuthToken(getName(), ctx, adUserInfo));
        }
        catch (
            AuthenticationException |
            UndeclaredThrowableException e ) {
            handleLoginFailure(e);
            return null;
        }
    }


    @Override
    protected AuthResponse preAuthentication ( HttpServletRequest req, HttpServletResponse resp, String mech, long connId, byte[] byteToken ) {
        boolean isNtlm = NTLM_MECH.equalsIgnoreCase(mech) || NTLMAcceptor.isNTLMToken(byteToken);
        if ( this.acceptNTLMFallback && isNtlm ) {
            return handleNTLMSSP(byteToken, req, resp, connId, mech);
        }
        else if ( isNtlm ) {
            throw new CustomWebAuthAuthenticationException(
                "ntlm", //$NON-NLS-1$
                "Browser responded with NTLM, this is disallowed"); //$NON-NLS-1$
        }

        return new AuthResponse(AuthResponseType.CONTINUE);
    }


    @Override
    protected boolean isAcceptedMechanism ( String mech ) {
        return super.isAcceptedMechanism(mech) || NTLM_MECH.equalsIgnoreCase(mech);
    }


    @Override
    public List<String> getChallenges () {
        List<String> challenges = super.getChallenges();
        if ( this.sendNTLMChallenge ) {
            challenges.add(NTLM_MECH);
        }
        return challenges;
    }


    /**
     * @param byteToken
     * @return
     * @throws ADException
     * @throws IOException
     * @throws GSSException
     * @throws KerberosException
     */
    private ADUserInfo validatePAC ( GSSContext ctx ) throws IOException, GSSException {
        if ( this.disablePACs ) {
            return null;
        }
        int kvno;
        try {
            kvno = GSSUtil.getServiceKVNO(ctx);
            if ( log.isDebugEnabled() ) {
                log.debug("KVNO is " + kvno); //$NON-NLS-1$
            }
        }
        catch ( KerberosException ex ) {
            log.error("Failed to extract service KVNO", ex); //$NON-NLS-1$
            throw new CustomWebAuthAuthenticationException(
                "internal", //$NON-NLS-1$
                "Failed to verify PAC", //$NON-NLS-1$
                ex);
        }
        for ( AuthDataEntry e : GSSUtil.getAuthDataFromContext(ctx) ) {
            try {
                List<KerberosAuthData> data = this.acceptor.parseAuthData(e.getData(), kvno);
                for ( KerberosAuthData authData : data ) {
                    if ( authData instanceof KerberosPacAuthData ) {
                        log.debug("Found PAC"); //$NON-NLS-1$
                        return extractPACData(authData);
                    }
                }
            }
            catch (
                PACDecodingException |
                KerberosException ex ) {
                log.warn("Failed to decode auth data", ex); //$NON-NLS-1$
                continue;
            }
            catch ( ADException ex ) {
                log.debug("PAC validation failed", ex); //$NON-NLS-1$
                throw new CustomWebAuthAuthenticationException(
                    "pacfail", //$NON-NLS-1$
                    "Failed to verify PAC", //$NON-NLS-1$
                    ex);
            }
        }

        return null;
    }


    /**
     * @param authData
     * @return
     * @throws ADException
     */
    private ADUserInfo extractPACData ( KerberosAuthData authData ) throws ADException {
        if ( this.disablePACValidation ) {
            return ADUserInfoImpl.fromPAC( ( (KerberosPacAuthData) authData ).getPac().getLogonInfo());
        }

        try ( NetlogonConnection conn = this.adRealm.getNetlogonConnection() ) {
            return conn.getNetlogonOperations().pacValidate( ( (KerberosPacAuthData) authData ).getPac());
        }
    }


    /**
     * @param byteToken
     * @param resp
     * @param req
     */
    private AuthResponse handleNTLMSSP ( byte[] byteToken, HttpServletRequest req, HttpServletResponse resp, long connId, String mech ) {
        if ( this.ntlmAcceptor == null ) {
            throw new CustomWebAuthAuthenticationException(
                "ntlm", //$NON-NLS-1$
                "Browser responded with NTLM"); //$NON-NLS-1$
        }

        log.debug("Doing NTLM"); //$NON-NLS-1$
        NTLMContext ctx = this.ntlmAcceptor.create(connId, getConnection(req), true);

        if ( !ctx.isComplete() ) {
            byte[] outToken;
            try {
                outToken = this.ntlmAcceptor.accept(ctx, byteToken);
            }
            catch ( NTLMException e ) {
                log.debug("NTLM failure", e); //$NON-NLS-1$
                throw new CustomWebAuthAuthenticationException(
                    "ntlm-fail", //$NON-NLS-1$
                    "NTLM failed", //$NON-NLS-1$
                    e);
            }

            if ( outToken != null ) {
                resp.setHeader(
                    "WWW-Authenticate", //$NON-NLS-1$
                    mech + " " + Base64.encodeBase64String(outToken)); //$NON-NLS-1$

                if ( !ctx.isComplete() ) {
                    log.debug("Need more data"); //$NON-NLS-1$
                    resp.setStatus(401);
                    return new AuthResponse(AuthResponseType.BREAK);
                }
            }

            if ( !ctx.isComplete() ) {
                throw new CustomWebAuthAuthenticationException(
                    "ntlm-incomplete", //$NON-NLS-1$
                    "NTLM auth did not complete"); //$NON-NLS-1$
            }
        }

        try {
            log.debug("Loggin in (NTLMSSP)"); //$NON-NLS-1$
            return new AuthResponse(AuthResponseType.COMPLETE, doGetAuthenticationInfo(new ActiveDirectoryRealmNTLMAuthToken(getName(), ctx)));
        }
        catch (
            AuthenticationException |
            UndeclaredThrowableException e ) {
            handleLoginFailure(e);
            return new AuthResponse(AuthResponseType.FAIL);
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.krb.internal.SPNEGOWebAuthRealm#doCleanup()
     */
    @Override
    protected void doCleanup () {
        super.doCleanup();
        if ( this.ntlmAcceptor != null ) {
            this.ntlmAcceptor.cleanUp();
        }
    }

}
