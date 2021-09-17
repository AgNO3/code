/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.runtime.crypto.tls.TLSConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = TLSConfiguration.class, configurationPid = TLSConfiguration.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TLSConfigurationImpl implements TLSConfiguration {

    private static final Logger log = Logger.getLogger(TLSConfigurationImpl.class);

    /**
     * 
     */
    private static final String[] DEFAULT_PROTOCOLS = new String[] {
        "TLSv1.2", //$NON-NLS-1$
        "TLSv1.1" //$NON-NLS-1$
    };
    /**
     * 
     */
    private static final String[] DEFAULT_CIPHERS = new String[] {
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", //$NON-NLS-1$
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", //$NON-NLS-1$
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", //$NON-NLS-1$
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", //$NON-NLS-1$
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", //$NON-NLS-1$
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", //$NON-NLS-1$
        "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", //$NON-NLS-1$
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", //$NON-NLS-1$
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", //$NON-NLS-1$
        "TLS_DHE_ECDSA_WITH_AES_128_GCM_SHA256", //$NON-NLS-1$
        "TLS_DHE_ECDSA_WITH_AES_128_CBC_SHA256", //$NON-NLS-1$
        "TLS_DHE_ECDSA_WITH_AES_128_CBC_SHA", //$NON-NLS-1$
        //
        "TLS_EMPTY_RENEGOTIATION_INFO_SCSV" //$NON-NLS-1$
    };

    private String matchSubsystem;
    private String matchRole;
    private URI matchUri;
    private int priority = 0;
    private String id;
    private List<String> ciphers;
    private List<String> protocols;
    private String keyStoreId;
    private String trustStoreId;
    private String hostnameVerifierId;
    private String keyAlias;
    private boolean requireClientAuth;
    private boolean requestClientAuth;
    private boolean useServerCipherPreferences = true;
    private boolean enableSNI = true;

    private Set<PublicKey> pinPublicKeys;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Activating " + //$NON-NLS-1$
                    ctx.getProperties().get("instanceId")); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Deactivating " + //$NON-NLS-1$
                    ctx.getProperties().get("instanceId")); //$NON-NLS-1$
        }
    }


    protected final void configure ( Dictionary<String, Object> config ) {
        String idSpec = (String) config.get(TLSConfiguration.ID);

        if ( idSpec == null ) {
            throw new IllegalArgumentException("id must be set"); //$NON-NLS-1$
        }
        this.id = idSpec.trim();

        String keyAliasSpec = (String) config.get(TLSConfiguration.KEY_ALIAS);

        if ( !StringUtils.isBlank(keyAliasSpec) ) {
            this.keyAlias = keyAliasSpec.trim();
        }

        configureMatcher(config);
        configureProtocol(config);
        configureStores(config);
        configurePinning(config);
    }


    /**
     * @param config
     */
    protected void configureMatcher ( Dictionary<String, Object> config ) {

        String roleSpec = (String) config.get(TLSConfiguration.ROLE);
        if ( roleSpec != null ) {
            this.matchRole = roleSpec.trim();
        }

        String subsysSpec = (String) config.get(TLSConfiguration.SUBSYSTEM);
        if ( subsysSpec != null ) {
            this.matchSubsystem = subsysSpec.trim();
        }

        String uriSpec = (String) config.get(TLSConfiguration.MATCH_URI);
        if ( uriSpec != null ) {
            this.matchUri = URI.create(uriSpec);
        }

        String prioSpec = (String) config.get(TLSConfiguration.PRIORITY);
        if ( prioSpec != null ) {
            this.priority = Integer.parseInt(prioSpec);
        }
        else if ( !StringUtils.isBlank(subsysSpec) ) {
            this.priority = 50;
        }
        else if ( roleSpec != null && !"default".equals(roleSpec.trim()) ) { //$NON-NLS-1$
            this.priority = 25;
        }
    }


    /**
     * @param config
     */
    protected void configureProtocol ( Dictionary<String, Object> config ) {
        String protocolSpec = (String) config.get(TLSConfiguration.PROTOCOLS);
        if ( protocolSpec != null ) {
            this.protocols = new ArrayList<>();
            for ( String proto : StringUtils.split(protocolSpec, ',') ) {
                this.protocols.add(proto.trim());
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Protocols are " + this.protocols); //$NON-NLS-1$
            }
        }

        String ciphersSpec = (String) config.get(TLSConfiguration.CIPHERS);
        if ( ciphersSpec != null ) {
            this.ciphers = new ArrayList<>();
            for ( String cipher : StringUtils.split(ciphersSpec, ',') ) {
                this.ciphers.add(cipher.trim());
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Ciphers are " + this.ciphers); //$NON-NLS-1$
            }
        }

        String disableSNISpec = (String) config.get(TLSConfiguration.DISABLE_SNI);
        if ( !StringUtils.isBlank(disableSNISpec) ) {
            this.enableSNI = !Boolean.parseBoolean(disableSNISpec);
        }
        else {
            this.enableSNI = true;
        }
    }


    /**
     * @param config
     */
    protected void configureStores ( Dictionary<String, Object> config ) {
        String keyStoreIdSpec = (String) config.get(TLSConfiguration.KEY_STORE);
        if ( keyStoreIdSpec != null ) {
            this.keyStoreId = keyStoreIdSpec.trim();
        }

        String trustStoreIdSpec = (String) config.get(TLSConfiguration.TRUST_STORE);
        if ( trustStoreIdSpec != null ) {
            this.trustStoreId = trustStoreIdSpec.trim();
        }

        String hostnameVerifierSpec = (String) config.get(TLSConfiguration.HOSTNAME_VERIFIER);
        if ( hostnameVerifierSpec != null ) {
            this.hostnameVerifierId = hostnameVerifierSpec.trim();
        }
    }


    /**
     * @param config
     */
    protected void configurePinning ( Dictionary<String, Object> config ) {
        Collection<String> pinKeys = ConfigUtil.parseStringCollection(config, "pinnedPublicKeys", Collections.EMPTY_LIST); //$NON-NLS-1$
        Set<PublicKey> pinnedPublicKeys = new HashSet<>();
        for ( String keytok : pinKeys ) {
            String[] parts = StringUtils.split(keytok, ':');
            if ( parts.length != 2 ) {
                log.warn("Invalid public key specification in config: " + keytok); //$NON-NLS-1$
                continue;
            }

            String algorithm = parts[ 0 ];
            byte[] encoded = Base64.getDecoder().decode(parts[ 1 ]);
            try {
                if ( "RSA".equals(algorithm) ) { //$NON-NLS-1$
                    pinnedPublicKeys.add(KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(encoded)));
                }
                else {
                    pinnedPublicKeys.add(KeyFactory.getInstance(algorithm).generatePublic(new PKCS8EncodedKeySpec(encoded)));
                }
            }
            catch (
                IllegalArgumentException |
                InvalidKeySpecException |
                NoSuchAlgorithmException e ) {
                log.warn("Invalid public key", e); //$NON-NLS-1$
            }
        }
        this.pinPublicKeys = pinnedPublicKeys;
    }


    @Override
    public boolean isApplicable ( String role, String subsystem, URI uri ) {

        if ( this.id == null ) {
            throw new IllegalStateException("TLSConfiguration not correctly initialized"); //$NON-NLS-1$
        }

        if ( this.matchSubsystem != null && !this.matchSubsystem.equals(subsystem) ) {
            return false;
        }

        if ( this.matchRole != null && !this.matchRole.equals(role) ) {
            return false;
        }

        if ( this.matchUri != null && !matchURIs(this.matchUri, uri) ) {
            return false;
        }

        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getPriority()
     */
    @Override
    public int getPriority () {
        return this.priority;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getCipherSuites()
     */
    @Override
    public List<String> getCipherSuites () {
        if ( this.ciphers == null ) {
            return Arrays.asList(DEFAULT_CIPHERS);
        }

        return this.ciphers;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getProtocols()
     */
    @Override
    public List<String> getProtocols () {
        if ( this.protocols == null ) {
            return Arrays.asList(DEFAULT_PROTOCOLS);
        }

        return this.protocols;
    }


    /**
     * @return the keyStoreId
     */
    @Override
    public String getKeyStoreId () {
        return this.keyStoreId;
    }


    /**
     * @return the keyAlias
     */
    public String getKeyAlias () {
        return this.keyAlias;
    }


    /**
     * @return the trustStoreId
     */
    @Override
    public String getTrustStoreId () {
        return this.trustStoreId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getRequireClientAuth()
     */
    @Override
    public boolean getRequireClientAuth () {
        return this.requireClientAuth;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getRequestClientAuth()
     */
    @Override
    public boolean getRequestClientAuth () {
        return this.requestClientAuth;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#useServerCipherPreferences()
     */
    @Override
    public boolean useServerCipherPreferences () {
        return this.useServerCipherPreferences;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#getHostnameVerifierId()
     */
    @Override
    public String getHostnameVerifierId () {
        return this.hostnameVerifierId;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSConfiguration#isEnableServerSNI()
     */
    @Override
    public boolean isEnableServerSNI () {
        return this.enableSNI;
    }


    /**
     * @return the pinPublicKeys
     */
    @Override
    public Set<PublicKey> getPinPublicKeys () {
        return Collections.unmodifiableSet(this.pinPublicKeys);
    }


    /**
     * @param toMatch
     * @param uri
     * @return
     */
    private static boolean matchURIs ( URI toMatch, URI b ) {
        if ( !schemeMatches(toMatch, b) || !hostMatches(toMatch, b) || !authorityMatches(toMatch, b) || !portMatches(toMatch, b)
                || !pathMatches(toMatch, b) ) {
            return false;
        }

        return true;
    }


    private static boolean pathMatches ( URI toMatch, URI b ) {
        return toMatch.getPath() == null || toMatch.getPath().equals(b.getPath());
    }


    private static boolean portMatches ( URI toMatch, URI b ) {
        return toMatch.getPort() == -1 || toMatch.getPort() == b.getPort();
    }


    private static boolean authorityMatches ( URI toMatch, URI b ) {
        return toMatch.getAuthority() == null || toMatch.getAuthority().equals(b.getHost());
    }


    private static boolean hostMatches ( URI toMatch, URI b ) {
        return toMatch.getHost() == null || toMatch.getHost().equals(b.getHost());
    }


    private static boolean schemeMatches ( URI toMatch, URI b ) {
        return toMatch.getScheme() == null || toMatch.getScheme().equals(b.getScheme());
    }

}
