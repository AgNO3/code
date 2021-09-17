/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:realms:kerberos" )
public interface KerberosConfig extends ConfigurationObject {

    /**
     * 
     * @return message size below which to use UDP
     */
    Integer getUdpPreferenceLimit ();


    /**
     * 
     * @return maximum kdc retries
     */
    Integer getMaxRetries ();


    /**
     * 
     * @return kdc response timeout
     */
    Duration getKdcTimeout ();


    /**
     * 
     * @return maximum allowed clock difference
     */
    Duration getMaxClockskew ();


    /**
     * 
     * @return the default requested ticket enctypes
     */
    Set<String> getDefaultTicketEnctypes ();


    /**
     * 
     * @return the default requested TGT enctypes
     */
    Set<String> getDefaultTGSEnctypes ();


    /**
     * 
     * @return the enctypes that may be used in tickets/preauth
     */
    Set<String> getPermittedEnctypes ();


    /**
     * 
     * @return allow weak cryptographic algorithms
     */
    Boolean getAllowWeakCrypto ();


    /**
     * 
     * @return request TGTs to be renewable by default
     */
    Boolean getDefaultTGTRenewable ();


    /**
     * 
     * @return request TGTs to be proxiable by default
     */
    Boolean getDefaultTGTProxiable ();


    /**
     * 
     * @return request TGTs to be forwardable by default
     */
    Boolean getDefaultTGTForwardable ();


    /**
     * 
     * @return do not use addresses in tickets
     */
    Boolean getDisableAddresses ();


    /**
     * 
     * @return lookup target realm in DNS
     */
    Boolean getDnsLookupRealm ();


    /**
     * 
     * @return lookup realm KDC in DNS
     */
    Boolean getDnsLookupKDC ();

}
