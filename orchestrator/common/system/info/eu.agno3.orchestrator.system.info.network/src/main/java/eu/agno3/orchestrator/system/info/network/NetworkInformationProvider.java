/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import eu.agno3.orchestrator.system.info.SystemInformationProvider;


/**
 * @author mbechler
 * 
 */
public interface NetworkInformationProvider extends SystemInformationProvider<NetworkInformation> {

    /**
     * @return the disovered network information
     */
    @Override
    NetworkInformation getInformation ();
}
