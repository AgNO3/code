/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.net.URI;
import java.security.PublicKey;
import java.util.List;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface TLSConfiguration {

    /**
     * Match role
     */
    public static final String ROLE = "role"; //$NON-NLS-1$
    /**
     * Match subsystem
     */
    public static final String SUBSYSTEM = "subsystem"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String MATCH_URI = "uri"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String PRIORITY = "priority"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String ID = "instanceId"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String PROTOCOLS = "protocols"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String CIPHERS = "ciphers"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String KEY_STORE = "keyStore"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TRUST_STORE = "trustStore"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String HOSTNAME_VERIFIER = "hostnameVerifier"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String KEY_ALIAS = "keyAlias"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String PID = "tls.mapping"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String DISABLE_SNI = "disableSNI"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String PINNED_PUBLIC_KEYS = "pinnedPublicKeys"; //$NON-NLS-1$


    /**
     * @return an unique identifier for this configuration
     */
    String getId ();


    /**
     * @param role
     * @param subsystem
     * @param uri
     * @return whether this configuration is applicable for the given subsystem and endpoint (local - for servers,
     *         remote - for clients)
     */
    boolean isApplicable ( String role, String subsystem, URI uri );


    /**
     * @return the priority of this configuration, higher meaning checked earlier, first match wins
     */
    int getPriority ();


    /**
     * @return the allowed ciphers
     */
    List<String> getCipherSuites ();


    /**
     * 
     * @return whether to use the server's cipher preferences
     */
    boolean useServerCipherPreferences ();


    /**
     * @return the allowed protocols
     */
    List<String> getProtocols ();


    /**
     * @return the used key store
     */
    String getKeyStoreId ();


    /**
     * @return the used trust store
     */
    String getTrustStoreId ();


    /**
     * 
     * @return the used hostname verifier
     */
    String getHostnameVerifierId ();


    /**
     * @return whether to require client authentication
     */
    boolean getRequireClientAuth ();


    /**
     * @return whether to require client authentication
     */
    boolean getRequestClientAuth ();


    /**
     * @return whether to enable SNI support
     */
    boolean isEnableServerSNI ();


    /**
     * @return public key pins to use for establishing trust
     */
    Set<PublicKey> getPinPublicKeys ();

}
