/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 9, 2016 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UploadException;
import eu.agno3.fileshare.vfs.VFSStoreHandle;


/**
 * @author mbechler
 *
 */
public class BlockStoreVFSStoreHandle implements VFSStoreHandle {

    private static final Logger log = Logger.getLogger(BlockStoreVFSStoreHandle.class);
    private final long written;
    private final Path entityPath;
    private final Path originalLocation;
    private final Path bakPath;


    /**
     * @param written
     * @param entityPath
     * @param originalLocation
     * @param bakPath
     */
    public BlockStoreVFSStoreHandle ( long written, Path entityPath, Path originalLocation, Path bakPath ) {
        this.written = written;
        this.entityPath = entityPath;
        this.originalLocation = originalLocation;
        this.bakPath = bakPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSStoreHandle#getLength()
     */
    @Override
    public long getLength () {
        return this.written;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSStoreHandle#revert()
     */
    @Override
    public void revert () throws FileshareException {
        FileshareException last = null;
        if ( this.originalLocation != null ) {
            try {
                Files.move(this.entityPath, this.originalLocation, StandardCopyOption.ATOMIC_MOVE);
            }
            catch ( IOException e ) {
                log.warn("Failed to revert data move", e); //$NON-NLS-1$
                last = new UploadException("Failed to revert data move", e); //$NON-NLS-1$
            }
        }

        if ( this.bakPath != null ) {
            try {
                Files.move(this.bakPath, this.entityPath, StandardCopyOption.ATOMIC_MOVE);
            }
            catch ( IOException e ) {
                log.warn("Failed to restore replaced data", e); //$NON-NLS-1$
                last = new UploadException("Failed to restore replaced data", e); //$NON-NLS-1$
            }
        }

        if ( last != null ) {
            throw last;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSStoreHandle#commit()
     */
    @Override
    public void commit () {
        if ( this.bakPath != null ) {
            try {
                Files.deleteIfExists(this.bakPath);
            }
            catch ( IOException e ) {
                log.warn("Failed to delete backup", e); //$NON-NLS-1$
            }
        }
    }

}
