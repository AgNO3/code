/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( KerberosConfig.class )
public interface KerberosConfigMutable extends KerberosConfig {

    /**
     * @param dnsLookupKDC
     *            the dnsLookupKDC to set
     */
    void setDnsLookupKDC ( Boolean dnsLookupKDC );


    /**
     * @param dnsLookupRealm
     *            the dnsLookupRealm to set
     */
    void setDnsLookupRealm ( Boolean dnsLookupRealm );


    /**
     * @param disableAddresses
     *            the disableAddresses to set
     */
    void setDisableAddresses ( Boolean disableAddresses );


    /**
     * @param defaultTGTForwardable
     *            the defaultTGTForwardable to set
     */
    void setDefaultTGTForwardable ( Boolean defaultTGTForwardable );


    /**
     * @param defaultTGTProxiable
     *            the defaultTGTProxiable to set
     */
    void setDefaultTGTProxiable ( Boolean defaultTGTProxiable );


    /**
     * @param defaultTGTRenewable
     *            the defaultTGTRenewable to set
     */
    void setDefaultTGTRenewable ( Boolean defaultTGTRenewable );


    /**
     * @param allowWeakCrypto
     *            the allowWeakCrypto to set
     */
    void setAllowWeakCrypto ( Boolean allowWeakCrypto );


    /**
     * @param permittedEnctypes
     *            the permittedEnctypes to set
     */
    void setPermittedEnctypes ( Set<String> permittedEnctypes );


    /**
     * @param defaultTGSEnctypes
     *            the defaultTGSEnctypes to set
     */
    void setDefaultTGSEnctypes ( Set<String> defaultTGSEnctypes );


    /**
     * @param defaultTicketEnctypes
     *            the defaultTicketEnctypes to set
     */
    void setDefaultTicketEnctypes ( Set<String> defaultTicketEnctypes );


    /**
     * @param maxClockskew
     *            the maxClockskewSeconds to set
     */
    void setMaxClockskew ( Duration maxClockskew );


    /**
     * @param kdcTimeout
     *            the kdcTimeout to set
     */
    void setKdcTimeout ( Duration kdcTimeout );


    /**
     * @param maxRetries
     *            the maxRetries to set
     */
    void setMaxRetries ( Integer maxRetries );


    /**
     * @param udpPreferenceLimit
     *            the udpPreferenceLimit to set
     */
    void setUdpPreferenceLimit ( Integer udpPreferenceLimit );

}