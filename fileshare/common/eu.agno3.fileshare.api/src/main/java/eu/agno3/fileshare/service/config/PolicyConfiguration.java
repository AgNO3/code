/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.fileshare.model.GrantType;


/**
 * @author mbechler
 *
 */
public interface PolicyConfiguration {

    /**
     * 
     * @return the label this policy handles
     */
    String getLabel ();


    /**
     * 
     * @return the sort order
     */
    float getSortOrder ();


    /**
     * 
     * @return the share types (subject, mail, link) that are allowed
     */
    Set<GrantType> getAllowedShareTypes ();


    /**
     * @return the maximum allowed entity expiration time
     */
    Duration getMaximumExpirationDuration ();


    /**
     * @return the default entity expiration time
     */
    Duration getDefaultExpirationDuration ();


    /**
     * 
     * @return the maximum share lifetime
     */
    Duration getMaximumShareLifetime ();


    /**
     * 
     * @return the default share lifetime
     */
    Duration getDefaultShareLifetime ();


    /**
     * @return the period the entity expiry will be extended after the share lifetime
     */
    Duration getAfterShareGracePeriod ();


    /**
     * 
     * @return whether to enforce transport encryption
     */
    boolean isTransportRequireEncryption ();


    /**
     * @return whether to enforce transport perfect forward secrecy
     */
    boolean isTransportRequirePFS ();


    /**
     * 
     * @return minimum encryption key size to enforce
     */
    int getTransportMinKeySize ();


    /**
     * 
     * @return minimum transport hash block size to enforce
     */
    int getTransportMinHashBlockSize ();


    /**
     * @return the roles that are required to access the resource
     */
    Set<String> getRequireAnyRole ();


    /**
     * @return if the accessing user has any of these roles, access is disallowed
     */
    Set<String> getDisallowRoles ();


    /**
     * 
     * @return the required number of authentication factors
     */
    int getMinAuthFactors ();


    /**
     * 
     * @return whether to require a hardware factor
     */
    boolean isRequireHardwareFactor ();


    /**
     * @return check password policy even when multifactor auth is used
     */
    boolean isAlwaysCheckPasswordPolicy ();


    /**
     * @return the maximum allowed password age
     */
    Duration getMaxPasswordAge ();


    /**
     * 
     * @return the minimum required password entropy
     */
    int getMinimumPasswordEntropy ();


    /**
     * @return whether a token password is required for mail/link shares
     */
    boolean isRequireTokenPassword ();


    /**
     * @return the minimum token password entropy
     */
    int getMinTokenPasswordEntropy ();


    /**
     * @return whether to disallow users to specify their own passwords
     */
    boolean isNoUserTokenPasswords ();


    /**
     * @return whether access to this file via WebDAV is disallowed
     */
    boolean isDisallowWebDAVAccess ();

}
