/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.krb5;


import java.util.Set;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfigMutable;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;


/**
 * @author mbechler
 *
 */
public interface KerberosAuthenticatorConfigMutable extends KerberosAuthenticatorConfig, AuthenticatorConfigMutable {

    /**
     * 
     * @param krbRealm
     */
    void setKerberosRealm ( String krbRealm );


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
     * @param keytabAlias
     */
    void setKeytabAlias ( String keytabAlias );
}
