/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 31, 2017 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;

import eu.agno3.runtime.net.krb5.internal.AbstractKerberosRealmConfigImpl;
import eu.agno3.runtime.util.config.DurationUtil;


/**
 * @author mbechler
 *
 */
public abstract class AbstractADRealmConfigImpl extends AbstractKerberosRealmConfigImpl {

    private static final String SECRET_FILE_PREFIX = "realm.secret"; //$NON-NLS-1$
    private static final String MACHINE_SID_FILE = "realm.machineSid"; //$NON-NLS-1$
    private static final String DOMAIN_SID_FILE = "realm.domainSid"; //$NON-NLS-1$
    private static final String KVNO_FILE = "realm.kvno"; //$NON-NLS-1$
    private static final String HOST_KEYTAB_FILE = "host.keytab"; //$NON-NLS-1$

    private String overrideMachineAccount;
    private String overrideNetbiosHostname;
    private String overrideNetbiosDomainName;
    private String machineBaseDN;

    private boolean rekeyMachineAccount;
    private Duration machineRekeyInterval;
    private boolean allowLegacyCrypto;

    private Path machineSidFile;
    private Path domainSidFile;
    private Path machineKVNOFile;
    private Path hostKeytabFile;
    private Path stateDir;


    /**
     * @param realm
     * @param stateDir
     */
    public AbstractADRealmConfigImpl ( String realm, Path stateDir ) {
        super(realm.toUpperCase(Locale.ROOT));
        this.stateDir = stateDir;
        this.domainSidFile = stateDir.resolve(DOMAIN_SID_FILE);
        this.machineSidFile = stateDir.resolve(MACHINE_SID_FILE);
        this.machineKVNOFile = stateDir.resolve(KVNO_FILE);
        this.hostKeytabFile = stateDir.resolve(HOST_KEYTAB_FILE);
    }


    /**
     * @return the stateDir
     */
    public Path getStateDir () {
        return this.stateDir;
    }


    /**
     * @param file
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Override
    protected void loadProperties ( Map<String, String> props ) {
        super.loadProperties(props);

        String netbiosHostname = props.get("netbiosHostname"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(netbiosHostname) ) {
            this.overrideNetbiosHostname = netbiosHostname.trim();
        }

        String machineAccount = props.get("machineAccountName"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(machineAccount) ) {
            this.overrideMachineAccount = machineAccount.trim();
        }

        String machineBaseDNAttr = props.get("machineBaseDN"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(machineBaseDNAttr) ) {
            this.machineBaseDN = machineBaseDNAttr.trim();
        }

        String netbiosDomainName = props.get("netbiosDomainName"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(netbiosDomainName) ) {
            this.overrideNetbiosDomainName = netbiosDomainName.trim();
        }

        String rekeyMachineAccountAttr = props.get("rekeyMachineAccount"); //$NON-NLS-1$
        this.rekeyMachineAccount = StringUtils.isBlank(rekeyMachineAccountAttr) || Boolean.parseBoolean(rekeyMachineAccountAttr.trim());

        String machineRekeyIntervalAttr = props.get("machineRekeyInterval"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(machineRekeyIntervalAttr) ) {
            this.machineRekeyInterval = DurationUtil.parseDurationCompat(machineRekeyIntervalAttr.trim());
        }
        else {
            this.machineRekeyInterval = Duration.standardDays(30);
        }

        String allowLegacyCryptoStr = props.get("allowLegacyCrypto"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(allowLegacyCryptoStr) ) {
            this.allowLegacyCrypto = Boolean.parseBoolean(allowLegacyCryptoStr);
        }
        else {
            this.allowLegacyCrypto = false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.KerberosRealmConfigImpl#getDomainMappings(java.util.Properties)
     */
    @Override
    protected List<String> getDomainMappings ( Map<String, String> props ) {
        List<String> maps = super.getDomainMappings(props);
        String domain = getRealm().toLowerCase(Locale.ROOT);
        maps.add(domain);
        maps.add("." + domain); //$NON-NLS-1$
        return maps;
    }


    /**
     * @return the machineOU
     */
    public String getMachineBaseDN () {
        return this.machineBaseDN;
    }


    /**
     * @return the rekeyMachineAccount
     */
    public boolean isRekeyMachineAccount () {
        return this.rekeyMachineAccount;
    }


    /**
     * @return the machineRekeyInterval
     */
    public Duration getMachineRekeyInterval () {
        return this.machineRekeyInterval;
    }


    /**
     * @return the override netbios hostname
     */
    public String getOverrideNetbiosHostname () {
        return this.overrideNetbiosHostname;
    }


    /**
     * @return the override machine account
     */
    public String getOverrideMachineAccount () {
        return this.overrideMachineAccount;
    }


    /**
     * @return the override netbios domain name
     */
    public String getOverrideNetbiosDomainName () {
        return this.overrideNetbiosDomainName;
    }


    /**
     * @return whether to allow legacy crypto
     */
    public boolean isAllowLegacyCrypto () {
        return this.allowLegacyCrypto;
    }


    /**
     * 
     * @return the file that holds the machine SID
     */
    public Path getMachineSIDFile () {
        return this.machineSidFile;
    }


    /**
     * @return the domainSidFile
     */
    public Path getDomainSIDFile () {
        return this.domainSidFile;
    }


    /**
     * @return the machine KVNO file
     */
    public Path getMachineKVNOFile () {
        return this.machineKVNOFile;
    }


    /**
     * @param kvno
     * @return the secretFile for the given kvno
     */
    public Path getSecretFile ( int kvno ) {
        return this.stateDir.resolve(String.format("%s.%d", SECRET_FILE_PREFIX, kvno)); //$NON-NLS-1$
    }


    /**
     * @return the hostKeytabFile
     */
    public Path getHostKeytabFile () {
        return this.hostKeytabFile;
    }

}
