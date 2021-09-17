/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.datetime;


import java.util.List;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTimeZone;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.types.net.NetworkAddressType;
import eu.agno3.orchestrator.types.net.name.HostOrAddress;
import eu.agno3.orchestrator.types.net.validation.ValidHostOrAddress;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkAddress;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:datetime" )
public interface DateTimeConfiguration extends ConfigurationObject {

    /**
     * 
     * @return whether the hardware clock shall be treated as UTC
     */
    @NotNull ( groups = Materialized.class )
    Boolean getHwClockUTC ();


    /**
     * 
     * @return the hosts timezone
     */
    @NotNull ( groups = Materialized.class )
    DateTimeZone getTimezone ();


    /**
     * 
     * @return whether to synchronize the clock via NTP
     */
    @NotNull ( groups = Materialized.class )
    Boolean getNtpEnabled ();


    /**
     * 
     * @return the NTP servers to synchronize with
     */
    @ValidHostOrAddress ( addr = @ValidNetworkAddress ( allowedTypes = {
        NetworkAddressType.LOOPBACK, NetworkAddressType.UNICAST, NetworkAddressType.MULTICAST, NetworkAddressType.BROADCAST,
        NetworkAddressType.ANYCAST
    } ) )
    List<HostOrAddress> getNtpServers ();
}
