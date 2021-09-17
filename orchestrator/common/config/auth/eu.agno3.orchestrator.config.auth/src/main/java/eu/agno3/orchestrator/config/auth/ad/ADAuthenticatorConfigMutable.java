/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ad;


import java.util.Set;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfigMutable;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.ldap.LDAPSyncOptionsMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( ADAuthenticatorConfig.class )
public interface ADAuthenticatorConfigMutable extends ADAuthenticatorConfig, AuthenticatorConfigMutable {

    /**
     * @param domain
     */
    void setDomain ( String domain );


    /**
     * @param keytab
     */
    void setKeytab ( String keytab );


    /**
     * 
     * @param rejectSids
     */
    void setRejectSids ( Set<String> rejectSids );


    /**
     * 
     * @param requiredSids
     */
    void setRequiredSids ( Set<String> requiredSids );


    /**
     * 
     * @param rejectDomainSids
     */
    void setRejectDomainSids ( Set<String> rejectDomainSids );


    /**
     * 
     * @param acceptDomainSids
     */
    void setAcceptDomainSids ( Set<String> acceptDomainSids );


    /**
     * 
     * @param requireDomainUserGroup
     */
    void setRequireDomainUserGroup ( Boolean requireDomainUserGroup );


    /**
     * 
     * @param acceptOnlyLocal
     */
    void setAcceptOnlyLocal ( Boolean acceptOnlyLocal );


    /**
     * 
     * @param rejectNonADPrincipals
     */
    void setRejectNonADPrincipals ( Boolean rejectNonADPrincipals );


    /**
     * 
     * @param disablePACs
     */
    void setDisablePACs ( Boolean disablePACs );


    /**
     * 
     * @param disablePACValidation
     */
    void setDisablePACValidation ( Boolean disablePACValidation );


    /**
     * 
     * @param sendNTLMChallenge
     */
    void setSendNTLMChallenge ( Boolean sendNTLMChallenge );


    /**
     * 
     * @param acceptNTLMFallback
     */
    void setAcceptNTLMFallback ( Boolean acceptNTLMFallback );


    /**
     * 
     * @param principalAddRoles
     */
    void setPrincipalAddRoles ( Set<PatternRoleMapEntry> principalAddRoles );


    /**
     * 
     * @param alwaysAddRoles
     */
    void setAlwaysAddRoles ( Set<String> alwaysAddRoles );


    /**
     * 
     * @param rejectPrincipalPatterns
     */
    void setRejectPrincipalPatterns ( Set<String> rejectPrincipalPatterns );


    /**
     * 
     * @param acceptPrincipalPatterns
     */
    void setAcceptPrincipalPatterns ( Set<String> acceptPrincipalPatterns );


    /**
     * 
     * @param allowPasswordFallback
     */
    void setAllowPasswordFallback ( Boolean allowPasswordFallback );


    /**
     * 
     * @param serviceName
     */
    void setServiceName ( String serviceName );


    /**
     * 
     * @param syncOptions
     */
    void setSyncOptions ( LDAPSyncOptionsMutable syncOptions );


    /**
     * 
     * @param enableSynchronization
     */
    void setEnableSynchronization ( Boolean enableSynchronization );


    /**
     * 
     * @param groupSyncFilter
     */
    void setGroupSyncFilter ( String groupSyncFilter );


    /**
     * 
     * @param groupSyncBase
     */
    void setGroupSyncBase ( String groupSyncBase );


    /**
     * 
     * @param userSyncFilter
     */
    void setUserSyncFilter ( String userSyncFilter );


    /**
     * 
     * @param userSyncBase
     */
    void setUserSyncBase ( String userSyncBase );

}
