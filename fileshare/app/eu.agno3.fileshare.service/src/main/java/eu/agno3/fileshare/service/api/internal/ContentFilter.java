/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.nio.channels.SeekableByteChannel;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSFileEntity;


/**
 * @author mbechler
 *
 */
public interface ContentFilter {

    /**
     * @param f
     * @param data
     * @throws FileshareException
     */
    void filterContent ( VFSFileEntity f, SeekableByteChannel data ) throws FileshareException;


    /**
     * @return an identifier for the content filter
     */
    String getId ();

}
