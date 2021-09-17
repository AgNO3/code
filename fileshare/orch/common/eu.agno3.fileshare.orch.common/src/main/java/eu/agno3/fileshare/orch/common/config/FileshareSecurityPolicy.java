/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:securityPolicy" )
public interface FileshareSecurityPolicy extends ConfigurationObject {

    /**
     * 
     * @return amount of file expiration time to add to the maximum share lifetime
     */
    Duration getAfterShareGracePeriod ();


    /**
     * 
     * @return whether to restrict the share lifetime
     */
    Boolean getRestrictShareLifetime ();


    /**
     * 
     * @return whether to enable share expiration by default
     */
    Boolean getEnableShareExpiration ();


    /**
     * 
     * @return whether to restrict entity expiration time
     */
    Boolean getRestrictExpirationDuration ();


    /**
     * 
     * @return whether to enable entity expiration by default
     */
    Boolean getEnableDefaultExpiration ();


    /**
     * 
     * @return default share lifetime
     */
    Duration getDefaultShareLifetime ();


    /**
     * 
     * @return default entity expiration time
     */
    Duration getDefaultExpirationDuration ();


    /**
     * 
     * @return maximum allowable share lifetime (may be extended)
     */
    Duration getMaximumShareLifetime ();


    /**
     * 
     * @return maximum allowable expiration duration
     */
    Duration getMaximumExpirationDuration ();


    /**
     * 
     * @return do not allow users to pick share passwords
     */
    Boolean getNoUserTokenPasswords ();


    /**
     * 
     * @return require the use of a share password
     */
    Boolean getRequireTokenPassword ();


    /**
     * 
     * @return the minimum entropy to require for share passwords
     */
    Integer getMinTokenPasswordEntropy ();


    /**
     * 
     * @return require the accessing user to have any of the given roles
     */
    Set<String> getRequireAnyRole ();


    /**
     * @return disallow access if the accessing user has any of the given roles
     */
    Set<String> getDisallowRoles ();


    /**
     * 
     * @return the minimum required transport TLS key size
     */
    Integer getTransportMinKeySize ();


    /**
     * 
     * @return the minimum required transport TLS hash block size
     */
    Integer getTransportMinHashBlockSize ();


    /**
     * 
     * @return require transport PFS
     */
    Boolean getTransportRequirePFS ();


    /**
     * 
     * @return require transport encryption
     */
    Boolean getTransportRequireEncryption ();


    /**
     * 
     * @return do not allow webdav client to access
     */
    Boolean getDisallowWebDAVAccess ();


    /**
     * 
     * @return share types to allow
     */
    Set<GrantType> getAllowedShareTypes ();


    /**
     * 
     * @return sort priority, for display purposes
     */
    Integer getSortPriority ();


    /**
     * 
     * @return the label to match
     */
    String getLabel ();

}
