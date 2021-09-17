/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.datetime;


import java.util.List;

import org.joda.time.DateTimeZone;

import eu.agno3.orchestrator.types.net.name.HostOrAddress;


/**
 * @author mbechler
 * 
 */
public interface DateTimeConfigurationMutable extends DateTimeConfiguration {

    /**
     * @param hwClockUTC
     */
    void setHwClockUTC ( Boolean hwClockUTC );


    /**
     * @param timezone
     */
    void setTimezone ( DateTimeZone timezone );


    /**
     * @param ntpEnabled
     */
    void setNtpEnabled ( Boolean ntpEnabled );


    /**
     * @param ntpServers
     */
    void setNtpServers ( List<HostOrAddress> ntpServers );

}
