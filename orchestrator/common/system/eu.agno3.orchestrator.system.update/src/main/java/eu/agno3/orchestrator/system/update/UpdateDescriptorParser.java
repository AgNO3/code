/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;

/**
 * @author mbechler
 *
 */
public interface UpdateDescriptorParser {

    /**
     * 
     * @param stream
     * @param desc
     * @return the materialized update descriptor
     * @throws UpdateException
     */
    UpdateDescriptor getEffective ( String stream, UpdateDescriptor desc ) throws UpdateException;


    /**
     * @param stream
     * @param imageType
     * @param cached
     * @return the materialized update descriptor
     * @throws UpdateException
     */
    UpdateDescriptor getLatestEffective ( String stream, String imageType, UpdateDescriptor cached ) throws UpdateException;


    /**
     * @param stream
     * @param ref
     * @return the materialized update descriptor
     * @throws UpdateException
     */
    UpdateDescriptor getEffective ( UpdateDescriptorRef ref ) throws UpdateException;

}
