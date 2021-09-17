/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.krb5;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:authenticator:krb5" )
public interface KerberosAuthenticatorConfig extends AuthenticatorConfig {

    /**
     * 
     * @return the kerberos realm to use
     */
    String getKerberosRealm ();


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
     * @return keytab to use
     */
    String getKeytabAlias ();

}
