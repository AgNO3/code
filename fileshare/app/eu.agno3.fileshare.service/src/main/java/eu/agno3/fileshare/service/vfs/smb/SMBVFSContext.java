/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2016 by mbechler
 */
package eu.agno3.fileshare.service.vfs.smb;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.MappedVFSEntity;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSContainerEntityImpl;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSEntityImpl;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.VFSFileEntityImpl;
import eu.agno3.fileshare.service.smb.internal.SMBStreamContentHandle;
import eu.agno3.fileshare.service.vfs.AbstractVFSContext;
import eu.agno3.fileshare.vfs.RequestRange;
import eu.agno3.fileshare.vfs.VFSChange;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.util.iter.ClosableIterator;

import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.ResourceFilter;
import jcifs.RuntimeCIFSException;
import jcifs.SmbResource;


/**
 * @author mbechler
 *
 */
public class SMBVFSContext extends AbstractVFSContext<SMBVFS> implements VFSContext {

    private static final Logger log = Logger.getLogger(SMBVFSContext.class);

    private URL root;
    private String rootString;


    /**
     * @param ets
     * @param readOnly
     * @param root
     * @param smbvfs
     * @throws EntityTransactionException
     */
    public SMBVFSContext ( EntityTransactionService ets, boolean readOnly, URL root, SMBVFS smbvfs ) throws EntityTransactionException {
        super(smbvfs, ets, readOnly);
        this.root = root;
        this.rootString = root.toString();
    }


    /**
     * @param ets
     * @param smbvfs
     * @param root
     * 
     */
    public SMBVFSContext ( EntityTransactionContext ets, SMBVFS smbvfs, URL root ) {
        super(smbvfs, ets);
        this.root = root;
        this.rootString = root.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#load(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public VFSEntity load ( EntityKey k ) throws FileshareException {
        String relative = getRelativePath(k);
        try ( SmbResource file = toFile(toURL(relative)) ) {
            return toEntity(file, relative);
        }
        catch ( IOException e ) {
            throw handleException(k, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#load(eu.agno3.fileshare.model.EntityKey, java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends VFSEntity> @Nullable T load ( EntityKey k, Class<T> type ) throws FileshareException {
        String relative = getRelativePath(k);
        try ( SmbResource p = toFile(toURL(relative)) ) {
            if ( ( VFSContainerEntity.class.isAssignableFrom(type) ) && p.isDirectory() ) {
                return (@Nullable T) makeContainerEntity(p, relative);
            }
            else if ( VFSFileEntity.class.isAssignableFrom(type) && p.isFile() ) {
                return (@Nullable T) makeFileEntity(p, relative);
            }
            else if ( VFSEntity.class.equals(type) ) {
                return (@Nullable T) toEntity(p, relative);
            }

            return null;
        }
        catch ( IOException e ) {
            throw handleException(k, e);
        }
    }


    /**
     * @param file
     * @return
     * @throws IOException
     * @throws CIFSException
     * @throws FileshareException
     */
    private VFSEntity toEntity ( SmbResource file, String relative ) throws CIFSException, IOException, FileshareException {
        if ( file.isDirectory() ) {
            String normalized = relative;
            if ( !StringUtils.isEmpty(normalized) && normalized.charAt(normalized.length() - 1) != '/' ) {
                normalized = normalized + '/';
            }
            return makeContainerEntity(file, normalized);
        }
        else if ( file.isFile() ) {
            return makeFileEntity(file, relative);
        }
        throw new FileshareException("Unsupported file type " + file.getLocator().getURL()); //$NON-NLS-1$
    }


    /**
     * @param p
     * @return
     * @throws IOException
     * @throws FileshareException
     */
    private @Nullable VFSFileEntity makeFileEntity ( SmbResource p, String relative ) throws IOException, FileshareException {
        if ( !p.isFile() ) {
            throw new FileshareException("Not a file " + p.getLocator().getURL()); //$NON-NLS-1$
        }
        VFSFileEntityImpl v = new VFSFileEntityImpl(relative, getVfs().getGroup(), getVfs().isReadOnly());
        setupCommon(p, relative, v);
        v.setContentType(getVfs().detectFileType(v));
        refreshFile(p, v);
        return v;

    }


    /**
     * @param p
     * @param v
     * @throws IOException
     */
    private void refreshFile ( SmbResource p, VFSFileEntityImpl v ) throws IOException {
        refreshCommon(p, v);
        v.setFileSize(p.length());
    }


    /**
     * @param p
     * @return
     * @throws IOException
     * @throws FileshareException
     */
    private @Nullable VFSContainerEntity makeContainerEntity ( SmbResource p, String relative ) throws IOException, FileshareException {
        if ( !p.isDirectory() ) {
            throw new FileshareException("Not a directory " + p.getLocator().getURL()); //$NON-NLS-1$
        }
        VFSContainerEntityImpl v = new VFSContainerEntityImpl(relative, getVfs().getGroup(), getVfs().isReadOnly());
        setupCommon(p, relative, v);
        refreshContainer(p, v);
        return v;
    }


    /**
     * @param p
     * @param v
     * @throws IOException
     */
    private void refreshContainer ( SmbResource p, VFSContainerEntityImpl v ) throws IOException {
        refreshCommon(p, v);

        // TODO: maybe this could be optimized
        v.setEmpty(false);
        // v.setNumChildren(numChildren);
    }


    /**
     * @param p
     * @param v
     * @throws IOException
     */
    private void refreshCommon ( SmbResource p, VFSEntityImpl v ) throws IOException {
        v.setCreated(new DateTime(p.createTime()));
        if ( p.lastModified() != 0 ) {
            v.setLastModified(new DateTime(p.lastModified()));
        }
        v.setLocalValidGrants(Collections.EMPTY_SET);
        v.setHaveGrants(false);
        v.setSharable(this.getVfs().isSharable());
        if ( v.hasParent() ) {
            ContentEntity mapped = this.findMappedEntity(v);
            if ( mapped instanceof MappedVFSEntity ) {
                v.setLocalValidGrants( ( (MappedVFSEntity) mapped ).getOriginalLocalValidGrants());
                v.setHaveGrants(!mapped.getGrants().isEmpty());
            }
        }
    }


    /**
     * @param p
     * @param v
     * @param attrs
     * @throws CIFSException
     * @throws FileshareException
     */
    private void setupCommon ( SmbResource p, String relative, VFSEntityImpl v ) throws CIFSException, FileshareException {
        if ( !StringUtils.isBlank(relative) ) {
            v.setHaveParent(true);
            String name = p.getName();
            if ( !StringUtils.isBlank(name) && name.charAt(name.length() - 1) == '/' ) {
                name = name.substring(0, name.length() - 1);
            }
            v.setLocalName(name);
        }
        else {
            v.setHaveParent(false);
        }
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Relative path is '%s' for '%s'", relative, p)); //$NON-NLS-1$
        }
        v.setOwner(getVfs().getGroup());
        v.setSecurityLabel(getVfs().getLabel());

        v.setInode(makeInode(p));
    }


    /**
     * @param p
     * @return
     * @throws CIFSException
     * @throws FileshareException
     */
    private static byte[] makeInode ( SmbResource p ) throws CIFSException, FileshareException {
        long idx = p.fileIndex();
        if ( idx == 0 ) {
            try {
                // need to normalize this here as we have to do lookups not knowing whether
                // that's a file or directory.
                String path = p.getLocator().getPath();
                if ( path.charAt(path.length() - 1) == '/' ) {
                    path = path.substring(0, path.length() - 1);
                }
                if ( log.isTraceEnabled() ) {
                    log.trace("Generating inode from path " + path); //$NON-NLS-1$
                }
                return MessageDigest.getInstance("MD5").digest(path.getBytes()); //$NON-NLS-1$
            }
            catch ( NoSuchAlgorithmException e ) {
                throw new FileshareException("Failed to generate path digest", e); //$NON-NLS-1$
            }
        }
        return new byte[] {
            0, 0, 0, 0, //
            0, 0, 0, 0, //
            (byte) ( idx >> 7 ), (byte) ( idx >> 6 ), (byte) ( idx >> 5 ), (byte) ( idx >> 4 ), //
            (byte) ( idx >> 3 ), (byte) ( idx >> 2 ), (byte) ( idx >> 1 ), (byte) idx
        };
    }


    /**
     * @param k
     * @param url
     * @return
     * @throws FileshareException
     */
    private SmbResource toFile ( URL url ) throws FileshareException {
        try {
            SmbResource cached = this.getVfs().getCached(url);
            if ( cached != null ) {
                return cached;
            }
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Opening %s ( ctx %s )", url, this)); //$NON-NLS-1$
            }
            URLConnection fileConn = url.openConnection();
            if ( ! ( fileConn instanceof SmbResource ) ) {
                throw new FileshareException("openConnection() did not return a SmbResource object"); //$NON-NLS-1$
            }

            SmbResource f = (SmbResource) fileConn;
            if ( !f.getLocator().getURL().toString().startsWith(this.rootString) ) {
                throw new IOException("Path traversal"); //$NON-NLS-1$
            }
            this.getVfs().putCache(url, f);
            return f;
        }
        catch (
            IOException |
            RuntimeCIFSException e ) {
            throw handleException(null, e);
        }
    }


    /**
     * @param k
     * @throws FileshareException
     */
    private URL toURL ( String relative ) throws FileshareException {
        try {
            checkPath(relative);
            String path = this.root.getPath();
            if ( !StringUtils.isBlank(relative) ) {
                path += relative;
            }
            return new URL("smb", this.root.getHost(), 0, path, getVfs().getContext().getUrlHandler()); //$NON-NLS-1$
        }
        catch ( MalformedURLException e ) {
            throw new FileshareException("Illegal URL", e); //$NON-NLS-1$
        }
    }


    /**
     * @param relative
     * @throws FileshareException
     */
    private static void checkPath ( String relative ) throws FileshareException {
        if ( relative == null ) {
            return;
        }

        for ( char c : relative.toCharArray() ) {
            if ( c == 0 || c == ':' ) {
                throw new FileshareException("Path contains invalid characters"); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getRoot()
     */
    @Override
    public VFSContainerEntity getRoot () throws FileshareException {
        try ( SmbResource r = toFile(this.root) ) {
            return makeContainerEntity(r, null);
        }
        catch ( IOException e ) {
            throw handleException(null, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getChildren(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public Set<? extends VFSEntity> getChildren ( VFSContainerEntity parent ) throws FileshareException {
        EntityKey k = parent.getEntityKey();
        String base = getRelativePath(k);
        try ( SmbResource f = toFile(toURL(base)) ) {
            Set<VFSEntity> children = new HashSet<>();

            try ( CloseableIterator<SmbResource> it = f.children(makeFileFiter()) ) {
                while ( it.hasNext() ) {
                    try ( SmbResource c = it.next() ) {
                        children.add(toEntity(c, ( !StringUtils.isBlank(base) ? base + "/" : StringUtils.EMPTY ) + c.getName() + //$NON-NLS-1$
                                ( c.isDirectory() ? "/" : StringUtils.EMPTY ))); //$NON-NLS-1$
                    }
                }
            }
            return children;
        }
        catch ( IOException e ) {
            throw handleException(k, e);
        }
    }


    /**
     * @return
     */
    private static ResourceFilter makeFileFiter () {
        return new ResourceFilter() {

            @Override
            public boolean accept ( SmbResource file ) throws CIFSException {
                return file.isDirectory() || file.isFile();
            }
        };
    }


    /**
     * @param ve
     * @return
     * @throws IOException
     * @throws FileshareException
     */
    private URL getURL ( VFSEntityImpl ve ) throws IOException, FileshareException {
        checkSameVFS(ve.getEntityKey());
        String relative = ve.getRelativePath();
        if ( StringUtils.isEmpty(relative) ) {
            return this.root;
        }

        return toURL(relative);
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
            URL url = getURL(ve);

            if ( url.equals(this.root) ) {
                return null;
            }

            try ( SmbResource path = toFile(url) ) {
                URI parentURL = new URI(path.getLocator().getParent());

                if ( !parentURL.toString().startsWith(this.rootString) ) {
                    throw new IOException("Path traversal"); //$NON-NLS-1$
                }

                String relative = parentURL.getPath().substring(this.root.getPath().length());
                if ( StringUtils.isBlank(relative) ) {
                    return getRoot();
                }

                try ( SmbResource rf = toFile(toURL(relative)) ) {
                    return makeContainerEntity(rf, relative);
                }
            }
        }
        catch (
            IOException |
            URISyntaxException ex ) {
            throw handleException(e.getEntityKey(), ex);
        }
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

        String relative = ( !StringUtils.isBlank(ve.getRelativePath()) ? ve.getRelativePath() + "/" : StringUtils.EMPTY ) + //$NON-NLS-1$
                StringUtils.join(relativeSegments, '/');
        try ( SmbResource file = toFile(toURL(relative)) ) {
            return toEntity(file, relative);
        }
        catch ( IOException ex ) {
            throw handleException(e.getEntityKey(), ex);
        }
    }


    @Override
    public VFSContentHandle getContents ( VFSFileEntity file, RequestRange r ) throws FileshareException {
        if ( ! ( file instanceof VFSFileEntityImpl ) ) {
            throw new UnsupportedOperationException();
        }
        VFSFileEntityImpl ve = (VFSFileEntityImpl) file;
        try {
            URL url = getURL(ve);
            if ( url.equals(this.root) ) {
                return null;
            }
            try ( SmbResource path = toFile(url) ) {
                return new SMBStreamContentHandle(path.openInputStream());
            }
        }
        catch ( IOException e ) {
            throw handleException(file.getEntityKey(), e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.vfs.AbstractVFSContext#refreshInternal(eu.agno3.fileshare.model.VFSEntityImpl)
     */
    @Override
    protected void refreshInternal ( VFSEntityImpl e ) throws IOException, FileshareException {
        URL url = getURL(e);
        try ( SmbResource f = toFile(url) ) {
            if ( e instanceof VFSContainerEntityImpl ) {
                refreshContainer(f, (VFSContainerEntityImpl) e);
            }
            else if ( e instanceof VFSFileEntityImpl ) {
                refreshFile(f, (VFSFileEntityImpl) e);
            }
            this.getVfs().putCache(url, f);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.vfs.AbstractVFSContext#findModifiedSince(eu.agno3.fileshare.model.VFSContainerEntity,
     *      java.lang.Long)
     */
    @Override
    public ClosableIterator<VFSChange> findModifiedSince ( VFSContainerEntity r, Long lastMod ) throws FileshareException {
        return super.findModifiedSince(r, lastMod);
        // return new ChangeIterator(new EmptyIterator(), new EmptyIterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#checkFreeSpace(long, long)
     */
    @Override
    public void checkFreeSpace ( long neededSize, long temporarySpace ) throws InsufficentStorageSpaceException {
        try ( SmbResource f = toFile(this.root) ) {
            if ( f.getDiskFreeSpace() < neededSize ) {
                throw new InsufficentStorageSpaceException("Insufficient storage on " + this.getVfs().getId()); //$NON-NLS-1$
            }
        }
        catch ( InsufficentStorageSpaceException e ) {
            throw e;
        }
        catch (
            CIFSException |
            FileshareException e ) {
            log.warn("Failed to check free space", e); //$NON-NLS-1$
        }
    }
}
