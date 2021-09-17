/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.InputStream;


/**
 * @author mbechler
 *
 */
public interface UpdateDescriptorLoader {

    /**
     * 
     * @param stream
     * @param imageType
     * @param cached
     * @return the lastest update descriptor
     * @throws UpdateException
     */
    public UpdateDescriptor getLatest ( String stream, String imageType, UpdateDescriptor cached ) throws UpdateException;


    /**
     * 
     * @param ref
     * @return the references descriptor
     * @throws UpdateException
     */
    public UpdateDescriptor getReference ( UpdateDescriptorRef ref ) throws UpdateException;


    /**
     * 
     * @param is
     * @return the parsed update descriptor
     * @throws UpdateException
     */
    public UpdateDescriptor fromStream ( InputStream is ) throws UpdateException;

}
