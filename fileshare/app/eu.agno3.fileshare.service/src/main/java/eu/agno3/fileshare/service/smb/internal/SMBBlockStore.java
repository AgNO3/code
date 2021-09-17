/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 3, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.StorageEntityNotFoundException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.exceptions.StoreFailedException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.api.internal.BlockStorageService;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSStoreHandle;

import jcifs.CIFSException;
import jcifs.SmbResource;


/**
 * @author mbechler
 *
 */
@Component ( service = BlockStorageService.class, configurationPid = "blockstore.smb", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SMBBlockStore implements BlockStorageService {

    private static final Logger log = Logger.getLogger(SMBBlockStore.class);

    private SMBClientProvider clientProvider;


    @Reference
    protected synchronized void setSMBClientProvider ( SMBClientProvider scp ) {
        this.clientProvider = scp;
    }


    protected synchronized void unsetSMBClientProvider ( SMBClientProvider scp ) {
        if ( this.clientProvider == scp ) {
            this.clientProvider = null;
        }
    }


    /**
     * @return
     */
    private SmbResource getRoot () {
        return this.clientProvider.getRoot();
    }


    /**
     * @param entity
     * @return
     * @throws StorageException
     */
    private SmbResource getResource ( VFSFileEntity entity ) throws StorageException {
        try {
            return getRoot().resolve(entity.getEntityKey().toString());
        }
        catch ( CIFSException e ) {
            throw new StorageException("Invalid URL", e); //$NON-NLS-1$
        }
    }


    @Override
    public VFSContentHandle getContents ( VFSFileEntity entity ) throws StorageException {
        try ( SmbResource r = getResource(entity) ) {
            return new SMBStreamContentHandle(r.openInputStream());
        }
        catch ( IOException e ) {
            throw new StorageEntityNotFoundException(e);
        }
    }


    @Override
    public VFSStoreHandle storeContents ( VFSFileEntity f, InputBuffer data ) throws StorageException {
        try ( SmbResource r = getResource(f) ) {
            long size = data.getSize();
            if ( data instanceof SMBInputBuffer ) {
                SMBInputBuffer sib = (SMBInputBuffer) data;
                try ( SmbResource dataFile = sib.getDataFile() ) {
                    dataFile.renameTo(r, false);
                    return new SMBVFSStoreHandle(size, r, dataFile, null);
                }
            }

            try ( SeekableByteChannel ch = data.getStream();
                  InputStream in = Channels.newInputStream(ch);
                  OutputStream out = r.openOutputStream() ) {
                if ( IOUtils.copyLarge(in, out) != size ) {
                    throw new StorageException("Size mismatch"); //$NON-NLS-1$
                }
                return new SMBVFSStoreHandle(size, r, null, null);
            }

        }
        catch ( IOException e ) {
            throw new StoreFailedException("Failed to store file contents", e); //$NON-NLS-1$
        }
    }


    @Override
    public VFSStoreHandle replaceContents ( VFSFileEntity f, InputBuffer data ) throws StorageException {
        try ( SmbResource r = getResource(f);
              SmbResource bak = getRoot().resolve("._bak_" + r.getName()) ) { //$NON-NLS-1$
            long size = data.getSize();
            try {
                r.renameTo(bak);
            }
            catch ( IOException e ) {
                log.warn("Failed to create backup of " + r, e); //$NON-NLS-1$
            }
            if ( data instanceof SMBInputBuffer ) {
                SMBInputBuffer sib = (SMBInputBuffer) data;
                try ( SmbResource dataFile = sib.getDataFile() ) {
                    dataFile.renameTo(r, true);
                    return new SMBVFSStoreHandle(size, r, dataFile, bak);
                }
            }

            try ( SeekableByteChannel ch = data.getStream();
                  InputStream in = Channels.newInputStream(ch);
                  OutputStream out = r.openOutputStream() ) {
                if ( IOUtils.copyLarge(in, out) != size ) {
                    throw new StorageException("Size mismatch"); //$NON-NLS-1$
                }
                return new SMBVFSStoreHandle(size, r, null, bak);
            }
        }
        catch ( IOException e ) {
            throw new StoreFailedException("Failed to replace file contents", e); //$NON-NLS-1$
        }
    }


    @Override
    public void removeContents ( VFSFileEntity f ) {
        try ( SmbResource r = getResource(f) ) {
            r.delete();
        }
        catch (
            IOException |
            StorageException e ) {
            log.error("Failed to delete file", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InsufficentStorageSpaceException
     *
     * @see eu.agno3.fileshare.service.api.internal.BlockStorageService#checkFreeSpace(long, long)
     */
    @Override
    public void checkFreeSpace ( long neededSize, long temporarySize ) throws InsufficentStorageSpaceException {
        try {
            long free = getRoot().getDiskFreeSpace();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Storage: have %d need %d, temporary: need %d", //$NON-NLS-1$
                    free,
                    neededSize,
                    temporarySize));
            }

            if ( free < neededSize ) {
                throw new InsufficentStorageSpaceException("Insufficient storage space"); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.error("Failed to check free space", e); //$NON-NLS-1$
        }
    }

}
