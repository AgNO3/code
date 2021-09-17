/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.realms.ADRealmManager;
import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil;
import eu.agno3.orchestrator.agent.realms.units.EnsureConfiguredRealm;
import eu.agno3.orchestrator.agent.realms.units.EnsureKeytabAccess;
import eu.agno3.orchestrator.agent.realms.units.EnsureKeytabExists;
import eu.agno3.orchestrator.agent.realms.units.ImportKey;
import eu.agno3.orchestrator.agent.realms.units.JoinAD;
import eu.agno3.orchestrator.agent.realms.units.LeaveAD;
import eu.agno3.orchestrator.agent.realms.units.RekeyAD;
import eu.agno3.orchestrator.agent.realms.units.RemoveRealm;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.HostIdentification;
import eu.agno3.orchestrator.config.hostconfig.agent.api.RealmConfigUtil;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.CredentialChallenge;
import eu.agno3.orchestrator.config.realms.ADRealmConfig;
import eu.agno3.orchestrator.config.realms.CAPathEntry;
import eu.agno3.orchestrator.config.realms.KRBRealmConfig;
import eu.agno3.orchestrator.config.realms.KerberosSecurityLevel;
import eu.agno3.orchestrator.config.realms.KeytabEntry;
import eu.agno3.orchestrator.config.realms.RealmConfig;
import eu.agno3.orchestrator.config.realms.RealmsConfig;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RealmConfigUtil.class, RealmConfigJobBuilder.class
} )
public class RealmConfigJobBuilder implements RealmConfigUtil {

    /**
     * 
     */
    private static final String ETC_KRB5_CONF = "/etc/krb5.conf"; //$NON-NLS-1$
    private static final String[] DEFAULT_ALLOW_USERS = new String[] {
        "orchserver" //$NON-NLS-1$
    };
    private KerberosConfigUtil kerberosConfigUtil;


    @Reference
    protected synchronized void setKerberosConfigUtil ( KerberosConfigUtil rm ) {
        this.kerberosConfigUtil = rm;
    }


    protected synchronized void unsetKerberosConfigUtil ( KerberosConfigUtil rm ) {
        if ( this.kerberosConfigUtil == rm ) {
            this.kerberosConfigUtil = null;
        }
    }


    /**
     * @param b
     * @param ctx
     * @throws UnitInitializationFailedException
     * @throws IOException
     */
    public void build ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws UnitInitializationFailedException, IOException {

        RealmsConfig old = null;
        RealmsConfig cur = ctx.cfg().getRealmsConfiguration();
        if ( ctx.cur().isPresent() ) {
            old = ctx.cur().get().getRealmsConfiguration();
            buildAD(b, ctx, cur.getRealms(), old.getRealms());
            buildKRB(b, ctx, cur.getRealms(), old.getRealms());
        }
        else {
            buildAD(b, ctx, cur.getRealms(), null);
            buildKRB(b, ctx, cur.getRealms(), null);
        }

        b.add(Contents.class).file(ETC_KRB5_CONF).content(ctx.tpl(ETC_KRB5_CONF)).perms(FileSecurityUtils.getWorldReadableFilePermissions());
    }


    /**
     * @param b
     * @param ctx
     * @param krbRealms
     * @param old
     * @throws UnitInitializationFailedException
     */
    private static void buildKRB ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            Set<RealmConfig> krbRealms, Set<RealmConfig> old ) throws UnitInitializationFailedException {

        Set<String> removed = new HashSet<>(getKRBRealmNames(old));
        Map<String, KRBRealmConfig> oldRealms = makeKRBRealmMap(old);
        removed.removeAll(getKRBRealmNames(krbRealms));
        for ( String remove : removed ) {
            b.add(RemoveRealm.class).realm(remove).type(RealmType.UNSPECIFIED);
        }

        for ( RealmConfig krbRealm : krbRealms ) {
            if ( ! ( krbRealm instanceof KRBRealmConfig ) ) {
                continue;
            }
            buildKRBRealm(b, ctx, (KRBRealmConfig) krbRealm, oldRealms.get(krbRealm.getRealmName()));
        }

    }


    /**
     * @param configs
     * @return
     */
    private static Map<String, KRBRealmConfig> makeKRBRealmMap ( Set<RealmConfig> configs ) {
        if ( configs == null || configs.isEmpty() ) {
            return Collections.EMPTY_MAP;
        }

        Map<String, KRBRealmConfig> realms = new HashMap<>();
        for ( RealmConfig cfg : configs ) {
            if ( ! ( cfg instanceof KRBRealmConfig ) ) {
                continue;
            }
            realms.put(cfg.getRealmName(), (KRBRealmConfig) cfg);
        }
        return realms;
    }


    /**
     * @param b
     * @param ctx
     * @param krbRealm
     * @throws UnitInitializationFailedException
     */
    private static void buildKRBRealm ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            KRBRealmConfig krbRealm, KRBRealmConfig oldKrbRealm ) throws UnitInitializationFailedException {

        b.add(EnsureConfiguredRealm.class).realm(krbRealm.getRealmName()).type(mapRealmType(krbRealm.getRealmType()))
                .config(makeRealmProperties(ctx.cfg().getHostIdentification(), krbRealm))
                .allowUsers(FileSecurityUtils.isRunningAsRoot() ? DEFAULT_ALLOW_USERS : new String[0]);

        buildKeytabs(
            b,
            ctx,
            krbRealm.getRealmName(),
            mapRealmType(krbRealm.getRealmType()),
            krbRealm.getImportKeytabs(),
            oldKrbRealm != null ? oldKrbRealm.getImportKeytabs() : Collections.EMPTY_SET);

    }


    /**
     * @param realmName
     * @param type
     * @param entries
     * @param oldEntries
     * @throws UnitInitializationFailedException
     */
    private static void buildKeytabs ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            String realmName, RealmType type, Set<KeytabEntry> entries, Set<KeytabEntry> oldEntries ) throws UnitInitializationFailedException {
        for ( KeytabEntry e : entries ) {
            b.add(EnsureKeytabExists.class).realm(realmName).type(type).keytab(e.getKeytabId());
            if ( !e.getKeyImportEntries().isEmpty() ) {
                b.add(ImportKey.class).realm(realmName).type(type).keytab(e.getKeytabId()).keys(e.getKeyImportEntries());
            }
        }
    }


    /**
     * @param krbRealm
     * @return
     */
    private static Properties makeRealmProperties ( HostIdentification hid, KRBRealmConfig krbRealm ) {
        PropertyConfigBuilder p = PropertyConfigBuilder.get();
        p.p("realm", krbRealm.getRealmName()); //$NON-NLS-1$
        p.p("realmType", krbRealm.getRealmType().name()); //$NON-NLS-1$
        p.p("securityLevel", krbRealm.getSecurityLevel().name()); //$NON-NLS-1$
        p.p("adminServer", krbRealm.getAdminServer()); //$NON-NLS-1$
        p.p("kpasswdServer", krbRealm.getKpasswdServer()); //$NON-NLS-1$

        if ( !StringUtils.isBlank(krbRealm.getOverrideLocalHostname()) ) {
            p.p("localHostname", krbRealm.getOverrideLocalHostname()); //$NON-NLS-1$
        }
        else {
            p.p("localHostname", makeDefaultHostName(krbRealm.getRealmName(), hid)); //$NON-NLS-1$
        }

        p.p("kdcs", krbRealm.getKdcs()); //$NON-NLS-1$
        p.p("mapDomain", krbRealm.getDomainMappings()); //$NON-NLS-1$
        p.p("maximumTicketLifetime", krbRealm.getMaximumTicketLifetime()); //$NON-NLS-1$
        p.p("rekeyServices", krbRealm.getRekeyServices()); //$NON-NLS-1$

        p.p(
            "allowLegacyCrypto", //$NON-NLS-1$
            krbRealm.getSecurityLevel() == KerberosSecurityLevel.LEGACY || krbRealm.getSecurityLevel() == KerberosSecurityLevel.WEAK);

        p.p(
            "allowWeakCrypto", //$NON-NLS-1$
            krbRealm.getSecurityLevel() == KerberosSecurityLevel.WEAK);

        if ( krbRealm.getRekeyServices() ) {
            p.p("serviceRekeyInterval", krbRealm.getServiceRekeyInterval()); //$NON-NLS-1$
        }

        if ( krbRealm.getCaPaths() != null && !krbRealm.getCaPaths().isEmpty() ) {
            p.p("caPaths", makeCAPaths(krbRealm.getCaPaths())); //$NON-NLS-1$
        }

        return p.build();
    }


    /**
     * @param b
     * @param ctx
     * @param adRealms
     * @param old
     * @throws UnitInitializationFailedException
     */
    private static void buildAD ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            Set<RealmConfig> adRealms, Set<RealmConfig> old ) throws UnitInitializationFailedException {

        Set<String> removed = new HashSet<>(getADRealmNames(old));
        Map<String, ADRealmConfig> oldRealms = makeADRealmMap(old);
        removed.removeAll(getADRealmNames(adRealms));
        for ( String remove : removed ) {
            b.add(RemoveRealm.class).realm(remove).type(RealmType.AD);
        }

        for ( RealmConfig adRealm : adRealms ) {
            if ( ! ( adRealm instanceof ADRealmConfig ) ) {
                continue;
            }
            buildADRealm(b, ctx, (ADRealmConfig) adRealm, oldRealms.get(adRealm.getRealmName()));
        }
    }


    /**
     * @param configs
     * @return
     */
    private static Map<String, ADRealmConfig> makeADRealmMap ( Set<RealmConfig> configs ) {
        if ( configs == null || configs.isEmpty() ) {
            return Collections.EMPTY_MAP;
        }

        Map<String, ADRealmConfig> realms = new HashMap<>();
        for ( RealmConfig cfg : configs ) {
            if ( ! ( cfg instanceof ADRealmConfig ) ) {
                continue;
            }
            realms.put(cfg.getRealmName(), (ADRealmConfig) cfg);
        }
        return realms;
    }


    /**
     * @param b
     * @param ctx
     * @param adRealm
     * @param old
     * @throws UnitInitializationFailedException
     */
    private static void buildADRealm ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            ADRealmConfig adRealm, ADRealmConfig old ) throws UnitInitializationFailedException {

        b.add(EnsureConfiguredRealm.class).realm(adRealm.getRealmName()).type(RealmType.AD)
                .config(makeRealmProperties(ctx.cfg().getHostIdentification(), adRealm))
                .allowUsers(FileSecurityUtils.isRunningAsRoot() ? DEFAULT_ALLOW_USERS : new String[0]);

        buildKeytabs(
            b,
            ctx,
            adRealm.getRealmName(),
            RealmType.AD,
            adRealm.getImportKeytabs(),
            old != null ? old.getImportKeytabs() : Collections.EMPTY_SET);

        if ( adRealm.getDoJoin() ) {
            doADJoin(b, ctx, adRealm);
        }
        else if ( adRealm.getDoLeave() ) {
            doAdLeave(b, ctx, adRealm);
        }
        else if ( adRealm.getDoRekey() ) {
            doAdRekey(b, ctx, adRealm);
        }

        if ( adRealm.getUpdateDns() && !adRealm.getDoJoin() && !adRealm.getDoLeave() ) {
            // try trigger DNS update
            b.add(Exec.class).cmd("/usr/share/agno3-base/scripts/ad-dns-update.sh").args(adRealm.getRealmName()).ignoreExitCode(); //$NON-NLS-1$
        }
    }


    /**
     * @param b
     * @param ctx
     * @param adRealm
     * @throws UnitInitializationFailedException
     */
    private static void doAdRekey ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            ADRealmConfig adRealm ) throws UnitInitializationFailedException {
        b.add(RekeyAD.class).realm(adRealm.getRealmName()).type(RealmType.AD);
    }


    /**
     * @param b
     * @param ctx
     * @param adRealm
     * @throws JobBuilderException
     */
    private static void doAdLeave ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            ADRealmConfig adRealm ) throws UnitInitializationFailedException {
        CredentialChallenge creds = getChallengeResponse(ctx, CredentialChallenge.class, makeRealmAdminCredentialKey(adRealm));
        WrappedCredentials wrapped = creds.getWrapped();
        if ( wrapped != null ) {
            b.add(LeaveAD.class).realm(adRealm.getRealmName()).type(RealmType.AD).creds(wrapped);
        }
        else {
            b.add(LeaveAD.class).realm(adRealm.getRealmName()).type(RealmType.AD).user(creds.getUsername()).password(creds.getPassword());
        }
    }


    /**
     * @param b
     * @param ctx
     * @param adRealm
     * @param challengeResponses
     * @throws UnitInitializationFailedException
     */
    private static void doADJoin ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            ADRealmConfig adRealm ) throws UnitInitializationFailedException {
        String machinePassword;
        switch ( adRealm.getJoinType() ) {
        case ADMIN:
            CredentialChallenge creds = getChallengeResponse(ctx, CredentialChallenge.class, makeRealmAdminCredentialKey(adRealm));
            WrappedCredentials wrapped = creds.getWrapped();
            if ( wrapped != null ) {
                b.add(JoinAD.class).realm(adRealm.getRealmName()).type(RealmType.AD).creds(wrapped);
            }
            else {
                b.add(JoinAD.class).realm(adRealm.getRealmName()).type(RealmType.AD).user(creds.getUsername()).password(creds.getPassword());
            }
            break;
        case JOIN_ACCOUNT:
            b.add(JoinAD.class).realm(adRealm.getRealmName()).type(RealmType.AD).user(adRealm.getJoinUser()).password(adRealm.getJoinPassword());
            break;
        case CUSTOM_MACHINE_PASSWORD:
            machinePassword = adRealm.getCustomMachineJoinPassword();
        case RESET_MACHINE_PASSWORD:
            machinePassword = null;
            b.add(JoinAD.class).realm(adRealm.getRealmName()).type(RealmType.AD).usingMachinePassword(machinePassword);
            break;
        default:
            throw new UnitInitializationFailedException("Unknown join type " + adRealm.getJoinType()); //$NON-NLS-1$

        }
    }


    /**
     * @param adRealm
     * @return
     */
    private static String makeRealmAdminCredentialKey ( ADRealmConfig adRealm ) {
        return adRealm.getId() + "-domainAdminPass"; //$NON-NLS-1$
    }


    /**
     * @param ctx
     * @param chlgKey
     * @return
     * @throws JobBuilderException
     */
    private static <T extends ConfigApplyChallenge> T getChallengeResponse (
            ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, Class<T> type, String chlgKey )
                    throws UnitInitializationFailedException {
        for ( ConfigApplyChallenge chlgResp : ctx.job().getApplyInfo().getChallengeResponses() ) {
            if ( chlgKey.equals(chlgResp.getKey()) && type.isAssignableFrom(chlgResp.getClass()) ) {
                return type.cast(chlgResp);
            }
        }
        throw new UnitInitializationFailedException("Requires administrative user credentials, but not present"); //$NON-NLS-1$
    }


    /**
     * @param realmName
     * @param hid
     * @return
     */
    private static String makeDefaultHostName ( String realmName, HostIdentification hid ) {
        String hostname = hid.getHostName().toLowerCase(Locale.ROOT);
        String domainname = hid.getDomainName().toLowerCase(Locale.ROOT);
        return String.format("%s.%s", hostname, domainname); //$NON-NLS-1$
    }


    /**
     * @param adRealm
     * @return
     */
    private static Properties makeRealmProperties ( HostIdentification hid, ADRealmConfig adRealm ) {
        PropertyConfigBuilder p = PropertyConfigBuilder.get();
        p.p("realm", adRealm.getRealmName()); //$NON-NLS-1$

        String fqdn;
        if ( !StringUtils.isBlank(adRealm.getOverrideLocalHostname()) ) {
            fqdn = adRealm.getOverrideLocalHostname();
        }
        else {
            fqdn = makeDefaultHostName(adRealm.getRealmName(), hid);
        }
        p.p("localHostname", fqdn); //$NON-NLS-1$

        p.p("securityLevel", adRealm.getSecurityLevel().name()); //$NON-NLS-1$
        p.p("mapDomain", adRealm.getDomainMappings()); //$NON-NLS-1$
        p.p("caPaths", makeCAPaths(adRealm.getCaPaths())); //$NON-NLS-1$
        p.p("machineBaseDN", adRealm.getMachineBaseDN()); //$NON-NLS-1$

        String hostname;
        if ( !StringUtils.isBlank(adRealm.getOverrideNetbiosHostname()) ) {
            hostname = adRealm.getOverrideNetbiosHostname();
        }
        else {
            int sep = fqdn.indexOf('.');
            if ( sep < 0 ) {
                hostname = fqdn;
            }
            else {
                hostname = fqdn.substring(0, sep);
            }
        }
        p.p("overrideNetbiosHostname", hostname); //$NON-NLS-1$

        String machineAccount;
        if ( !StringUtils.isBlank(adRealm.getOverrideMachineAccount()) ) {
            machineAccount = adRealm.getOverrideMachineAccount();

        }
        else {
            machineAccount = hostname + '$';
        }
        p.p("overrideMachineAccount", machineAccount); //$NON-NLS-1$

        p.p("overrideNetbiosDomainName", adRealm.getOverrideNetbiosDomainName()); //$NON-NLS-1$
        p.p("maximumTicketLifetime", adRealm.getMaximumTicketLifetime()); //$NON-NLS-1$
        p.p("rekeyServices", adRealm.getRekeyServices()); //$NON-NLS-1$
        if ( adRealm.getRekeyServices() ) {
            p.p("serviceRekeyInterval", adRealm.getServiceRekeyInterval()); //$NON-NLS-1$
        }
        p.p("rekeyMachineAccount", adRealm.getRekeyMachineAccount()); //$NON-NLS-1$
        if ( adRealm.getRekeyMachineAccount() ) {
            p.p("machineRekeyInterval", adRealm.getMachineRekeyInterval()); //$NON-NLS-1$
        }

        p.p("updateDNS", adRealm.getUpdateDns()); //$NON-NLS-1$
        p.p("updateDNSForceSecure", adRealm.getUpdateDnsForceSecure()); //$NON-NLS-1$
        p.p("updateDNSTTL", adRealm.getUpdateDnsTtl()); //$NON-NLS-1$
        p.p("updateDNSFromInterface", adRealm.getUpdateDnsFromInterface()); //$NON-NLS-1$

        p.p("allowSMB1", adRealm.getAllowSMB1()); //$NON-NLS-1$
        p.p("disableSMB2", adRealm.getDisableSMB2()); //$NON-NLS-1$
        return p.build();
    }


    /**
     * @param caPaths
     * @return
     */
    private static String makeCAPaths ( Set<CAPathEntry> caPaths ) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for ( CAPathEntry e : caPaths ) {
            if ( first ) {
                first = false;
            }
            else {
                sb.append(',');
            }
            sb.append(e.getTargetRealm());
            sb.append('=');
            sb.append(e.getNextRealm());
        }

        return sb.toString();
    }


    /**
     * @param realmType
     * @return
     */
    private static RealmType mapRealmType ( eu.agno3.orchestrator.realms.RealmType realmType ) {
        return RealmType.valueOf(realmType.name());
    }


    private static Set<String> getKRBRealmNames ( Set<RealmConfig> realms ) throws UnitInitializationFailedException {
        Set<String> names = new HashSet<>();
        if ( realms == null ) {
            return names;
        }

        for ( RealmConfig realm : realms ) {
            if ( ! ( realm instanceof KRBRealmConfig ) ) {
                continue;
            }
            if ( !names.add(realm.getRealmName()) ) {
                throw new UnitInitializationFailedException("Duplicate KRB realm name " + realm); //$NON-NLS-1$
            }
        }
        return names;
    }


    private static Set<String> getADRealmNames ( Set<RealmConfig> realms ) throws UnitInitializationFailedException {
        Set<String> names = new HashSet<>();
        if ( realms == null ) {
            return names;
        }

        for ( RealmConfig realm : realms ) {
            if ( ! ( realm instanceof ADRealmConfig ) ) {
                continue;
            }
            if ( !names.add(realm.getRealmName()) ) {
                throw new UnitInitializationFailedException("Duplicate AD realm name " + realm); //$NON-NLS-1$
            }
        }
        return names;
    }


    /**
     * @param realmConfig
     * @param realmName
     * @return thte realm config
     * @throws JobBuilderException
     */
    @Override
    public RealmConfig findRealm ( RealmsConfig realmConfig, String realmName ) throws JobBuilderException {

        RealmConfig found = null;
        for ( RealmConfig cfg : realmConfig.getRealms() ) {
            if ( realmName.equals(cfg.getRealmName()) ) {
                found = cfg;
                break;
            }
        }

        if ( found == null ) {
            throw new JobBuilderException("Target realm is not configured " + realmName); //$NON-NLS-1$
        }
        return found;
    }


    /**
     * @param b
     * @param ctx
     * @param realmConfig
     * @param princ
     * @param realmName
     * @param keytab
     * @return the path to the keytab
     * @throws JobBuilderException
     * @throws ADException
     * @throws UnitInitializationFailedException
     * @throws KerberosException
     */
    @Override
    public Path ensureKeytab ( JobBuilder b, ConfigurationJobContext<@NonNull ?, ?> ctx, RealmsConfig realmConfig, String princ, String realmName,
            String keytab ) throws JobBuilderException, ADException, UnitInitializationFailedException, KerberosException {

        RealmConfig found = this.findRealm(realmConfig, realmName);
        eu.agno3.orchestrator.realms.RealmType realmType = found.getRealmType();

        Path keytabFile = null;
        if ( StringUtils.isBlank(keytab) ) {
            if ( realmType != eu.agno3.orchestrator.realms.RealmType.AD ) {
                throw new JobBuilderException("You have to specify a keytab"); //$NON-NLS-1$
            }

            // for AD, an empty keytab means to use the host credentials
            // check domain is joined
            ADRealmManager rlm = (ADRealmManager) this.kerberosConfigUtil.getRealmManager(realmName, eu.agno3.runtime.net.krb5.RealmType.AD);
            this.kerberosConfigUtil.checkJoin(rlm);
            keytabFile = rlm.getHostKeytab();
            b.add(EnsureKeytabAccess.class).type(rlm.getType()).realm(realmName).user(princ);
        }
        else {
            // check the keytab is either configured for import or already exists
            KeytabEntry foundKeytab = null;
            RealmManager rlm = this.kerberosConfigUtil.getRealmManager(realmName);
            for ( KeytabEntry keytabEntry : found.getImportKeytabs() ) {
                if ( keytab.equals(keytabEntry.getKeytabId()) ) {
                    foundKeytab = keytabEntry;
                    keytabFile = rlm.getKeytabManager(keytab).getPath();
                    break;
                }
            }

            if ( foundKeytab == null ) {
                // check keytab does exist
                if ( !rlm.exists() ) {
                    throw new JobBuilderException("Realm does not exist " + realmName); //$NON-NLS-1$
                }

                KeyTabManager keytabManager = rlm.getKeytabManager(keytab);
                if ( !keytabManager.exists() ) {
                    throw new JobBuilderException("Keytab does not exist " + keytab); //$NON-NLS-1$
                }
                keytabFile = keytabManager.getPath();
            }
            b.add(EnsureKeytabAccess.class).type(rlm.getType()).realm(realmName).keytab(keytab).user(princ);
        }

        if ( keytabFile == null ) {
            throw new JobBuilderException("Failed to locate keytab " + keytab); //$NON-NLS-1$
        }
        return keytabFile;
    }

}
