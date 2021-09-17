/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.auth.validation.internal;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.auth.UserPasswordAuthTestParams;
import eu.agno3.orchestrator.config.auth.krb5.KerberosAuthenticatorConfig;
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
import eu.agno3.runtime.net.dns.SRVEntries;
import eu.agno3.runtime.net.dns.SRVEntry;
import eu.agno3.runtime.net.dns.SRVUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealmConfig;
import eu.agno3.runtime.net.krb5.KeyTabEntry;
import eu.agno3.runtime.net.krb5.Krb5SubjectUtil;
import eu.agno3.runtime.net.krb5.KrbRealmManager;
import eu.agno3.runtime.net.krb5.UserPasswordGSSAPIFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigTestPlugin.class )
public class KerberosAuthenticatorConfigurationTestPlugin implements ConfigTestPluginAsync<KerberosAuthenticatorConfig> {

    private static final Logger log = Logger.getLogger(KerberosAuthenticatorConfigurationTestPlugin.class);
    private KrbRealmManager realmManager;


    @Reference
    protected synchronized void setADRealmManager ( KrbRealmManager rm ) {
        this.realmManager = rm;
    }


    protected synchronized void unsetADRealmManager ( KrbRealmManager rm ) {
        if ( this.realmManager == rm ) {
            this.realmManager = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<KerberosAuthenticatorConfig> getTargetType () {
        return KerberosAuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getRunOn()
     */
    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return Collections.singleton(ConfigTestPluginRunOn.SERVER);
    }


    @Override
    public ConfigTestResult testAsync ( KerberosAuthenticatorConfig config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {
        log.debug("Running kerberos authenticator test"); //$NON-NLS-1$

        if ( ! ( params instanceof UserPasswordAuthTestParams ) ) {
            return r.state(ConfigTestState.FAILURE);
        }

        UserPasswordAuthTestParams p = (UserPasswordAuthTestParams) params;
        String realm = config.getKerberosRealm();
        if ( !checkKDC(r, realm) ) {
            return r.state(ConfigTestState.FAILURE);
        }

        if ( !checkTicket(r, h, p, realm) ) {
            return r.state(ConfigTestState.FAILURE);
        }
        return r.state(ConfigTestState.SUCCESS);
    }


    /**
     * @param r
     * @param realm
     */
    private boolean checkKDC ( ConfigTestResult r, String realm ) {
        try {
            KerberosRealmConfig rc = this.realmManager.getRealmConfig(realm);
            List<String> kdcs = rc.getKdcs();
            if ( kdcs != null && !kdcs.isEmpty() ) {
                r.info("KRB5_KDC_OVERRIDE", kdcs.toString()); //$NON-NLS-1$
            }
            else {
                r.info("KRB5_KDC_LOOKUP"); //$NON-NLS-1$
                try {
                    SRVEntries lookup = SRVUtil.lookup(realm, "_kerberos._udp"); //$NON-NLS-1$
                    r.info("KRB5_KDC_LOOKUP_OK", dumpEntries(lookup)); //$NON-NLS-1$
                }
                catch ( NamingException e ) {
                    r.error("KRB5_KDC_LOOKUP_FAIL", e.getMessage()); //$NON-NLS-1$
                    return false;
                }
            }
            return true;
        }
        catch ( KerberosException e ) {
            log.debug("Failed to get realm config for " + realm, e); //$NON-NLS-1$
            r.error("KRB5_KDC_REALM_CONFIG_MISSING", realm); //$NON-NLS-1$
            return false;
        }
    }


    private boolean checkTicket ( ConfigTestResult r, ConfigTestAsyncHandler h, UserPasswordAuthTestParams p, String realm ) {
        if ( StringUtils.isBlank(p.getUsername()) && StringUtils.isBlank(p.getPassword()) ) {
            r.warn("KRB5_SKIP_NO_USER"); //$NON-NLS-1$
            return true;
        }

        Subject initSubj;
        UserPasswordGSSAPIFactory initFactory = new UserPasswordGSSAPIFactory(p.getUsername(), p.getPassword(), realm);
        try {
            r.info("KRB5_INIT", initFactory.getPrincipal().toString()); //$NON-NLS-1$
            h.update(r);
            initSubj = initFactory.getSubject();
            r.info("KRB5_INIT_OK", initFactory.getPrincipal().toString()); //$NON-NLS-1$
        }
        catch ( KerberosException e ) {
            log.debug("Failed to get ticket", e); //$NON-NLS-1$
            KRBValidationUtils.handleException(r, e);
            return false;
        }

        try {
            Collection<String> kts = this.realmManager.listKeytabs(realm);
            if ( kts == null || kts.isEmpty() ) {
                r.warn("KRB5_NO_VALIDATION_KEYTAB"); //$NON-NLS-1$
            }
            else {
                validateCreds(r, realm, initSubj, initFactory, kts);
            }
        }
        catch ( KerberosException e ) {
            log.debug("Failed to enumerate keytabs", e); //$NON-NLS-1$
            r.warn("KRB5_NO_VALIDATION_KEYTAB"); //$NON-NLS-1$
        }
        return true;
    }


    /**
     * @param r
     * @param realm
     * @param initSubj
     * @param initFactory
     * @param kts
     * @throws KerberosException
     */
    void validateCreds ( ConfigTestResult r, String realm, Subject initSubj, UserPasswordGSSAPIFactory initFactory, Collection<String> kts )
            throws KerberosException {
        KeyTab validationKeyTab = getValidationKeyTab(r, realm, kts);

        if ( validationKeyTab == null ) {
            r.warn("KRB5_NO_VALIDATION_KEY"); //$NON-NLS-1$
            return;
        }

        Subject acceptorSubject;
        try {
            acceptorSubject = Krb5SubjectUtil.getAcceptorSubject(validationKeyTab, validationKeyTab.getPrincipal());
        }
        catch ( KerberosException e ) {
            log.debug("Failed to get acceptor credentials", e); //$NON-NLS-1$
            return;
        }
        try {
            GSSContext ctx = Krb5SubjectUtil
                    .validateServiceCredentials(initFactory.getPrincipal(), initSubj, validationKeyTab.getPrincipal(), acceptorSubject, 0);
            try {
                r.info("KRB5_VALIDATE_OK", ctx.getSrcName().toString(), ctx.getTargName().toString()); //$NON-NLS-1$
            }
            finally {
                ctx.dispose();
            }
        }
        catch (
            IOException |
            GSSException e ) {
            log.debug("Failed to validate credentials", e); //$NON-NLS-1$
            r.error("KRB5_VALIDATE_FAIL", e.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param r
     * @param realm
     * @param kts
     * @return
     */
    private KeyTab getValidationKeyTab ( ConfigTestResult r, String realm, Collection<String> kts ) {
        for ( String ktId : kts ) {
            try {
                eu.agno3.runtime.net.krb5.KeyTab kt = this.realmManager.getKeytabData(realm, ktId);

                KerberosPrincipal principal = null;
                for ( KeyTabEntry keyTabEntry : kt.getEntries() ) {
                    principal = keyTabEntry.getPrincipal();
                    break;
                }

                if ( principal != null ) {
                    r.info("KRB5_VALIDATION_PRINC", principal.toString()); //$NON-NLS-1$
                    return this.realmManager.getKeytab(realm, ktId, principal);
                }
            }
            catch ( KerberosException e ) {
                log.debug("Failed to get keytab " + ktId, e); //$NON-NLS-1$
            }
        }
        return null;
    }


    /**
     * @param lookup
     * @return
     */
    private static String dumpEntries ( SRVEntries lookup ) {
        List<String> kdcentries = new LinkedList<>();
        for ( SRVEntry srvEntry : lookup.getEntries() ) {
            kdcentries.add(String.format("%s (prio: %d)", srvEntry.getName(), srvEntry.getPriority())); //$NON-NLS-1$
        }
        return kdcentries.toString();
    }

}
