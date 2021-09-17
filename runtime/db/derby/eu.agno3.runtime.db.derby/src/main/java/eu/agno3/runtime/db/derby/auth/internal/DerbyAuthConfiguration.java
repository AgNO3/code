/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.auth.internal;


import java.util.Base64;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyWithVersion;
import eu.agno3.runtime.db.derby.auth.UserAccess;


/**
 * @author mbechler
 * 
 */
@Component ( configurationPid = DerbyAuthConfiguration.PID, service = {
    DerbyAuthConfiguration.class
} )
public class DerbyAuthConfiguration {

    /**
     * 
     */
    private static final String FAILED_TO_OBTAIN_USER_PASSWORD = "Failed to obtain user password"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String NOT_ACTIVE_CHECK_CONFIGURATION = "Not active, check configuration"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DerbyAuthConfiguration.class);

    private static final String USER_PREFIX = "user-"; //$NON-NLS-1$

    /**
     * Configuration PID
     */
    public static final String PID = "db.server.derby.auth"; //$NON-NLS-1$

    /**
     * Administrative username
     */
    public static final String ADMIN_USER = "adminUser"; //$NON-NLS-1$

    /**
     * Administrative user password
     */
    public static final String ADMIN_PASSWORD = "adminPassword"; //$NON-NLS-1$

    private Map<String, Map<String, UserAccess>> acls = new HashMap<>();
    private Set<String> users = new HashSet<>();

    private boolean active = false;

    private String adminUser;

    private SecretKeyProvider secretProvider;

    private int kvno;


    @Reference
    protected synchronized void setSecretKeyProvider ( SecretKeyProvider skp ) {
        this.secretProvider = skp;
    }


    protected synchronized void unsetSecretKeyProvider ( SecretKeyProvider skp ) {
        if ( this.secretProvider == skp ) {
            this.secretProvider = null;
        }
    }


    /**
     * @return the adminUser
     */
    public final String getAdminUser () {
        return this.adminUser;
    }


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext context ) {
        log.debug("Updating derby authentication configuration"); //$NON-NLS-1$
        setupUsers(context.getProperties());
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        this.active = false;
    }


    /**
     * @return the active
     */
    public boolean isActive () {
        return this.active;
    }


    /**
     * @param authConfig
     * @throws CryptoException
     */
    private void setupUsers ( Dictionary<String, Object> authConfig ) {
        String adminUserSpec = (String) authConfig.get(ADMIN_USER);

        if ( adminUserSpec == null ) {
            return;
        }

        this.adminUser = adminUserSpec;
        try {
            SecretKeyWithVersion secret = this.secretProvider.getOrCreateExportableSecret(makeKeyId(this.adminUser), -1, 8);
            this.kvno = secret.getVersion();
        }
        catch ( CryptoException e ) {
            log.error("Failed to obtain admin password", e); //$NON-NLS-1$
            return;
        }
        Enumeration<String> properties = authConfig.keys();
        this.users.add(this.adminUser);

        while ( properties.hasMoreElements() ) {
            String property = properties.nextElement();
            handleAuthProperty(authConfig, property);
        }

        this.active = true;
    }


    /**
     * @return the adminPassword
     * @throws CryptoException
     */
    public String getAdminPassword () throws CryptoException {
        return getUserPassword(this.adminUser);
    }


    /**
     * @param adminUser2
     * @return
     * @throws CryptoException
     */
    private String getUserPassword ( String user ) throws CryptoException {
        SecretKeyWithVersion key = this.secretProvider.getOrCreateExportableSecret(makeKeyId(user), this.kvno, 8);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }


    /**
     * @return
     */
    private static String makeKeyId ( String user ) {
        return String.format("db-%s", user); //$NON-NLS-1$
    }


    /**
     * @param authConfig
     * @param property
     */
    private void handleAuthProperty ( Dictionary<String, Object> authConfig, String property ) {
        if ( property.startsWith(USER_PREFIX) ) {
            String userName = property.substring(USER_PREFIX.length());

            try {
                this.secretProvider.getOrCreateExportableSecret(makeKeyId(userName), this.kvno, 8);
            }
            catch ( CryptoException e ) {
                log.error("Failed to obtain user password " + userName, e); //$NON-NLS-1$
                return;
            }

            this.users.add(userName);

            String aclSpec = (String) authConfig.get(property);
            handleAclProperty(userName, aclSpec);
        }
    }


    /**
     * @param userName
     * @param aclSpec
     */
    private void handleAclProperty ( String userName, String aclSpec ) {
        String[] aclSpecs = aclSpec.split(Pattern.quote(",")); //$NON-NLS-1$

        for ( String acl : aclSpecs ) {
            String[] parts = acl.split(Pattern.quote("@"), 2); //$NON-NLS-1$

            if ( parts.length != 2 ) {
                log.warn("Illegal ACL specification (expect PRIV@database): " + acl); //$NON-NLS-1$
                continue;
            }

            UserAccess priv = null;
            try {
                priv = UserAccess.valueOf(parts[ 0 ].trim());
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Illegal ACL specification, not a valid privilege level: " + parts[ 0 ].trim(), e); //$NON-NLS-1$
                continue;
            }
            String db = parts[ 1 ].trim();

            if ( !this.acls.containsKey(db) ) {
                this.acls.put(db, new HashMap<String, UserAccess>());
            }

            this.acls.get(db).put(userName, priv);
        }
    }


    /**
     * Get the configured access for a specific database and user
     * 
     * @param database
     * @param userName
     * @return access granted to user
     */
    public UserAccess getAccess ( String database, String userName ) {

        if ( !this.active ) {
            throw new IllegalStateException(NOT_ACTIVE_CHECK_CONFIGURATION);
        }

        if ( userName.equals(this.adminUser) ) {
            return UserAccess.ADMIN;
        }

        if ( !this.acls.containsKey(database) ) {
            return UserAccess.NONE;
        }

        return getUserAccess(database, userName);
    }


    /**
     * @param database
     * @param userName
     * @return
     */
    private UserAccess getUserAccess ( String database, String userName ) {
        Map<String, UserAccess> dbUsers = this.acls.get(database);

        if ( dbUsers == null ) {
            return UserAccess.NONE;
        }

        if ( !dbUsers.containsKey(userName) ) {
            return UserAccess.NONE;
        }

        return dbUsers.get(userName);
    }


    /**
     * Get all authorized users for a specific databasef
     * 
     * @param database
     * @return a map of user authorizations
     */
    public Map<String, UserAccess> getAuthorizedUsers ( String database ) {
        Map<String, UserAccess> result = new HashMap<>();

        result.put(this.getAdminUser(), UserAccess.ADMIN);

        if ( !this.active || !this.acls.containsKey(database) ) {
            return result;
        }

        result.putAll(this.acls.get(database));

        return result;
    }


    /**
     * Check a user password
     * 
     * @param userName
     * @param userPassword
     * @return whether the password is valid or not
     */
    public boolean checkPassword ( String userName, String userPassword ) {

        if ( !this.active ) {
            throw new IllegalStateException(NOT_ACTIVE_CHECK_CONFIGURATION);
        }

        if ( !this.users.contains(userName) ) {
            return false;
        }

        try {
            return getUserPassword(userName).equals(userPassword);
        }
        catch ( CryptoException e ) {
            throw new IllegalStateException(FAILED_TO_OBTAIN_USER_PASSWORD, e);
        }
    }


    /**
     * Get a users password
     * 
     * @param userName
     * @return the user password
     */
    public String getPassword ( String userName ) {
        if ( !this.active ) {
            throw new IllegalStateException(NOT_ACTIVE_CHECK_CONFIGURATION);
        }

        if ( !this.users.contains(userName) ) {
            throw new IllegalStateException("Unknown user"); //$NON-NLS-1$
        }

        try {
            return getUserPassword(userName);
        }
        catch ( CryptoException e ) {
            throw new IllegalStateException(FAILED_TO_OBTAIN_USER_PASSWORD, e);
        }
    }

}
