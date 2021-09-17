/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.Set;


/**
 * @author mbechler
 * 
 */
public interface InterfaceConfigurationMutable extends InterfaceConfiguration {

    /**
     * @param interfaces
     */
    void setInterfaces ( Set<InterfaceEntry> interfaces );

}
