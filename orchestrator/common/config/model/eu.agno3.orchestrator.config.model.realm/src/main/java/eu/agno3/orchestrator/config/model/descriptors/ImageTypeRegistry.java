/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;


/**
 * @author mbechler
 *
 */
public interface ImageTypeRegistry {

    /**
     * @param type
     * @return the image type descriptor
     * @throws ModelServiceException
     */
    ImageTypeDescriptor getDescriptor ( String type ) throws ModelServiceException;


    /**
     * @return the known image types
     */
    Set<String> getImageTypes ();

}
