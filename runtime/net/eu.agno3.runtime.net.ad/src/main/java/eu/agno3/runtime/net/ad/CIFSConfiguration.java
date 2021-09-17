/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.01.2016 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import jcifs.CIFSException;
import jcifs.Configuration;
import jcifs.DialectVersion;
import jcifs.ResolverType;
import jcifs.SmbConstants;
import jcifs.config.BaseConfiguration;
import jcifs.config.DelegatingConfiguration;


/**
 * @author mbechler
 *
 */
public class CIFSConfiguration extends DelegatingConfiguration {

    private static final Logger log = Logger.getLogger(CIFSConfiguration.class);

    /**
     * 
     */
    private static final String DISABLE_EXTENDED_SECURITY = "disableExtendedSecurity"; //$NON-NLS-1$
    private static final String DISABLE_SIGNATURES = "disableSignatures"; //$NON-NLS-1$
    private static final String ENFORCE_SIGNATURES = "enforceSignatures"; //$NON-NLS-1$
    private static final String ENABLE_ENCRYPTION = "enableEncryption"; //$NON-NLS-1$

    private static final String MIN_VERSION = "minProtocol"; //$NON-NLS-1$
    private static final String MAX_VERSION = "maxProtocol"; //$NON-NLS-1$

    private static final String ENABLE_WINS = "enableWins"; //$NON-NLS-1$
    private static final String WINS_SERVER = "winsServer"; //$NON-NLS-1$

    private boolean extendedSecurity = true;
    private boolean signingPreferred = true;
    private boolean signingEnforced = false;
    private boolean encryptionEnabled = false;

    private boolean enableWins = false;
    private String netbiosHostName;
    private DialectVersion minVersion = DialectVersion.SMB1;
    private DialectVersion maxVersion = DialectVersion.SMB210;

    private List<InetAddress> winsServers = Collections.EMPTY_LIST;


    /**
     * @param netbiosHostname
     * @param allowLegacyCrypto
     * @param properties
     * @throws CIFSException
     * 
     */
    public CIFSConfiguration ( String netbiosHostname, boolean allowLegacyCrypto, Map<String, Object> properties ) throws CIFSException {
        super(new BaseConfiguration(true));
        this.netbiosHostName = netbiosHostname;
        parseConfig(properties);
    }


    /**
     * @param netbiosHostname
     * @param allowLegacyCrypto
     * @param properties
     * @param delegate
     */
    public CIFSConfiguration ( String netbiosHostname, boolean allowLegacyCrypto, Map<String, Object> properties, Configuration delegate ) {
        super(delegate);
        this.netbiosHostName = netbiosHostname;
        parseConfig(properties);
    }


    /**
     * @param properties
     */
    private void parseConfig ( Map<String, Object> properties ) {
        if ( properties.containsKey(DISABLE_EXTENDED_SECURITY) ) {
            this.extendedSecurity = Boolean.parseBoolean((String) properties.get(DISABLE_EXTENDED_SECURITY)); // $NON-NLS-1$
        }

        if ( properties.containsKey(DISABLE_SIGNATURES) && Boolean.parseBoolean((String) properties.get(DISABLE_SIGNATURES)) ) {
            this.signingPreferred = false;
        }

        if ( properties.containsKey(ENFORCE_SIGNATURES) && Boolean.parseBoolean((String) properties.get(ENFORCE_SIGNATURES)) ) {
            this.signingEnforced = true;
        }

        if ( properties.containsKey(ENABLE_ENCRYPTION) && Boolean.parseBoolean((String) properties.get(ENABLE_ENCRYPTION)) ) {
            this.encryptionEnabled = true;
        }

        if ( properties.containsKey(ENABLE_WINS) && Boolean.parseBoolean((String) properties.get(ENABLE_WINS)) ) {
            this.enableWins = true;
        }

        if ( properties.containsKey(WINS_SERVER) ) {
            String wservers = (String) properties.get(WINS_SERVER);
            List<InetAddress> wsaddrs = new LinkedList<>();

            for ( String wserver : StringUtils.split(wservers.trim(), ',') ) {
                try {
                    wsaddrs.add(InetAddress.getByName(wserver.trim()));
                }
                catch ( UnknownHostException e ) {
                    log.error("Failed to lookup WINS host " + wserver, e); //$NON-NLS-1$
                }
            }

            this.winsServers = wsaddrs;
        }

        if ( properties.containsKey(MIN_VERSION) ) {
            this.minVersion = DialectVersion.valueOf( ( (String) properties.get(MIN_VERSION) ).trim());
        }

        if ( properties.containsKey(MAX_VERSION) ) {
            this.maxVersion = DialectVersion.valueOf( ( (String) properties.get(MAX_VERSION) ).trim());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getNetbiosHostname()
     */
    @Override
    public String getNetbiosHostname () {
        return this.netbiosHostName;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getResolveOrder()
     */
    @Override
    public List<ResolverType> getResolveOrder () {
        if ( this.enableWins ) {
            return Arrays.asList(ResolverType.RESOLVER_LMHOSTS, ResolverType.RESOLVER_DNS, ResolverType.RESOLVER_WINS, ResolverType.RESOLVER_BCAST);
        }
        return Arrays.asList(ResolverType.RESOLVER_LMHOSTS, ResolverType.RESOLVER_DNS);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getWinsServers()
     */
    @Override
    public InetAddress[] getWinsServers () {
        return this.winsServers.toArray(new InetAddress[this.winsServers.size()]);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getNativeOs()
     */
    @Override
    public String getNativeOs () {
        return "AgNO3 Appliance"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#isDisablePlainTextPasswords()
     */
    @Override
    public boolean isDisablePlainTextPasswords () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#isForceUnicode()
     */
    @Override
    public boolean isForceUnicode () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#isUseUnicode()
     */
    @Override
    public boolean isUseUnicode () {
        return true;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#isSigningEnabled()
     */
    @Override
    public boolean isSigningEnabled () {
        return this.signingPreferred || this.signingEnforced;
    }


    @Override
    public boolean isSigningEnforced () {
        return this.signingEnforced;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#isEncryptionEnabled()
     */
    @Override
    public boolean isEncryptionEnabled () {
        return this.encryptionEnabled;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getMinimumVersion()
     */
    @Override
    public DialectVersion getMinimumVersion () {
        return this.minVersion;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getMaximumVersion()
     */
    @Override
    public DialectVersion getMaximumVersion () {
        return this.maxVersion;
    }


    /**
     * @return whether extended security is enabled
     */
    public boolean isExtendedSecurity () {
        return this.extendedSecurity;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getFlags2()
     */
    @Override
    public int getFlags2 () {
        return SmbConstants.FLAGS2_LONG_FILENAMES | SmbConstants.FLAGS2_EXTENDED_ATTRIBUTES | SmbConstants.FLAGS2_STATUS32
                | ( this.isExtendedSecurity() ? SmbConstants.FLAGS2_EXTENDED_SECURITY_NEGOTIATION : 0 )
                | ( this.isSigningEnforced() ? SmbConstants.FLAGS2_SECURITY_REQUIRE_SIGNATURES : 0 )
                | ( this.isSigningEnabled() ? SmbConstants.FLAGS2_SECURITY_SIGNATURES : 0 )
                | ( this.isUseBatching() ? SmbConstants.FLAGS2_UNICODE : 0 );
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.config.DelegatingConfiguration#getCapabilities()
     */
    @Override
    public int getCapabilities () {
        return SmbConstants.CAP_NT_SMBS | SmbConstants.CAP_STATUS32 | SmbConstants.CAP_LARGE_READX | SmbConstants.CAP_LARGE_WRITEX
                | ( this.isExtendedSecurity() ? SmbConstants.CAP_EXTENDED_SECURITY : 0 );
    }

}
