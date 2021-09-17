/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UploadException;
import eu.agno3.fileshare.vfs.VFSStoreHandle;

import jcifs.CIFSException;
import jcifs.SmbResource;


/**
 * @author mbechler
 *
 */
public class SMBVFSStoreHandle implements VFSStoreHandle {

    private static final Logger log = Logger.getLogger(SMBVFSStoreHandle.class);
    private final long size;
    private final SmbResource resource;
    private final SmbResource backup;
    private final SmbResource originalLocation;


    /**
     * @param size
     * @param resource
     * @param originalLocation
     * @param bak
     */
    public SMBVFSStoreHandle ( long size, SmbResource resource, SmbResource originalLocation, SmbResource bak ) {
        this.size = size;
        this.resource = resource;
        this.originalLocation = originalLocation;
        this.backup = bak;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSStoreHandle#getLength()
     */
    @Override
    public long getLength () {
        return this.size;
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
                this.resource.renameTo(this.originalLocation);
            }
            catch ( IOException e ) {
                log.warn("Failed to revert data move", e); //$NON-NLS-1$
                last = new UploadException("Failed to revert data move", e); //$NON-NLS-1$
            }
        }
        else {
            try {
                this.resource.delete();
            }
            catch ( IOException e ) {
                log.warn("Failed to remove broken upload", e); //$NON-NLS-1$
                last = new UploadException("Failed to remove broken upload", e); //$NON-NLS-1$
            }
        }

        if ( this.backup != null ) {
            try {
                this.backup.renameTo(this.resource);
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
        if ( this.backup != null ) {
            try {
                this.backup.delete();
            }
            catch ( CIFSException e ) {
                log.warn("Failed to remove backup", e); //$NON-NLS-1$
            }
            this.backup.close();
        }
    }

}
