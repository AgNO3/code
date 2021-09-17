/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.resolver;


import java.util.List;

import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 * 
 */
public interface ResolverConfigurationMutable extends ResolverConfiguration {

    /**
     * @param autoconfigure
     * 
     */
    void setAutoconfigureDns ( Boolean autoconfigure );


    /**
     * @param nameservers
     */
    void setNameservers ( List<NetworkAddress> nameservers );

}
