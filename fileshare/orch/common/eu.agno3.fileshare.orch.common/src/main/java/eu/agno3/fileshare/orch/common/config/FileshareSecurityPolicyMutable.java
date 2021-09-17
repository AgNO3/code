/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareSecurityPolicy.class )
public interface FileshareSecurityPolicyMutable extends FileshareSecurityPolicy {

    /**
     * 
     * @param afterShareGracePeriod
     */
    void setAfterShareGracePeriod ( Duration afterShareGracePeriod );


    /**
     * 
     * @param defaultShareLifetime
     */
    void setDefaultShareLifetime ( Duration defaultShareLifetime );


    /**
     * 
     * @param defaultExpirationDuration
     */
    void setDefaultExpirationDuration ( Duration defaultExpirationDuration );


    /**
     * 
     * @param noUserTokenPasswords
     */
    void setNoUserTokenPasswords ( Boolean noUserTokenPasswords );


    /**
     * 
     * @param requireTokenPassword
     */
    void setRequireTokenPassword ( Boolean requireTokenPassword );


    /**
     * 
     * @param minTokenPasswordEntropy
     */
    void setMinTokenPasswordEntropy ( Integer minTokenPasswordEntropy );


    /**
     * 
     * @param requireAnyRole
     */
    void setRequireAnyRole ( Set<String> requireAnyRole );


    /**
     * 
     * @param transportMinKeySize
     */
    void setTransportMinKeySize ( Integer transportMinKeySize );


    /**
     * 
     * @param transportMinHashBlockSize
     */
    void setTransportMinHashBlockSize ( Integer transportMinHashBlockSize );


    /**
     * 
     * @param transportRequirePFS
     */
    void setTransportRequirePFS ( Boolean transportRequirePFS );


    /**
     * 
     * @param transportRequireEncryption
     */
    void setTransportRequireEncryption ( Boolean transportRequireEncryption );


    /**
     * 
     * @param maximumShareLifetime
     */
    void setMaximumShareLifetime ( Duration maximumShareLifetime );


    /**
     * 
     * @param maximumExpirationDuration
     */
    void setMaximumExpirationDuration ( Duration maximumExpirationDuration );


    /**
     * 
     * @param disallowWebDAVAccess
     */
    void setDisallowWebDAVAccess ( Boolean disallowWebDAVAccess );


    /**
     * 
     * @param allowedShareTypes
     */
    void setAllowedShareTypes ( Set<GrantType> allowedShareTypes );


    /**
     * 
     * @param sortPriority
     */
    void setSortPriority ( Integer sortPriority );


    /**
     * 
     * @param label
     */
    void setLabel ( String label );


    /**
     * 
     * @param restrictShareLifetime
     */
    void setRestrictShareLifetime ( Boolean restrictShareLifetime );


    /**
     * 
     * @param enableShareExpiration
     */
    void setEnableShareExpiration ( Boolean enableShareExpiration );


    /**
     * 
     * @param restrictExpirationDuration
     */
    void setRestrictExpirationDuration ( Boolean restrictExpirationDuration );


    /**
     * 
     * @param enableDefaultExpiration
     */
    void setEnableDefaultExpiration ( Boolean enableDefaultExpiration );


    /**
     * @param disallowRoles
     */
    void setDisallowRoles ( Set<String> disallowRoles );

}
