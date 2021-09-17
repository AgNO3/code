/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.vfs.fs;


import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.exceptions.UnsupportedOperationException;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.MappedVFSEntity;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSContainerEntityImpl;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSEntityImpl;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.VFSFileEntityImpl;
import eu.agno3.fileshare.service.vfs.AbstractVFSContext;
import eu.agno3.fileshare.vfs.RequestRange;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;


/**
 * @author mbechler
 *
 */
public class FilesystemVFSContext extends AbstractVFSContext<FilesystemVFS> implements VFSContext {

    private static final Logger log = Logger.getLogger(FilesystemVFSContext.class);

    private static Class<?> FILE_KEY_CLASS;
    private static Field DEV_FIELD;
    private static Field INODE_FIELD;

    static {
        try {
            FILE_KEY_CLASS = FilesystemVFSContext.class.getClassLoader().loadClass("sun.nio.fs.UnixFileKey"); //$NON-NLS-1$
            DEV_FIELD = FILE_KEY_CLASS.getDeclaredField("st_dev"); //$NON-NLS-1$
            DEV_FIELD.setAccessible(true);
            INODE_FIELD = FILE_KEY_CLASS.getDeclaredField("st_ino"); //$NON-NLS-1$
            INODE_FIELD.setAccessible(true);
        }
        catch (
            ClassNotFoundException |
            NoSuchFieldException |
            SecurityException e ) {
            log.error("Failed to get file inode attributes"); //$NON-NLS-1$
        }
    }

    private final Path root;


    /**
     * @param ets
     * @param roTransaction
     * @param root
     * @param vfs
     * @throws EntityTransactionException
     */
    public FilesystemVFSContext ( EntityTransactionService ets, boolean roTransaction, Path root, FilesystemVFS vfs )
            throws EntityTransactionException {
        super(vfs, ets, roTransaction);
        this.root = root;
    }


    /**
     * @param v
     * @param ets
     * @param root
     * 
     */
    public FilesystemVFSContext ( FilesystemVFS v, EntityTransactionContext ets, Path root ) {
        super(v, ets);
        this.root = root;
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#load(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public VFSEntity load ( EntityKey k ) throws FileshareException {
        try {
            Path p = loadEntity(k);
            if ( p == null ) {
                return null;
            }

            return makeEntity(p, getRelativePath(k));
        }
        catch ( IOException e ) {
            throw handleException(k, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#load(eu.agno3.fileshare.model.EntityKey, java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends VFSEntity> @Nullable T load ( EntityKey k, Class<T> type ) throws FileshareException {
        try {
            Path p = loadEntity(k);

            if ( p == null ) {
                return null;
            }

            String relative = getRelativePath(k);

            boolean isDir = Files.isDirectory(p);
            boolean isFile = Files.isRegularFile(p);
            if ( ( VFSContainerEntity.class.isAssignableFrom(type) ) && isDir ) {
                return (@Nullable T) makeContainerEntity(p, relative);
            }
            else if ( VFSFileEntity.class.isAssignableFrom(type) && isFile ) {
                return (@Nullable T) makeFileEntity(p, relative);
            }
            else if ( VFSEntity.class.equals(type) ) {
                return (@Nullable T) makeEntity(p, relative);
            }
            else {
                return null;
            }
        }
        catch ( IOException e ) {
            throw handleException(k, e);
        }
    }


    /**
     * @param p
     * @param relative
     * @return
     * @throws IOException
     */
    private VFSEntity makeEntity ( Path p, String relative ) {
        try {
            if ( Files.isDirectory(p) ) {
                return makeContainerEntity(p, relative);
            }
            else if ( Files.isRegularFile(p) ) {
                return makeFileEntity(p, relative);
            }
            else {
                return null;
            }
        }
        catch ( IOException e ) {
            log.error("Failed to read VFS file", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param k
     * @return
     * @throws IOException
     * @throws eu.agno3.fileshare.exceptions.UnsupportedOperationException
     */
    private Path loadEntity ( EntityKey k ) throws IOException, UnsupportedOperationException {
        Path resolve = extractPath(k);
        if ( !Files.exists(resolve) ) {
            return null;
        }
        Path realPath = resolve.toRealPath();
        checkTraversal(realPath);
        return realPath;
    }


    /**
     * @param k
     * @return
     * @throws IOException
     * @throws eu.agno3.fileshare.exceptions.UnsupportedOperationException
     */
    protected Path extractPath ( EntityKey k ) throws IOException, UnsupportedOperationException {
        String relativePath = getRelativePath(k);
        if ( StringUtils.isBlank(relativePath) ) {
            return this.root;
        }
        return this.root.resolve(relativePath);
    }


    /**
     * @param realPath
     * @throws IOException
     */
    private void checkTraversal ( Path realPath ) throws IOException {
        if ( !realPath.startsWith(this.root) ) {
            throw new IOException("Path traversal is forbidden " + realPath); //$NON-NLS-1$
        }
    }


    /**
     * @param p
     * @return
     * @throws IOException
     */
    private @Nullable VFSFileEntity makeFileEntity ( Path p, String relative ) throws IOException {
        VFSFileEntityImpl v = new VFSFileEntityImpl(relative, getVfs().getGroup(), getVfs().isReadOnly());
        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
        setupCommon(p, relative, v, attrs);
        v.setContentType(getVfs().detectFileType(v));
        refreshFile(p, v, attrs);
        return v;

    }


    /**
     * @param p
     * @param v
     * @throws IOException
     */
    private void refreshFile ( Path p, VFSFileEntityImpl v, BasicFileAttributes attrs ) throws IOException {
        refreshCommon(p, v, attrs);
        v.setFileSize(Files.size(p));
    }


    /**
     * @param p
     * @return
     * @throws IOException
     */
    private @Nullable VFSContainerEntity makeContainerEntity ( Path p, String relative ) throws IOException {
        VFSContainerEntityImpl v = new VFSContainerEntityImpl(relative, getVfs().getGroup(), getVfs().isReadOnly());
        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
        setupCommon(p, relative, v, attrs);
        refreshContainer(p, v, attrs);
        return v;
    }


    /**
     * @param p
     * @param v
     * @throws IOException
     */
    private void refreshContainer ( Path p, VFSContainerEntityImpl v, BasicFileAttributes attrs ) throws IOException {
        refreshCommon(p, v, attrs);
        Stream<Path> filter = Files.list(p).filter(makeFilesFilter());
        long numFiles = filter.count();
        v.setNumChildren(numFiles);
        v.setEmpty(numFiles > 0);
    }


    /**
     * @param p
     * @param v
     * @param attrs
     */
    private void setupCommon ( Path p, String relative, VFSEntityImpl v, BasicFileAttributes attrs ) {
        if ( !StringUtils.isBlank(relative) ) {
            v.setHaveParent(true);
            int sep = relative.lastIndexOf('/');
            if ( sep < 0 ) {
                v.setLocalName(relative);
            }
            else {
                v.setLocalName(relative.substring(sep + 1));
            }
        }
        else {
            v.setHaveParent(false);
        }
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Relative path is '%s' for '%s'", relative, p)); //$NON-NLS-1$
        }
        v.setOwner(getVfs().getGroup());
        v.setSecurityLabel(getVfs().getLabel());
        v.setInode(makeInode(attrs));
    }


    /**
     * @param attrs
     * @return
     */
    private static byte[] makeInode ( BasicFileAttributes attrs ) {
        Object fileKey = attrs.fileKey();
        if ( fileKey != null && FILE_KEY_CLASS.isAssignableFrom(fileKey.getClass()) ) {

            try {
                long dev = DEV_FIELD.getLong(fileKey);
                long inode = INODE_FIELD.getLong(fileKey);
                return new byte[] {
                    (byte) ( dev >> 56 ), (byte) ( dev >> 48 ), (byte) ( dev >> 40 ), (byte) ( dev >> 32 ), (byte) ( dev >> 24 ),
                    (byte) ( dev >> 16 ), (byte) ( dev >> 8 ), (byte) dev, (byte) ( inode >> 56 ), (byte) ( inode >> 48 ), (byte) ( inode >> 40 ),
                    (byte) ( inode >> 32 ), (byte) ( inode >> 24 ), (byte) ( inode >> 16 ), (byte) ( inode >> 8 ), (byte) inode,
                };
            }
            catch (
                IllegalArgumentException |
                IllegalAccessException e ) {
                log.error("Failed to extract inode", e); //$NON-NLS-1$
            }
        }

        return null;
    }


    /**
     * @param p
     * @param v
     * @throws IOException
     */
    private void refreshCommon ( Path p, VFSEntityImpl v, BasicFileAttributes attrs ) throws IOException {
        v.setCreated(new DateTime(attrs.creationTime().toMillis()));
        v.setLastModified(new DateTime(Files.getLastModifiedTime(p).toMillis()));
        v.setLocalValidGrants(Collections.EMPTY_SET);
        v.setHaveGrants(false);
        if ( v.hasParent() ) {
            ContentEntity mapped = this.findMappedEntity(v);

            if ( mapped instanceof MappedVFSEntity ) {
                v.setLocalValidGrants( ( (MappedVFSEntity) mapped ).getOriginalLocalValidGrants());
                v.setHaveGrants(!mapped.getGrants().isEmpty());
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getRoot()
     */
    @Override
    public VFSContainerEntity getRoot () throws FileshareException {
        try {
            return makeContainerEntity(this.root, StringUtils.EMPTY);
        }
        catch ( IOException e ) {
            throw handleException(null, e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getChildren(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public Set<? extends VFSEntity> getChildren ( VFSContainerEntity parent ) throws FileshareException {
        if ( ! ( parent instanceof VFSEntityImpl ) ) {
            throw new UnsupportedOperationException();
        }

        try {
            String base = getRelativePath(parent.getEntityKey());
            Path resolve = this.root.resolve(base);
            checkTraversal(resolve);
            return Files.list(resolve).filter(makeFilesFilter())
                    .map(p -> makeEntity(p, ( !StringUtils.isBlank(base) ? base + "/" : StringUtils.EMPTY ) + p.getFileName().toString())) //$NON-NLS-1$
                    .collect(Collectors.toSet());
        }
        catch ( IOException e ) {
            throw handleException(parent.getEntityKey(), e);
        }

    }


    static Predicate<? super Path> makeFilesFilter () {
        return p -> Files.isReadable(p) && ( Files.isDirectory(p) || Files.isRegularFile(p) );
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getParent(eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public VFSContainerEntity getParent ( VFSEntity e ) throws FileshareException {
        if ( ! ( e instanceof VFSEntityImpl ) ) {
            throw new UnsupportedOperationException();
        }
        VFSEntityImpl ve = (VFSEntityImpl) e;

        try {
            Path path = getAbsolutePath(ve);
            if ( path.equals(this.root) ) {
                return null;
            }
            Path parentPath = path.getParent();

            Path relativize = this.root.relativize(parentPath);
            String relative = relativize.toString();

            if ( StringUtils.isBlank(relative) ) {
                return getRoot();
            }

            checkTraversal(parentPath);
            return makeContainerEntity(parentPath, relative);
        }
        catch ( IOException ex ) {
            throw handleException(e.getEntityKey(), ex);
        }
    }


    /**
     * @param ve
     * @return
     * @throws IOException
     * @throws eu.agno3.fileshare.exceptions.UnsupportedOperationException
     */
    private Path getAbsolutePath ( VFSEntityImpl ve ) throws IOException, UnsupportedOperationException {
        checkSameVFS(ve.getEntityKey());
        String relative = ve.getRelativePath();
        if ( StringUtils.isEmpty(relative) ) {
            return this.root;
        }
        Path p = this.root.resolve(relative);
        checkTraversal(p);
        return p;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#resolveRelative(eu.agno3.fileshare.model.VFSContainerEntity,
     *      java.lang.String[])
     */
    @Override
    public VFSEntity resolveRelative ( VFSContainerEntity e, String[] relativeSegments ) throws FileshareException {
        if ( ! ( e instanceof VFSEntityImpl ) ) {
            throw new UnsupportedOperationException();
        }
        VFSEntityImpl ve = (VFSEntityImpl) e;

        if ( relativeSegments == null || relativeSegments.length == 0 ) {
            return ve;
        }

        try {
            String relative = ( !StringUtils.isBlank(ve.getRelativePath()) ? ve.getRelativePath() + "/" : StringUtils.EMPTY ) + //$NON-NLS-1$
                    StringUtils.join(relativeSegments, '/');
            Path p = this.root.resolve(relative);
            checkTraversal(p);

            return makeEntity(p, relative);
        }
        catch ( IOException ex ) {
            throw handleException(e.getEntityKey(), ex);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#canResolveByName()
     */
    @Override
    public boolean canResolveByName () {
        return true;
    }


    /**
     * @param e
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    protected void refreshInternal ( VFSEntityImpl e ) throws IOException, UnsupportedOperationException {
        Path p = getAbsolutePath(e);
        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
        if ( e instanceof VFSContainerEntityImpl ) {
            refreshContainer(p, (VFSContainerEntityImpl) e, attrs);
        }
        else if ( e instanceof VFSFileEntityImpl ) {
            refreshFile(p, (VFSFileEntityImpl) e, attrs);
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getContents(eu.agno3.fileshare.model.VFSFileEntity,
     *      eu.agno3.fileshare.vfs.RequestRange)
     */
    @Override
    public VFSContentHandle getContents ( VFSFileEntity file, RequestRange r ) throws FileshareException {
        if ( ! ( file instanceof VFSFileEntityImpl ) ) {
            throw new UnsupportedOperationException();
        }

        try {
            return new SeekableChannelContentHandle(FileChannel.open(this.getAbsolutePath((VFSEntityImpl) file), StandardOpenOption.READ));
        }
        catch ( IOException e ) {
            throw new StorageException("Failed to get file contents", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#checkFreeSpace(long, long)
     */
    @Override
    public void checkFreeSpace ( long neededSize, long temporarySize ) throws InsufficentStorageSpaceException {

        try {
            if ( Files.getFileStore(this.root).getUsableSpace() < neededSize ) {
                throw new InsufficentStorageSpaceException();
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to check free space", e); //$NON-NLS-1$
        }
    }

}
