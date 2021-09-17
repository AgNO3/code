/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update;


import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateException;


/**
 * @author mbechler
 *
 */
public interface UpdateDescriptorGenerator {

    /**
     * 
     * @param stream
     * @param imageType
     * @param sequence
     * @return an update descriptor including all available updates
     * @throws UpdateException
     */
    UpdateDescriptor generateDescriptor ( String stream, String imageType, long sequence ) throws UpdateException;

}