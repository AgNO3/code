/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.net.krb5.ETypesUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.util.config.ConfigUtil;

import sun.security.krb5.Config;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.KrbException;
import sun.security.krb5.internal.crypto.EType;


/**
 * @author mbechler
 *
 */
@Component ( service = KerberosConfig.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "kerberos" )
public class KerberosConfig {

    private static final Logger log = Logger.getLogger(KerberosConfig.class);

    private static final Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private boolean useSystem = false;

    private boolean dnsLookupKDC = true;
    private boolean dnsLookupRealm = true;

    private boolean allowWeakCrypto = false;
    private Set<Integer> permittedEnctypes;
    private Set<Integer> defaultTicketEnctypes;
    private Set<Integer> defaultTGSEnctypes;

    private boolean rdnsCanonicalize = false;
    private boolean disableAddresses = true;

    private boolean defaultTGTRenewable = false;
    private boolean defaultTGTProxiable = false;
    private boolean defaultTGTForwardable = false;

    private int maxClockskewSeconds = 300;
    private long kdcTimeoutMS = 10 * 1000;
    private int maxRetries = 3;
    private int udpPreferenceLimit = 1400;

    private Map<String, AbstractKerberosRealmConfigImpl> realmConfigs = new HashMap<>();
    private File krbConfigFile;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        String configFilePath = ConfigUtil.parseString(ctx.getProperties(), "krbConfPath", null); //$NON-NLS-1$
        this.useSystem = ConfigUtil.parseBoolean(ctx.getProperties(), "useSystem", StringUtils.isBlank(configFilePath)); //$NON-NLS-1$
        if ( this.useSystem ) {
            System.setProperty(
                "java.security.krb5.conf", //$NON-NLS-1$
                "/etc/krb5.conf"); //$NON-NLS-1$
            try {
                Config.refresh();
            }
            catch ( KrbException e ) {
                log.error("Failed to load system kerberos config", e); //$NON-NLS-1$
            }
            return;
        }

        if ( StringUtils.isBlank(configFilePath) ) {
            log.error("No kerberos configuration path configured, not setting up"); //$NON-NLS-1$
            return;
        }

        File configFile = new File(configFilePath);
        if ( ( configFile.exists() && !configFile.canWrite() ) || ( !configFile.exists() && !configFile.getParentFile().canWrite() ) ) {
            log.warn("Cannot write kerberos configuration " + configFile); //$NON-NLS-1$
            return;
        }

        parseConfig(ctx.getProperties());

        this.krbConfigFile = configFile;
        System.setProperty("java.security.krb5.conf", configFile.toString()); //$NON-NLS-1$
        updateKrbConfig();
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        log.debug("Rebuilding kerberos config"); //$NON-NLS-1$
        if ( this.useSystem ) {
            try {
                Config.refresh();
            }
            catch ( KrbException e ) {
                log.error("Failed to reload system kerberos config", e); //$NON-NLS-1$
            }
            return;
        }
        parseConfig(ctx.getProperties());
        for ( Entry<String, AbstractKerberosRealmConfigImpl> rc : this.realmConfigs.entrySet() ) {
            rc.getValue().reload();
        }
        updateKrbConfig();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {

    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {

        this.dnsLookupKDC = ConfigUtil.parseBoolean(cfg, "dnsLookupKDC", true); //$NON-NLS-1$
        this.dnsLookupRealm = ConfigUtil.parseBoolean(cfg, "dnsLookupRealm", true); //$NON-NLS-1$
        this.allowWeakCrypto = ConfigUtil.parseBoolean(cfg, "allowWeakCrypto", false); //$NON-NLS-1$
        this.disableAddresses = ConfigUtil.parseBoolean(cfg, "disableAddresses", true); //$NON-NLS-1$
        this.defaultTGTRenewable = ConfigUtil.parseBoolean(cfg, "defaultTGTRenewable", false); //$NON-NLS-1$
        this.defaultTGTForwardable = ConfigUtil.parseBoolean(cfg, "defaultTGTForwardable", false); //$NON-NLS-1$
        this.defaultTGTProxiable = ConfigUtil.parseBoolean(cfg, "defaultTGTProxiable", false); //$NON-NLS-1$

        this.maxClockskewSeconds = ConfigUtil.parseInt(cfg, "maxClockskewSeconds", 300); //$NON-NLS-1$
        this.kdcTimeoutMS = ConfigUtil.parseInt(cfg, "kdcTimeoutMS", 5 * 1000); //$NON-NLS-1$
        this.maxRetries = ConfigUtil.parseInt(cfg, "maxRetries", 3); //$NON-NLS-1$
        this.udpPreferenceLimit = ConfigUtil.parseInt(cfg, "udpPreferenceLimit", 1400); //$NON-NLS-1$

        this.defaultTGSEnctypes = parseEnctypes(cfg, "defaultTGSEnctypes"); //$NON-NLS-1$
        this.defaultTicketEnctypes = parseEnctypes(cfg, "defaultTicketEnctypes"); //$NON-NLS-1$
        this.permittedEnctypes = parseEnctypes(cfg, "permittedEnctypes"); //$NON-NLS-1$

        if ( this.permittedEnctypes != null ) {
            if ( this.defaultTicketEnctypes == null ) {
                this.defaultTicketEnctypes = this.permittedEnctypes;
            }
            if ( this.defaultTGSEnctypes == null ) {
                this.defaultTGSEnctypes = this.permittedEnctypes;
            }
        }
    }


    /**
     * @param cfg
     * @param string
     * @return
     */
    private static Set<Integer> parseEnctypes ( Dictionary<String, Object> cfg, String prop ) {
        Set<String> etypes = ConfigUtil.parseStringSet(cfg, prop, null);
        if ( etypes == null || etypes.isEmpty() ) {
            return null;
        }

        Set<Integer> etypeVals = new HashSet<>();
        for ( String etype : etypes ) {
            Integer etypeVal = ETypesUtil.eTypeFromMITString(etype.trim());
            if ( etypeVal != null ) {
                etypeVals.add(etypeVal);
            }
        }
        return etypeVals;
    }


    /**
     * @param config
     */
    public synchronized void ensureConfigured ( AbstractKerberosRealmConfigImpl config ) {
        String rlm = config.getRealm();

        if ( StringUtils.isEmpty(rlm) || this.useSystem ) {
            return;
        }

        AbstractKerberosRealmConfigImpl existing = this.realmConfigs.get(rlm);
        if ( existing != null && config.equals(existing) && !existing.isModified() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Realm already configured " + config.getRealm()); //$NON-NLS-1$
            }
            return;
        }

        config.reload();
        this.realmConfigs.put(rlm, config);

        if ( log.isDebugEnabled() ) {
            log.debug("Configuring realm " + config.getRealm()); //$NON-NLS-1$
        }

        updateKrbConfig();
    }


    /**
     * 
     */
    private void updateKrbConfig () {
        if ( this.krbConfigFile == null || this.useSystem ) {
            return;
        }
        try {
            Path temp = Files.createTempFile(
                "krbconfig", //$NON-NLS-1$
                ".conf", //$NON-NLS-1$
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))); //$NON-NLS-1$

            try ( OutputStream os = Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                  Writer w = new OutputStreamWriter(os, CHARSET) ) {
                this.write(w);
            }

            Files.move(temp, this.krbConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Config.refresh();
        }
        catch (
            IOException |
            KerberosException |
            KrbException e ) {
            log.error("Failed to update kerberos configuration", e); //$NON-NLS-1$
        }
    }


    /**
     * @param etype
     * @return whether the encryption type is permitted
     */
    public boolean isPermittedEtype ( int etype ) {
        if ( this.permittedEnctypes != null ) {
            return this.permittedEnctypes.contains(etype);
        }
        else if ( this.allowWeakCrypto ) {
            return ETypesUtil.DEFAULT_WEAK_ETYPES.contains(etype);
        }

        return ETypesUtil.DEFAULT_ETYPES.contains(etype);
    }


    /**
     * @return the permitted etypes
     */
    public Collection<Integer> getPermittedETypes () {
        if ( this.permittedEnctypes != null ) {
            return Collections.unmodifiableCollection(this.permittedEnctypes);
        }
        else if ( this.allowWeakCrypto ) {
            return ETypesUtil.DEFAULT_WEAK_ETYPES;
        }

        return ETypesUtil.DEFAULT_ETYPES;
    }


    /**
     * @param etype
     * @return whether the encryption type is permitted and supported
     */
    public boolean isSupported ( int etype ) {
        return this.isPermittedEtype(etype) && EType.isSupported(etype);
    }


    /**
     * 
     * @param w
     * @throws IOException
     * @throws KerberosException
     */
    public void write ( Writer w ) throws IOException, KerberosException {
        writeDefaults(w);
        writeETypes(w);

        writeLine(w, "[realms]"); //$NON-NLS-1$
        for ( AbstractKerberosRealmConfigImpl realmConfig : this.realmConfigs.values() ) {
            realmConfig.writeRealmSection(w);
        }

        writeLine(w, "[domain_realm]"); //$NON-NLS-1$
        for ( AbstractKerberosRealmConfigImpl realmConfig : this.realmConfigs.values() ) {
            realmConfig.writeDomainMapSection(w);
        }

        writeLine(w, "[capaths]"); //$NON-NLS-1$
        for ( AbstractKerberosRealmConfigImpl realmConfig : this.realmConfigs.values() ) {
            realmConfig.writeCAPathSection(w);
        }
    }


    /**
     * @param w
     * @throws IOException
     * @throws KerberosException
     */
    private void writeETypes ( Writer w ) throws IOException, KerberosException {
        Set<Integer> tgsEnctypes = new HashSet<>();
        Set<Integer> ticketEnctypes = new HashSet<>();
        Set<Integer> permitted = new HashSet<>();

        for ( AbstractKerberosRealmConfigImpl realmConfig : this.realmConfigs.values() ) {
            if ( realmConfig.getDefaultTGSEnctypes() != null ) {
                tgsEnctypes.addAll(realmConfig.getDefaultTGSEnctypes());
            }
            if ( realmConfig.getDefaultTicketEnctypes() != null ) {
                ticketEnctypes.addAll(realmConfig.getDefaultTicketEnctypes());
            }
            if ( realmConfig.getPermittedEnctypes() != null ) {
                permitted.addAll(realmConfig.getPermittedEnctypes());
            }
        }

        if ( ticketEnctypes.isEmpty() ) {
            if ( this.defaultTicketEnctypes != null ) {
                ticketEnctypes.addAll(this.defaultTicketEnctypes);
            }
            else {
                ticketEnctypes.addAll(this.allowWeakCrypto ? ETypesUtil.DEFAULT_WEAK_ETYPES : ETypesUtil.DEFAULT_ETYPES);
            }
        }

        if ( tgsEnctypes.isEmpty() ) {
            if ( this.defaultTGSEnctypes != null ) {
                tgsEnctypes.addAll(this.defaultTGSEnctypes);
            }
            else {
                tgsEnctypes.addAll(this.allowWeakCrypto ? ETypesUtil.DEFAULT_WEAK_ETYPES : ETypesUtil.DEFAULT_ETYPES);
            }
        }

        if ( permitted.isEmpty() ) {
            if ( this.permittedEnctypes != null ) {
                permitted.addAll(this.permittedEnctypes);
            }
            else {
                permitted.addAll(this.allowWeakCrypto ? ETypesUtil.DEFAULT_WEAK_ETYPES : ETypesUtil.DEFAULT_ETYPES);
            }
        }

        writeETypes(w, containsWeak(permitted), tgsEnctypes, ticketEnctypes, permitted);
    }


    /**
     * @param etypes
     * @return
     */
    private static boolean containsWeak ( Set<Integer> etypes ) {
        return etypes.contains(EncryptedData.ETYPE_DES_CBC_MD5) || etypes.contains(EncryptedData.ETYPE_DES_CBC_MD4)
                || etypes.contains(EncryptedData.ETYPE_DES_CBC_MD5);
    }


    /**
     * @param tgtEnctypes
     * @param ticketEnctypes
     * @param permitted
     * @throws IOException
     * @throws KerberosException
     */
    private void writeETypes ( Writer w, boolean allowWeak, Set<Integer> tgtEnctypes, Set<Integer> ticketEnctypes, Set<Integer> permitted )
            throws IOException, KerberosException {

        if ( log.isDebugEnabled() ) {
            log.debug("Allow weak " + allowWeak); //$NON-NLS-1$
            log.debug("TGS enctypes " + ETypesUtil.makeETypesString(tgtEnctypes)); //$NON-NLS-1$
            log.debug("Ticket enctypes " + ETypesUtil.makeETypesString(ticketEnctypes)); //$NON-NLS-1$
            log.debug("Permitted enctypes " + ETypesUtil.makeETypesString(permitted)); //$NON-NLS-1$
        }

        writeLine(w, "  allow_weak_crypto = " + this.allowWeakCrypto); //$NON-NLS-1$
        writeLine(w, "  default_tkt_enctypes = " + ETypesUtil.makeETypesString(ticketEnctypes)); //$NON-NLS-1$
        writeLine(w, "  default_tgs_enctypes = " + ETypesUtil.makeETypesString(tgtEnctypes)); //$NON-NLS-1$
        writeLine(w, "  permitted_enctypes = " + ETypesUtil.makeETypesString(permitted)); //$NON-NLS-1$
    }


    /**
     * @param w
     * @throws IOException
     * @throws KerberosException
     */
    protected void writeDefaults ( Writer w ) throws IOException, KerberosException {
        writeLine(w, "[libdefaults]"); //$NON-NLS-1$
        writeLine(w, "  dns_lookup_kdc = " + this.dnsLookupKDC); //$NON-NLS-1$
        writeLine(w, "  dns_lookup_realm = " + this.dnsLookupRealm); //$NON-NLS-1$
        writeLine(w, "  noaddresses = " + this.disableAddresses); //$NON-NLS-1$
        writeLine(w, "  renewable = " + this.defaultTGTRenewable); //$NON-NLS-1$
        writeLine(w, "  proxiable = " + this.defaultTGTProxiable); //$NON-NLS-1$
        writeLine(w, "  forwardable = " + this.defaultTGTForwardable); //$NON-NLS-1$
        writeLine(w, "  clockskew = " + this.maxClockskewSeconds); //$NON-NLS-1$
        writeLine(w, "  kdc_timeout = " + this.kdcTimeoutMS); //$NON-NLS-1$
        writeLine(w, "  max_retries = " + this.maxRetries); //$NON-NLS-1$
        writeLine(w, "  udp_preference_limit = " + this.udpPreferenceLimit); //$NON-NLS-1$
        writeLine(w, "  rdns = " + this.rdnsCanonicalize); //$NON-NLS-1$
    }


    private static final void writeLine ( Writer w, String data ) throws IOException {
        w.write(data);
        w.write("\n"); //$NON-NLS-1$
    }

}
