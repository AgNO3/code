/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import eu.agno3.orchestrator.system.info.SystemInformationProvider;


/**
 * @author mbechler
 * 
 */
public interface PlatformInformationProvider extends SystemInformationProvider<PlatformInformation> {

    /**
     * @return the platform information
     */
    @Override
    PlatformInformation getInformation ();
}
