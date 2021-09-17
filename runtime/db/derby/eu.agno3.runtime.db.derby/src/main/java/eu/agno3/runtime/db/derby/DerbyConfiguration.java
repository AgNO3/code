/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby;


/**
 * @author mbechler
 * 
 */
public final class DerbyConfiguration {

    private DerbyConfiguration () {}

    /**
     * Path to store database
     */
    public static final String SYSTEM_HOME_ATTR = "systemHome"; //$NON-NLS-1$

    /**
     * Enable data encryption
     */
    public static final String DATA_ENCRYPTION_ATTR = "dataEncryption"; //$NON-NLS-1$

    /**
     * Key used for data encryption
     */
    public static final String ENCRYPTION_KEY_ATTR = "encryptionKey"; //$NON-NLS-1$

    /**
     * Encryption algorithm used
     */
    public static final String ENCRYPTION_ALGORITHM_ATTR = "encryptionAlgorithm"; //$NON-NLS-1$

    /**
     * Encryption algorithm key length
     */
    public static final String ENCRYPTION_KEY_LENGTH_ATTR = "encryptionKeyLength"; //$NON-NLS-1$

    /**
     * Disable authentication
     */
    public static final String DISABLE_AUTHENTICATION = "disableAuthentication"; //$NON-NLS-1$

    /**
     * Default schema
     */
    public static final String DEFAULT_SCHEMA_ATTR = "defaultSchema"; //$NON-NLS-1$

    /**
     * Turn on lock debugging
     */
    public static final String DEBUG_LOCKS_ATTR = "debugLocks"; //$NON-NLS-1$

    /**
     * Turn on debug output
     */
    public static final String ENABLE_DEBUG = "enableDebug"; //$NON-NLS-1$

}
