/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 6, 2016 by mbechler
 */
package eu.agno3.fileshare.service.archive.internal;


import java.io.IOException;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public interface ArchiveContext extends AutoCloseable {

    /**
     * 
     * @return the archive type
     */
    ArchiveType getArchiveType ();


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws IOException;


    /**
     * @param path
     * @param v
     * @param e
     * @param compress
     * @throws IOException
     * @throws FileshareException
     */
    void addEntry ( String path, VFSContext v, VFSFileEntity e, boolean compress ) throws IOException, FileshareException;
}
