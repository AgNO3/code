/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.List;
import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( RealmConfig.class )
public interface RealmConfigMutable extends RealmConfig {

    /**
     * @param domainName
     *            the domainName to set
     */
    void setRealmName ( String domainName );


    /**
     * @param type
     * 
     */
    void setRealmType ( RealmType type );


    /**
     * @param overrideLocalHostname
     *            the overrideLocalHostname to set
     */
    void setOverrideLocalHostname ( String overrideLocalHostname );


    /**
     * @param domainMappings
     *            the domainMappings to set
     */
    void setDomainMappings ( List<String> domainMappings );


    /**
     * @param caPaths
     *            the caPaths to set
     */
    void setCaPaths ( Set<CAPathEntry> caPaths );


    /**
     * @param importKeytabs
     */
    void setImportKeytabs ( Set<KeytabEntry> importKeytabs );


    /**
     * @param maximumTicketLifetime
     */
    void setMaximumTicketLifetime ( Duration maximumTicketLifetime );


    /**
     * @param serviceRekeyInterval
     */
    void setServiceRekeyInterval ( Duration serviceRekeyInterval );


    /**
     * @param rekeyServices
     */
    void setRekeyServices ( Boolean rekeyServices );


    /**
     * @param secLevel
     */
    void setSecurityLevel ( KerberosSecurityLevel secLevel );

}
