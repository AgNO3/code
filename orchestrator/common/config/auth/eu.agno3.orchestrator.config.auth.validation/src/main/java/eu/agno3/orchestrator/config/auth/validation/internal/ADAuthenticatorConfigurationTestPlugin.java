/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.auth.validation.internal;


import java.io.IOException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import eu.agno3.orchestrator.config.auth.UserPasswordAuthTestParams;
import eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfig;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.web.validation.KRBValidationUtils;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.ADRealmManager;
import eu.agno3.runtime.net.ad.ADUserInfo;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.dns.SRVEntries;
import eu.agno3.runtime.net.dns.SRVEntry;
import eu.agno3.runtime.net.krb5.AuthDataEntry;
import eu.agno3.runtime.net.krb5.GSSUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.Krb5SubjectUtil;
import eu.agno3.runtime.net.krb5.UserPasswordGSSAPIFactory;
import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.pac.PACDecodingException;
import jcifs.pac.kerberos.KerberosAuthData;
import jcifs.pac.kerberos.KerberosPacAuthData;
import jcifs.pac.kerberos.KerberosRelevantAuthData;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ConfigTestPlugin.class
} )
public class ADAuthenticatorConfigurationTestPlugin implements ConfigTestPluginAsync<ADAuthenticatorConfig> {

    private static final Logger log = Logger.getLogger(ADAuthenticatorConfigurationTestPlugin.class);

    private ADRealmManager adrlm;
    private SecureRandomProvider randomProvider;
    private SecureRandom secureRandom;

    private static final String[] MINIMAL_ATTRS = new String[] {
        "objectClass" //$NON-NLS-1$
    };


    @Reference
    protected synchronized void setADRealmManager ( ADRealmManager arm ) {
        this.adrlm = arm;
    }


    protected synchronized void unsetADRealmManager ( ADRealmManager arm ) {
        if ( this.adrlm == arm ) {
            this.adrlm = null;
        }
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.randomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.randomProvider == srp ) {
            this.randomProvider = null;
        }
    }


    private SecureRandom getSecureRandom () {
        if ( this.secureRandom == null ) {
            this.secureRandom = this.randomProvider.getSecureRandom();
        }
        return this.secureRandom;
    }


    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return Collections.singleton(ConfigTestPluginRunOn.SERVER);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<ADAuthenticatorConfig> getTargetType () {
        return ADAuthenticatorConfig.class;
    }


    @Override
    public ConfigTestResult testAsync ( ADAuthenticatorConfig config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {
        log.debug("Running AD authenticator test"); //$NON-NLS-1$

        if ( ! ( params instanceof UserPasswordAuthTestParams ) ) {
            throw new ModelServiceException("Invalid test parameters"); //$NON-NLS-1$
        }
        UserPasswordAuthTestParams p = (UserPasswordAuthTestParams) params;

        try {
            String domain = config.getDomain();
            if ( log.isDebugEnabled() ) {
                log.debug("Realm is " + domain); //$NON-NLS-1$
            }

            if ( !this.adrlm.exists(domain) ) {
                r.error("AD_REALM_UNCONFIGURED", domain); //$NON-NLS-1$
                return r.state(ConfigTestState.FAILURE);
            }

            ADRealm rlm = this.adrlm.getRealmInstance(domain);
            try {
                // make sure kerberos is configured
                this.adrlm.getRealmConfig(domain);

                if ( log.isDebugEnabled() ) {
                    log.debug("Actual domain name is " + rlm.getDomainName()); //$NON-NLS-1$
                }

                if ( !checkSRV(r, rlm) ) {
                    return r.state(ConfigTestState.FAILURE);
                }

                if ( !checkJoin(r, h, domain, rlm) ) {
                    return r.state(ConfigTestState.FAILURE);
                }

                if ( !checkNetlogon(r, h, rlm, p, getSecureRandom()) ) {
                    return r.state(ConfigTestState.FAILURE);
                }

                if ( !checkKerberos(r, h, rlm, p) ) {
                    return r.state(ConfigTestState.FAILURE);
                }

                if ( !checkLDAP(r, h, config, rlm, p) ) {
                    return r.state(ConfigTestState.FAILURE);
                }

                return r.state(ConfigTestState.SUCCESS);
            }
            finally {
                log.trace("Closing realm"); //$NON-NLS-1$
                rlm.close();
                log.trace("Closed realm"); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.debug("Uncaught exception", e); //$NON-NLS-1$
            r.error("FAIL_AD_UNKNOWN", e.getMessage()); //$NON-NLS-1$
            return r.state(ConfigTestState.FAILURE);
        }
    }


    /**
     * @param r
     * @param rlm
     * @param p
     * @return
     */
    private static boolean checkKerberos ( ConfigTestResult r, ConfigTestAsyncHandler h, ADRealm rlm, UserPasswordAuthTestParams p ) {
        if ( !StringUtils.isBlank(p.getUsername()) && !StringUtils.isBlank(p.getPassword()) ) {
            UserPasswordGSSAPIFactory subjFactory = new UserPasswordGSSAPIFactory(p.getUsername(), p.getPassword(), rlm.getKrbRealm());

            try {
                r.info("AD_REALM_KRB_CHECK", p.getUsername(), rlm.getKrbRealm()); //$NON-NLS-1$
                h.update(r);

                Subject initiatorSubject = subjFactory.getSubject();
                Subject acceptorSubject = Krb5SubjectUtil
                        .getAcceptorSubject(rlm.getHostPrincipal(), rlm.getMachinePasswords(), rlm.getSalts(rlm.getHostPrincipal()));

                GSSContext ctx = Krb5SubjectUtil
                        .validateServiceCredentials(subjFactory.getPrincipal(), initiatorSubject, rlm.getHostPrincipal(), acceptorSubject, 100);
                try {
                    ADUserInfo ui = validatePAC(r, rlm, acceptorSubject, ctx);

                    if ( ui != null ) {
                        r.info(
                            "AD_REALM_PAC_SUCCESS", //$NON-NLS-1$
                            ui.getAccountName(),
                            ui.getDisplayName(),
                            ui.getUserSid().toString(),
                            ui.getPrimaryGroupSid().toString(),
                            ui.getGroupSids().toString());
                    }
                    else {
                        r.error("AD_REALM_NO_PAC"); //$NON-NLS-1$
                    }
                }
                catch ( Exception e ) {
                    r.error("AD_REALM_PAC_FAIL", e.getMessage()); //$NON-NLS-1$
                }
                finally {
                    ctx.dispose();
                }

                r.info("AD_REALM_KRB_OK", p.getUsername(), rlm.getKrbRealm()); //$NON-NLS-1$
            }
            catch ( KerberosException e ) {
                KRBValidationUtils.handleException(r, e);
            }
            catch (
                ADException |
                GSSException |
                IOException e ) {
                r.error("AD_REALM_KRB_FAIL", p.getUsername(), rlm.getKrbRealm(), e.getMessage()); //$NON-NLS-1$
                log.debug("Error checking kerberos validation", e); //$NON-NLS-1$
            }
        }
        else {
            r.warn("AD_REALM_KRB_NOUSER"); //$NON-NLS-1$
        }
        return true;
    }


    /**
     * @param byteToken
     * @return
     * @throws ADException
     * @throws IOException
     * @throws GSSException
     * @throws KerberosException
     */
    private static ADUserInfo validatePAC ( ConfigTestResult r, ADRealm rlm, Subject acceptorSubject, GSSContext ctx )
            throws IOException, GSSException, KerberosException {

        Key sessionKey = GSSUtil.getSessionKey(ctx);

        int kvno = GSSUtil.getServiceKVNO(ctx);
        if ( log.isDebugEnabled() ) {
            log.debug("KVNO is " + kvno); //$NON-NLS-1$
        }

        String algo = sessionKey.getAlgorithm();
        for ( AuthDataEntry e : GSSUtil.getAuthDataFromContext(ctx) ) {
            try {
                int algoNum = Integer.parseInt(algo);
                List<KerberosAuthData> data = ( new KerberosRelevantAuthData(e.getData(), selectKeys(acceptorSubject, algoNum, kvno)) )
                        .getAuthorizations();
                for ( KerberosAuthData authData : data ) {
                    if ( authData instanceof KerberosPacAuthData ) {
                        log.debug("Found PAC"); //$NON-NLS-1$

                        try ( NetlogonConnection conn = rlm.getNetlogonConnection() ) {
                            return conn.getNetlogonOperations().pacValidate( ( (KerberosPacAuthData) authData ).getPac());
                        }
                        catch ( ADException ex ) {
                            r.error("AD_REALM_PAC_VALIDATE_FAIL", ex.getMessage()); //$NON-NLS-1$
                        }
                    }
                }
            }
            catch (
                PACDecodingException |
                KerberosException ex ) {
                log.debug("Failed to decode auth data", ex); //$NON-NLS-1$
                continue;
            }
        }

        return null;
    }


    /**
     * @param algo
     * @return
     * @throws KerberosException
     */
    private static Map<Integer, KerberosKey> selectKeys ( Subject acceptorSubject, int algoNum, int kvno ) throws KerberosException {
        Map<Integer, KerberosKey> keys = new HashMap<>();
        for ( KerberosKey k : acceptorSubject.getPrivateCredentials(KerberosKey.class) ) {
            if ( k.getVersionNumber() == kvno ) {
                keys.put(k.getKeyType(), k);
            }
        }

        if ( keys.isEmpty() ) {
            throw new KerberosException("Failed to find key for KVNO " + kvno); //$NON-NLS-1$
        }

        return keys;
    }


    /**
     * @param r
     * @param rlm
     */
    private static boolean checkSRV ( ConfigTestResult r, ADRealm rlm ) {
        try {
            SRVEntries dcs = rlm.getDomainControllers();
            List<String> dcentries = new LinkedList<>();
            for ( SRVEntry srvEntry : dcs.getEntries() ) {
                dcentries.add(String.format("%s (prio: %d)", srvEntry.getName(), srvEntry.getPriority())); //$NON-NLS-1$
            }
            r.info("AD_REALM_DCLOOKUP_OK", rlm.getDomainName(), dcentries.toString()); //$NON-NLS-1$
            return true;
        }
        catch ( ADException e ) {
            r.error("AD_REALM_DCLOOKUP_FAIL", rlm.getDomainName()); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param r
     * @param config
     * @param h
     * @param rlm
     * @param p
     */
    private static boolean checkLDAP ( ConfigTestResult r, ConfigTestAsyncHandler h, ADAuthenticatorConfig config, ADRealm rlm,
            UserPasswordAuthTestParams p ) {
        try ( LDAPClient connection = rlm.getConnection() ) {
            r.info("AD_REALM_LDAP_OK"); //$NON-NLS-1$

            checkUserLookup(r, config, p, connection);

            Filter usf = LDAPSchemaStyle.AD.createUserFilter();
            if ( !StringUtils.isBlank(config.getUserSyncFilter()) ) {
                usf = Filter.createANDFilter(usf, Filter.create(config.getUserSyncFilter()));
            }

            SearchResult userResult = connection.search(config.getUserSyncBase(), SearchScope.SUB, usf, MINIMAL_ATTRS);

            if ( userResult.getEntryCount() == 0 ) {
                r.warn("AD_REALM_LDAP_SYNC_NOUSERS"); //$NON-NLS-1$
            }
            else {
                r.info("AD_REALM_LDAP_SYNC_USERS", String.valueOf(userResult.getEntryCount())); //$NON-NLS-1$
            }

            Filter gsf = LDAPSchemaStyle.AD.createGroupFilter();
            if ( !StringUtils.isBlank(config.getUserSyncFilter()) ) {
                gsf = Filter.createANDFilter(gsf, Filter.create(config.getUserSyncFilter()));
            }

            SearchResult groupResult = connection.search(config.getGroupSyncBase(), SearchScope.SUB, gsf, MINIMAL_ATTRS);

            if ( groupResult.getEntryCount() == 0 ) {
                r.info("AD_REALM_LDAP_SYNC_NOGROUPS"); //$NON-NLS-1$
            }
            else {
                r.info("AD_REALM_LDAP_SYNC_GROUPS", String.valueOf(groupResult.getEntryCount())); //$NON-NLS-1$
            }

            return true;
        }
        catch ( LDAPException e ) {
            log.debug("Failed to get ldap connection", e); //$NON-NLS-1$
            r.error("AD_REALM_LDAP_FAIL", e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param r
     * @param config
     * @param p
     * @param connection
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private static void checkUserLookup ( ConfigTestResult r, ADAuthenticatorConfig config, UserPasswordAuthTestParams p, LDAPClient connection )
            throws LDAPException, LDAPSearchException {
        if ( !StringUtils.isBlank(p.getUsername()) ) {
            Filter f = Filter.createANDFilter(LDAPSchemaStyle.AD.createUserFilter(), Filter.createEqualityFilter("sAMAccountName", p.getUsername())); //$NON-NLS-1$
            if ( !StringUtils.isBlank(config.getUserSyncFilter()) ) {
                f = Filter.createANDFilter(f, Filter.create(config.getUserSyncFilter()));
            }
            SearchResult res = connection.search(config.getUserSyncBase(), SearchScope.SUB, f);

            if ( res.getEntryCount() == 0 ) {
                r.warn("AD_REALM_LDAP_USER_NOTFOUND", p.getUsername()); //$NON-NLS-1$
            }
            else if ( res.getEntryCount() > 1 ) {
                r.error("AD_REALM_LDAP_USER_MULTIPLE", p.getUsername()); //$NON-NLS-1$
            }
            else {
                SearchResultEntry userRes = res.getSearchEntries().get(0);
                r.info("AD_REALM_LDAP_USER_FOUND", p.getUsername(), userRes.getDN()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param r
     * @param h
     * @param realm
     * @param rlm
     */
    private static boolean checkJoin ( ConfigTestResult r, ConfigTestAsyncHandler h, String realm, ADRealm rlm ) {
        if ( !rlm.isJoined() ) {
            r.error("AD_REALM_UNJOINED", realm); //$NON-NLS-1$
            return false;
        }

        r.info("AD_REALM_JOINED_UNCHECKED", realm, rlm.getLocalHostname(), rlm.getMachineAccount(), rlm.getMachineSid().toString()); //$NON-NLS-1$

        try {
            r.info("AD_REALM_CHECK"); //$NON-NLS-1$
            h.update(r);

            KerberosPrincipal princ = new KerberosPrincipal(String.format("%s@%s", rlm.getMachineAccount(), rlm.getKrbRealm())); //$NON-NLS-1$

            UserPasswordGSSAPIFactory subjFactory = new UserPasswordGSSAPIFactory(princ, rlm.getMachinePasswords(), false, rlm.getSalts(princ));

            subjFactory.getSubject();

            rlm.ensureJoined();

            r.info("AD_REALM_CHECK_OK"); //$NON-NLS-1$
            h.update(r);
            return true;
        }
        catch ( KerberosException e ) {
            log.debug("Exception authenticating using machine credentials", e); //$NON-NLS-1$
            KRBValidationUtils.handleException(r, e);
            return false;
        }
        catch ( ADException e ) {
            log.debug("Exception connecting to domain", e); //$NON-NLS-1$
            if ( e.getCause() instanceof KerberosException ) {
                KRBValidationUtils.handleException(r, (KerberosException) e.getCause());
            }
            else if ( e.getCause() instanceof LDAPException ) {
                handleLDAPException(r, (LDAPException) e.getCause());
            }
            else {
                r.error("AD_REALM_CHECK_FAIL", realm, e.getMessage()); //$NON-NLS-1$
            }
            return false;
        }

    }


    /**
     * @param r
     * @param cause
     */
    private static void handleLDAPException ( ConfigTestResult r, LDAPException e ) {
        if ( e.getCause() instanceof KerberosException ) {
            KRBValidationUtils.handleException(r, (KerberosException) e.getCause());
        }
        else {
            r.error("AD_LDAP_FAIL", e.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param r
     * @param rlm
     * @param p
     * @param secureRandom2
     */
    private static boolean checkNetlogon ( ConfigTestResult r, ConfigTestAsyncHandler h, ADRealm rlm, UserPasswordAuthTestParams p,
            SecureRandom sr ) {
        try ( NetlogonConnection nc = rlm.getNetlogonConnection() ) {
            if ( !nc.check() ) {
                r.error("AD_REALM_NETLOGON_FAIL"); //$NON-NLS-1$
                return false;
            }

            r.info("AD_REALM_NETLOGON_OK"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(p.getUsername()) && !StringUtils.isBlank(p.getPassword()) ) {
                checkNTLM(r, h, nc, rlm, p.getUsername(), p.getPassword(), sr);
            }
            else {
                r.warn("AD_REALM_NTLM_NOUSER"); //$NON-NLS-1$
            }
            return true;
        }
        catch ( ADException e ) {
            log.debug("Failed to get netlogon connection", e); //$NON-NLS-1$
            r.error("AD_REALM_NETLOGON_FAIL", e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param nc
     * @param username
     * @param password
     * @param sr
     */
    private static void checkNTLM ( ConfigTestResult r, ConfigTestAsyncHandler h, NetlogonConnection nc, ADRealm rlm, String username,
            String password, SecureRandom sr ) {

        try {
            byte[] serverChallenge = new byte[8];
            sr.nextBytes(serverChallenge);

            int ntlmsspFlags = NtlmFlags.NTLMSSP_REQUEST_TARGET | NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 | NtlmFlags.NTLMSSP_NEGOTIATE_128
                    | NtlmFlags.NTLMSSP_NEGOTIATE_SIGN | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN | NtlmFlags.NTLMSSP_NEGOTIATE_KEY_EXCH;

            String localHostname = rlm.getLocalNetbiosHostname();
            String userDomain = rlm.getNetbiosDomainName();
            r.info("AD_REALM_NTLM_CHECK", username, userDomain); //$NON-NLS-1$
            h.update(r);

            Type1Message t1 = new Type1Message(rlm.getCIFSContext(), ntlmsspFlags, userDomain, localHostname);
            Type2Message t2 = new Type2Message(rlm.getCIFSContext(), t1, serverChallenge, localHostname);
            Type3Message t3 = new Type3Message(rlm.getCIFSContext(), t2, password, userDomain, username, localHostname, ntlmsspFlags);

            ADUserInfo ui = nc.getNetlogonOperations().ntlmValidate(serverChallenge, t3);
            r.info(
                "AD_REALM_NTLM_SUCCESS", //$NON-NLS-1$
                ui.getAccountName(),
                ui.getDisplayName(),
                ui.getUserSid().toString(),
                ui.getPrimaryGroupSid().toString(),
                ui.getGroupSids().toString());
        }
        catch ( Exception e ) {
            log.debug("NTLM validation failed", e); //$NON-NLS-1$
            r.error("AD_REALM_NTLM_FAIL", e.getMessage()); //$NON-NLS-1$
        }

    }

}
