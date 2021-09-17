/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( ADRealmConfig.class )
public interface ADRealmConfigMutable extends ADRealmConfig, RealmConfigMutable {

    /**
     * @param overrideMachineAccount
     *            the overrideMachineAccount to set
     */
    void setOverrideMachineAccount ( String overrideMachineAccount );


    /**
     * @param overrideNetbiosDomainName
     *            the overrideNetbiosDomainName to set
     */
    void setOverrideNetbiosDomainName ( String overrideNetbiosDomainName );


    /**
     * @param overrideNetbiosHostname
     *            the overrideNetbiosHostname to set
     */
    void setOverrideNetbiosHostname ( String overrideNetbiosHostname );


    /**
     * @param machineBaseDN
     *            the machineBaseDN to set
     */
    void setMachineBaseDN ( String machineBaseDN );


    /**
     * @param machineRekeyInterval
     */
    void setMachineRekeyInterval ( Duration machineRekeyInterval );


    /**
     * @param rekeyMachineAccount
     */
    void setRekeyMachineAccount ( Boolean rekeyMachineAccount );


    /**
     * @param updateDNS
     */
    void setUpdateDns ( Boolean updateDNS );


    /**
     * @param intf
     */
    void setUpdateDnsFromInterface ( String intf );


    /**
     * @param ttl
     */
    void setUpdateDnsTtl ( Duration ttl );


    /**
     * @param forceSecure
     */
    void setUpdateDnsForceSecure ( Boolean forceSecure );


    /**
     * @param doJoin
     */
    void setDoJoin ( Boolean doJoin );


    /**
     * @param doRekey
     */
    void setDoRekey ( Boolean doRekey );


    /**
     * @param doLeave
     */
    void setDoLeave ( Boolean doLeave );


    /**
     * 
     * @param customMachineJoinPassword
     */
    void setCustomMachineJoinPassword ( String customMachineJoinPassword );


    /**
     * 
     * @param joinPassword
     */
    void setJoinPassword ( String joinPassword );


    /**
     * 
     * @param joinUser
     */
    void setJoinUser ( String joinUser );


    /**
     * 
     * @param joinType
     */
    void setJoinType ( ADJoinType joinType );


    /**
     * 
     * @param disableSMB2
     */
    void setDisableSMB2 ( Boolean disableSMB2 );


    /**
     * 
     * @param allowSMB1
     */
    void setAllowSMB1 ( Boolean allowSMB1 );

}