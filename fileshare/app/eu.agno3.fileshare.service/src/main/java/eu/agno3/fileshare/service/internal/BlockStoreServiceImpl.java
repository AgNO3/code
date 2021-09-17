/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.StorageEntityNotFoundException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.exceptions.StoreFailedException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.api.internal.BlockStorageService;
import eu.agno3.fileshare.service.vfs.fs.SeekableChannelContentHandle;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSStoreHandle;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = BlockStorageService.class, configurationPid = BlockStoreServiceImpl.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class BlockStoreServiceImpl implements BlockStorageService {

    private static final Logger log = Logger.getLogger(BlockStoreServiceImpl.class);

    /**
     * 
     */
    public static final String PID = "blockstore"; //$NON-NLS-1$

    private static final String DEFAULT_BLOCKSTORE_PATH = "/srv/fileshare/files/"; //$NON-NLS-1$

    private Path basePath;
    private GroupPrincipal storageGroup;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        String pathSpec = ConfigUtil.parseString(ctx.getProperties(), "storagePath", DEFAULT_BLOCKSTORE_PATH); //$NON-NLS-1$
        Path p = Paths.get(pathSpec.trim());

        if ( !Files.exists(p) ) {
            try {
                Files.createDirectories(p);
            }
            catch ( IOException e ) {
                log.error("Failed to create directory", e); //$NON-NLS-1$
            }
        }

        this.basePath = p;

        String storageGroupSpec = ConfigUtil.parseString(ctx.getProperties(), "storageGroup", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(storageGroupSpec) ) {
            try {
                this.storageGroup = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(storageGroupSpec);
            }
            catch ( IOException e ) {
                log.error("Failed to get storage group", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param entity
     * @return
     */
    private Path makeEntityPath ( VFSFileEntity entity ) {
        return getRootStoragePath().resolve(entity.getEntityKey().toString());
    }


    /**
     * @return
     */
    private Path getRootStoragePath () {
        return this.basePath;
    }


    @Override
    public VFSContentHandle getContents ( VFSFileEntity entity ) throws StorageException {
        Path entityPath = this.makeEntityPath(entity);
        try {
            // may need some locking, if partial writes were implemented
            return new SeekableChannelContentHandle(FileChannel.open(entityPath, StandardOpenOption.READ));
        }
        catch ( IOException e ) {
            throw new StorageEntityNotFoundException(e);
        }
    }


    @Override
    public VFSStoreHandle storeContents ( VFSFileEntity f, InputBuffer data ) throws StorageException {
        if ( ! ( data instanceof FileInputBuffer ) ) {
            throw new StoreFailedException("Unsupport input buffer"); //$NON-NLS-1$
        }
        FileInputBuffer fib = (FileInputBuffer) data;
        Path entityPath = this.makeEntityPath(f);
        try {
            fib.move(entityPath, true, this.storageGroup);
            return new BlockStoreVFSStoreHandle(data.getSize(), entityPath, extractOriginalPath(data), null);
        }
        catch ( IOException e ) {
            throw new StoreFailedException(e);
        }
    }


    /**
     * @param data
     * @return the path to a file based buffer
     */
    private static Path extractOriginalPath ( InputBuffer data ) {
        if ( data instanceof FileInputBuffer ) {
            return ( (FileInputBuffer) data ).getPath();
        }
        return null;
    }


    @Override
    public VFSStoreHandle replaceContents ( VFSFileEntity f, InputBuffer data ) throws StoreFailedException {
        if ( ! ( data instanceof FileInputBuffer ) ) {
            throw new StoreFailedException("Unsupport input buffer"); //$NON-NLS-1$
        }
        FileInputBuffer fib = (FileInputBuffer) data;
        Path entityPath = this.makeEntityPath(f);
        try {
            // unfortunately we cannot do this in an atomic fashion
            // 1. move current data to backup file (no replace, so this will fail if there is a concurrent upload)
            // 2. move input data to it's final location
            // on failure: move input data back to it's source, move backup file to current data
            // on success: remove backup file and be done
            Path bakPath = entityPath.resolveSibling("._bak_" + entityPath.getFileName().toString()); //$NON-NLS-1$
            Files.move(entityPath, bakPath, StandardCopyOption.ATOMIC_MOVE);
            fib.move(entityPath, false, this.storageGroup);
            return new BlockStoreVFSStoreHandle(data.getSize(), entityPath, extractOriginalPath(data), bakPath);
        }
        catch ( IOException e ) {
            throw new StoreFailedException(e);
        }
    }


    @Override
    public void removeContents ( VFSFileEntity f ) {
        Path file = makeEntityPath(f);
        try {
            Files.delete(file);
        }
        catch ( IOException e ) {
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
            FileStore fileStore = Files.getFileStore(this.basePath);
            long usableSpace = fileStore.getUsableSpace();

            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Storage: have %d need %d, temporary: need %d", //$NON-NLS-1$
                    usableSpace,
                    neededSize,
                    temporarySize));
            }

            if ( usableSpace < neededSize ) {
                throw new InsufficentStorageSpaceException("Insufficient storage space"); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.error("Failed to check free space", e); //$NON-NLS-1$
        }
    }
}
