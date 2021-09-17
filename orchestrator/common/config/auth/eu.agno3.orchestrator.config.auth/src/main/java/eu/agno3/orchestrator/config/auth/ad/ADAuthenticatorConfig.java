/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ad;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.auth.ldap.LDAPSyncOptions;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.types.validation.ValidDN;
import eu.agno3.orchestrator.types.validation.ValidLDAPFilter;
import eu.agno3.orchestrator.types.validation.ValidSID;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:authenticator:ad" )
public interface ADAuthenticatorConfig extends AuthenticatorConfig {

    /**
     * 
     * @return the ad domain to use
     */
    String getDomain ();


    /**
     * @return keytab to use, if not set the host account is used
     */
    String getKeytab ();


    /**
     * 
     * @return reject users that are member of any of these SIDs
     */
    @ValidSID
    Set<String> getRejectSids ();


    /**
     * 
     * @return accept only users that are member of at least one of these SIDs
     */
    @ValidSID
    Set<String> getRequiredSids ();


    /**
     * 
     * @return reject users from the listed domains
     */
    @ValidSID
    Set<String> getRejectDomainSids ();


    /**
     * 
     * @return accept only users from listed domains
     */
    @ValidSID
    Set<String> getAcceptDomainSids ();


    /**
     * 
     * @return accept only users with the domain user group (..-513)
     */
    Boolean getRequireDomainUserGroup ();


    /**
     * 
     * @return accept only users from the local domain
     */
    Boolean getAcceptOnlyLocal ();


    /**
     * 
     * @return reject non active directory principals (e.g. via cross realm trust)
     */
    Boolean getRejectNonADPrincipals ();


    /**
     * 
     * @return disable all PAC usage
     */
    Boolean getDisablePACs ();


    /**
     * 
     * @return disable online validation of PACs
     */
    Boolean getDisablePACValidation ();


    /**
     * 
     * @return send NTLM challenge
     */
    Boolean getSendNTLMChallenge ();


    /**
     * 
     * @return accept NTLM authentication
     */
    Boolean getAcceptNTLMFallback ();


    /**
     * 
     * @return SID to role mappings
     */
    @ReferencedObject
    @Valid
    Set<StaticRoleMapEntry> getSidRoles ();


    /**
     * 
     * @return pricipal name patterns to add roles
     */
    @ReferencedObject
    @Valid
    Set<PatternRoleMapEntry> getPrincipalAddRoles ();


    /**
     * 
     * @return roles always added to authenticated users
     */
    Set<String> getAlwaysAddRoles ();


    /**
     * 
     * @return reject principals matching this pattern
     */
    Set<String> getRejectPrincipalPatterns ();


    /**
     * 
     * @return accept principals matching this pattern
     */
    Set<String> getAcceptPrincipalPatterns ();


    /**
     * 
     * @return allow fallback to password authentication against kerberos
     */
    Boolean getAllowPasswordFallback ();


    /**
     * 
     * @return service name
     */
    String getServiceName ();


    /**
     * 
     * @return the directory synchronization options
     */
    @ReferencedObject
    @Valid
    LDAPSyncOptions getSyncOptions ();


    /**
     * 
     * @return whether to syncronize users with the directory
     */
    Boolean getEnableSynchronization ();


    /**
     * 
     * @return filter to restrict synchronized groups
     */
    @ValidLDAPFilter
    String getGroupSyncFilter ();


    /**
     * 
     * @return base DN, relative to domain DN to synchronize groups from
     */
    @ValidDN
    String getGroupSyncBase ();


    /**
     * 
     * @return filter to restrict synchronized users
     */
    @ValidLDAPFilter
    String getUserSyncFilter ();


    /**
     * 
     * @return base DN, relative to domain DN to synchronize users from
     */
    @ValidDN
    String getUserSyncBase ();

}
