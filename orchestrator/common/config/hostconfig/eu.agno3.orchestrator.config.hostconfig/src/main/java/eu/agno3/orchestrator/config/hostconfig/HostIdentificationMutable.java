/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( HostIdentification.class )
public interface HostIdentificationMutable extends HostIdentification {

    /**
     * @param hostname
     */
    void setHostName ( String hostname );


    /**
     * @param domainName
     */
    void setDomainName ( String domainName );

}
