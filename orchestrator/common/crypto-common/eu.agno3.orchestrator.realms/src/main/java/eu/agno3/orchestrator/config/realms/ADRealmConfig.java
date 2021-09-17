/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.config.OneOff;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:realms:ad" )
public interface ADRealmConfig extends RealmConfig {

    /**
     * 
     * @return store the machine entry under this DN
     */
    String getMachineBaseDN ();


    /**
     * 
     * @return override the netbios hostname
     */
    String getOverrideNetbiosHostname ();


    /**
     * 
     * @return override the netbios domain name
     */
    String getOverrideNetbiosDomainName ();


    /**
     * 
     * @return override the machine account name
     */
    String getOverrideMachineAccount ();


    /**
     * @return the interval after which the machine account is rekeyed
     */
    Duration getMachineRekeyInterval ();


    /**
     * @return whether to rekey the machine account perodically
     */
    Boolean getRekeyMachineAccount ();


    /**
     * @return whether to perform automatic DNS updates
     */
    Boolean getUpdateDns ();


    /**
     * 
     * @return TTL to set when performing automatic DNS update
     */
    Duration getUpdateDnsTtl ();


    /**
     * 
     * @return update with addresses from the given interface
     */
    String getUpdateDnsFromInterface ();


    /**
     * @return whether to perform only secure DNS updates
     */
    Boolean getUpdateDnsForceSecure ();


    /**
     * @return whether to join domain on the next apply
     */
    @OneOff
    Boolean getDoJoin ();


    /**
     * @return whether to rekey on the next apply
     */
    @OneOff
    Boolean getDoRekey ();


    /**
     * @return whether to leave domain on the next apply
     */
    @OneOff
    Boolean getDoLeave ();


    /**
     * 
     * @return type of domain join
     */
    ADJoinType getJoinType ();


    /**
     * 
     * @return machine password to use for account-less join
     */
    String getCustomMachineJoinPassword ();


    /**
     * 
     * @return join user account to use for joining
     */
    String getJoinUser ();


    /**
     * 
     * @return join user password
     */
    String getJoinPassword ();


    /**
     * 
     * @return whether to disable SMB2
     */
    Boolean getDisableSMB2 ();


    /**
     * 
     * @return whether to allow SMB1 fallback
     */
    Boolean getAllowSMB1 ();

}
