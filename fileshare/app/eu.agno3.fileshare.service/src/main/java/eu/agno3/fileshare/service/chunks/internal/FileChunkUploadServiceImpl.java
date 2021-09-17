/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.exceptions.UnsupportedOperationException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.service.ChunkUploadService;
import eu.agno3.fileshare.service.QuotaService;
import eu.agno3.fileshare.service.UploadState;
import eu.agno3.fileshare.service.api.internal.ChunkContextInternal;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.chunks.internal.ChunkUploadUtil.FileDeleter;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ChunkUploadService.class
}, configurationPid = "chunks.file", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class FileChunkUploadServiceImpl extends BaseChunkUploadService implements ChunkUploadService, Runnable {

    private static final String CHUNK_PROPERTIES_REOWN = "chunk.properties.reown"; //$NON-NLS-1$
    private static final String CHUNK_PROPERTIES = "chunk.properties"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileChunkUploadServiceImpl.class);

    private static final String DEFAULT_CHUNK_PATH = "/srv/fileshare/tmp-chunks/"; //$NON-NLS-1$

    private static final FileAttribute<Set<PosixFilePermission>> OWNER_PERMISSIONS = PosixFilePermissions
            .asFileAttribute(PosixFilePermissions.fromString("rwx------")); //$NON-NLS-1$

    private Path basePath;
    private GroupPrincipal storageGroup;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        String chunkPath = ConfigUtil.parseString(ctx.getProperties(), "chunksPath", DEFAULT_CHUNK_PATH); //$NON-NLS-1$

        String storageGroupSpec = ConfigUtil.parseString(ctx.getProperties(), "storageGroup", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(storageGroupSpec) ) {
            try {
                this.storageGroup = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(storageGroupSpec);
            }
            catch ( IOException e ) {
                log.error("Failed to lookup storage group", e); //$NON-NLS-1$
            }
        }

        this.basePath = Paths.get(chunkPath);
        try {
            if ( !Files.exists(this.basePath) ) {
                Files.createDirectory(this.basePath, OWNER_PERMISSIONS);
            }
            setDirectoryPermissions(this.basePath);

            if ( !Files.exists(this.basePath) || !Files.isWritable(this.basePath) ) {
                log.error("Path not writeable " + chunkPath); //$NON-NLS-1$
            }

        }
        catch ( IOException e ) {
            log.error("Failed to set storage permissions", e); //$NON-NLS-1$
        }

        startCleanup();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        stopCleanup();
    }


    @Override
    @Reference
    protected synchronized void setConfig ( ChunkConfiguration cfg ) {
        super.setConfig(cfg);
    }


    @Override
    protected synchronized void unsetConfig ( ChunkConfiguration cfg ) {
        super.unsetConfig(cfg);
    }


    @Override
    @Reference
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        super.setAccessControlService(acs);
    }


    @Override
    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        super.unsetAccessControlService(acs);
    }


    @Override
    @Reference
    protected synchronized void setPolicyEvaluator ( PolicyEvaluator pe ) {
        super.setPolicyEvaluator(pe);
    }


    @Override
    protected synchronized void unsetPolicyEvaluator ( PolicyEvaluator pe ) {
        super.unsetPolicyEvaluator(pe);
    }


    @Override
    @Reference
    protected synchronized void setQuotaService ( QuotaService qs ) {
        super.setQuotaService(qs);
    }


    @Override
    protected synchronized void unsetQuotaService ( QuotaService qs ) {
        super.unsetQuotaService(qs);
    }


    @Override
    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext ctx ) {
        super.setServiceContext(ctx);
    }


    @Override
    protected synchronized void unsetServiceContext ( DefaultServiceContext ctx ) {
        super.unsetServiceContext(ctx);
    }


    @Override
    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        super.setVFSService(vs);
    }


    @Override
    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        super.unsetVFSService(vs);
    }


    /**
     * @param basePath2
     * @throws IOException
     */
    private void setDirectoryPermissions ( Path path ) throws IOException {
        if ( this.storageGroup != null ) {
            PosixFileAttributeView attrs = Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            attrs.setGroup(this.storageGroup);
            attrs.setPermissions(PosixFilePermissions.fromString("rwxrwx---")); //$NON-NLS-1$
        }
    }


    /**
     * @param basePath2
     * @throws IOException
     */
    private void setFilePermissions ( Path path ) throws IOException {
        if ( this.storageGroup != null ) {
            PosixFileAttributeView attrs = Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            attrs.setGroup(this.storageGroup);
            attrs.setPermissions(PosixFilePermissions.fromString("rw-rw----")); //$NON-NLS-1$
        }
    }


    /**
     * @return the basePath
     */
    protected Path getBasePath () {
        return this.basePath;
    }


    protected Path getContextPath ( String token ) throws UserNotFoundException, AuthenticationException, IOException {
        checkToken(token);
        Path userPath = getUserPath();
        if ( !Files.exists(userPath) ) {
            Files.createDirectory(userPath, OWNER_PERMISSIONS);
            setDirectoryPermissions(userPath);
        }

        return userPath.resolve(String.format(
            "ctx-%s", //$NON-NLS-1$
            token != null ? token : StringUtils.EMPTY));
    }


    @Override
    protected boolean ensureAndCheck ( VFSFileEntity f, String token ) throws AuthenticationException, UserNotFoundException, IOException {
        Path contextPath = getContextPath(token);
        boolean created = false;
        if ( !Files.exists(contextPath) ) {
            try {
                Files.createDirectory(contextPath, OWNER_PERMISSIONS);
                created = true;
            }
            catch ( FileAlreadyExistsException e ) {
                log.debug("Already exists", e); //$NON-NLS-1$
            }
        }

        if ( !Files.exists(contextPath) || !Files.isDirectory(contextPath) || !Files.isWritable(contextPath) ) {
            log.warn("Context path is not accessible " + contextPath); //$NON-NLS-1$
            throw new IOException("Failed to create context path"); //$NON-NLS-1$
        }
        return created;
    }


    @Override
    protected void ensureFreeSpace ( String ref, long alloc, long used, long limit )
            throws UserNotFoundException, AuthenticationException, QuotaExceededException, IOException {
        Path contextPath = getContextPath(ref);
        long curContextSize = ChunkUploadUtil.getContextSize(contextPath);
        if ( used + alloc - curContextSize > limit ) {
            long exzess = limit + curContextSize - alloc;
            if ( !makeFreeSpace(getUserPath(), contextPath, exzess) ) {
                throw new QuotaExceededException();
            }
        }
    }


    private Path getUserPath () throws AuthenticationException, UserNotFoundException {
        return getBasePath().resolve(getUserId());
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param userPath
     * @param contextPath
     * @param needed
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws UserNotFoundException
     */
    private boolean makeFreeSpace ( Path userPath, Path contextPath, long needed )
            throws AuthenticationException, UserNotFoundException, IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Need to make free space " + needed); //$NON-NLS-1$
        }

        List<ChunkContext> contexts = getIncompleteContexts();
        Collections.sort(contexts, new Comparator<ChunkContext>() {

            @Override
            public int compare ( ChunkContext o1, ChunkContext o2 ) {
                try {
                    return o1.getLastModified().compareTo(o2.getLastModified());
                }
                catch ( IOException e ) {
                    getLog().warn("Failed to get last modified time", e); //$NON-NLS-1$
                    return 0;
                }
            }

        });

        long freed = 0;
        for ( ChunkContext ctx : contexts ) {
            if ( freed >= needed ) {
                return true;
            }

            if ( ! ( ctx instanceof FileChunkContextImpl ) ) {
                continue;
            }

            FileChunkContextImpl fc = (FileChunkContextImpl) ctx;

            if ( contextPath.equals(fc.getContextPath()) ) {
                continue;
            }
            long size = ChunkUploadUtil.getContextSize(fc.getContextPath());
            removeChunkContext(ctx.getMetadata().getReference());
            freed += size;
        }

        return freed >= needed;
    }


    @Override
    protected ChunkContext initContext ( String ref, EntityKey target, Properties props )
            throws IOException, UserNotFoundException, AuthenticationException {

        if ( log.isDebugEnabled() ) {
            log.debug("Target is " + target); //$NON-NLS-1$
        }

        Path contextPath = getContextPath(ref);

        Path propsFile = contextPath.resolve(CHUNK_PROPERTIES);
        try ( FileChannel ch = FileChannel.open(propsFile, new OpenOption[] {
            StandardOpenOption.CREATE, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE
        });
              OutputStream fos = Channels.newOutputStream(ch) ) {
            props.store(fos, "Chunk info"); //$NON-NLS-1$
        }

        setFilePermissions(propsFile);
        PropertiesMetaImpl meta = new PropertiesMetaImpl(target, props); // $NON-NLS-1$
        FileChunkContextImpl ctx = new FileChunkContextImpl(meta, contextPath, this.storageGroup);
        setDirectoryPermissions(contextPath);
        return ctx;
    }


    @Override
    public ChunkContext reownContext ( String ref ) throws IOException, FileshareException {
        Path contextPath = getContextPath(ref);
        if ( !Files.exists(contextPath) ) {
            throw new ChunkUploadCanceledException();
        }

        ChunkContext oldCtx = getChunkContext(ref);
        if ( oldCtx.getMetadata().isExternalSource() ) {
            throw new UnsupportedOperationException("Cannot re-own context from external source"); //$NON-NLS-1$
        }

        UUID newId = UUID.randomUUID();
        String newIdStr = newId.toString();
        Path propsFile = contextPath.resolve(CHUNK_PROPERTIES); // $NON-NLS-1$
        Properties props = new Properties();
        try ( FileChannel ch = FileChannel.open(propsFile);
              InputStream fs = Channels.newInputStream(ch) ) {
            props.load(fs);
        }
        props.setProperty("reference", newIdStr); //$NON-NLS-1$

        Path newPropsFile = contextPath.resolve(CHUNK_PROPERTIES_REOWN); // $NON-NLS-1$
        try ( FileChannel ch = FileChannel
                .open(newPropsFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
              OutputStream fs = Channels.newOutputStream(ch) ) {
            props.store(fs, "Reowned context"); //$NON-NLS-1$
        }
        setFilePermissions(newPropsFile);
        Path newContextPath = getContextPath(newIdStr);
        // move context
        Files.move(contextPath, newContextPath, StandardCopyOption.ATOMIC_MOVE);
        try {
            Files.move(
                newContextPath.resolve(CHUNK_PROPERTIES_REOWN),
                newContextPath.resolve(CHUNK_PROPERTIES),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
        }
        catch ( IOException e ) {
            // try to restore
            Files.move(newContextPath, contextPath, StandardCopyOption.ATOMIC_MOVE);
            throw e;
        }
        return getChunkContext(newIdStr);
    }


    /**
     * 
     * @param contextDir
     * @return
     * @throws FileshareException
     * @throws IOException
     */
    private FileChunkContextImpl loadContext ( Path contextDir ) throws FileshareException, IOException {
        Path propsFile = contextDir.resolve(CHUNK_PROPERTIES); // $NON-NLS-1$
        Properties props = new Properties();
        try ( FileChannel ch = FileChannel.open(propsFile);
              InputStream fis = Channels.newInputStream(ch) ) {
            props.load(fis);
        }

        EntityKey entityKey = parseEntityKey(props.getProperty("target-id")); //$NON-NLS-1$
        PropertiesMetaImpl meta = new PropertiesMetaImpl(entityKey, props);
        FileChunkContextImpl ctx = new FileChunkContextImpl(meta, contextDir, this.storageGroup);
        setDirectoryPermissions(contextDir);
        return ctx;
    }


    @Override
    protected FileChunkContextImpl loadContext ( String ref ) throws FileshareException, IOException {
        return loadContext(getContextPath(ref));
    }


    @Override
    protected boolean contextExists ( String ref ) throws UserNotFoundException, AuthenticationException, IOException {
        return Files.exists(getContextPath(ref));
    }


    @Override
    public void removeChunkContext ( String ref ) throws IOException, UserNotFoundException, AuthenticationException {
        Path contextPath = getContextPath(ref);

        if ( log.isDebugEnabled() ) {
            log.debug("Deleting " + contextPath); //$NON-NLS-1$
        }

        if ( Files.exists(contextPath) && Files.isDirectory(contextPath) ) {
            Files.walkFileTree(contextPath, ChunkUploadUtil.FileDeleter.INSTANCE);
        }
        else {
            log.debug("Not removing"); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#getIncompleteContexts()
     */
    @Override
    public List<ChunkContext> getIncompleteContexts () throws AuthenticationException {
        return getContextsWithState(EnumSet.of(UploadState.FAILED, UploadState.UPLOADING, UploadState.PROCESSING, UploadState.COMPLETE));
    }


    /**
     * @param state
     * @return
     * @throws AuthenticationException
     */
    private List<ChunkContext> getContextsWithState ( Set<UploadState> state ) throws AuthenticationException {
        Path path;
        try {
            path = getUserPath();
        }
        catch ( UserNotFoundException e ) {
            log.trace("Not a user", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }

        if ( !Files.exists(path) ) {
            return Collections.EMPTY_LIST;
        }

        try ( Stream<Path> files = Files.list(path) ) {
            return files.map(p -> {
                try {
                    if ( Files.isReadable(p) ) {
                        ChunkContext cc = loadContext(p);
                        if ( !state.contains(cc.getState()) ) {
                            return null;
                        }
                        return cc;
                    }
                }
                catch (
                    FileshareException |
                    IOException e ) {
                    log.warn("Failed to load context", e); //$NON-NLS-1$
                }
                return null;
            }).filter(cc -> {
                return cc != null;
            }).collect(Collectors.toList());
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate context", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * 
     * @return the user's/session's used space
     */
    @Override
    public long getIncompleteSize () {
        Path path;
        try {
            path = getUserPath();
        }
        catch (
            UserNotFoundException |
            AuthenticationException e ) {
            log.trace("Not a user", e); //$NON-NLS-1$
            return 0;
        }

        if ( !Files.exists(path) || !Files.isReadable(path) ) {
            return 0;
        }

        try ( Stream<Path> files = Files.list(path) ) {
            return files.filter(FileChunkUploadServiceImpl::CONTEXT_FILTER).mapToLong(p -> {
                return ChunkUploadUtil.getContextSize(p);
            }).sum();
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate contexts", e); //$NON-NLS-1$
            return 0;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AuthenticationException
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#getNumIncompleteContexts()
     */
    @Override
    public int getNumIncompleteContexts () throws AuthenticationException {
        Path path;
        try {
            path = getUserPath();
        }
        catch ( UserNotFoundException e ) {
            log.trace("Not a user", e); //$NON-NLS-1$
            return 0;
        }

        if ( !Files.exists(path) ) {
            return 0;
        }

        try ( Stream<Path> files = Files.list(path) ) {
            return (int) files.filter(FileChunkUploadServiceImpl::CONTEXT_FILTER).count();
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate contexts", e); //$NON-NLS-1$
            return 0;
        }
    }


    @Override
    protected void runCleanup () {
        try ( Stream<Path> contexts = Files.list(getBasePath()) ) {
            contexts.forEach(userDir -> {
                if ( !Files.isDirectory(userDir) ) {
                    return;
                }

                if ( this.exit.get() ) {
                    throw new ChunkUploadUtil.BreakException();
                }

                DateTime expireBefore;
                if ( userDir.getFileName().toString().startsWith("tok-") ) { //$NON-NLS-1$
                    expireBefore = DateTime.now().minus(getConfiguration().getFrontendConfiguration().getTokenIncompleteExpireDuration());
                }
                else {
                    expireBefore = DateTime.now().minus(getConfiguration().getFrontendConfiguration().getUserIncompleteExpireDuration());
                }

                try ( Stream<Path> contextFiles = Files.list(userDir) ) {
                    expireUserContexts(userDir, contextFiles, expireBefore);
                }
                catch ( IOException e ) {
                    log.warn("Failed to cleanup context contents of " + userDir, e); //$NON-NLS-1$
                }
            });

        }
        catch ( ChunkUploadUtil.BreakException e ) {
            return;
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate contexts", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.error("Exception in cleanup runner", e); //$NON-NLS-1$
        }
    }


    /**
     * @param f
     * @param contexts
     * @param expireBefore
     */
    protected void expireUserContexts ( Path f, Stream<Path> contexts, DateTime expireBefore ) {
        boolean foundOne = false;
        try {
            foundOne = contexts.map(p -> {

                if ( this.exit.get() ) {
                    throw new ChunkUploadUtil.BreakException();
                }

                try {
                    ChunkContextInternal ctx = loadContext(p);
                    DateTime lastMod = ctx.getLastModified();
                    if ( ctx.getState().getState() != UploadState.UPLOADING && lastMod.isBefore(expireBefore) ) {
                        try {
                            Files.walkFileTree(f, FileDeleter.INSTANCE);
                        }
                        catch ( IOException ex ) {
                            log.warn("Failed to delete user/session directory", ex); //$NON-NLS-1$
                        }
                    }
                    else if ( ctx.getState().getState() == UploadState.UPLOADING || ctx.getState().getState() == UploadState.INITIAL ) {
                        long diff = System.currentTimeMillis() - lastMod.getMillis();
                        if ( log.isDebugEnabled() ) {
                            log.debug(String.format("Is currently uploading %s last write %d ms ago", p, diff)); //$NON-NLS-1$
                        }

                        if ( diff > this.getUploadTimeout().getMillis() ) {
                            log.debug("Upload timed out"); //$NON-NLS-1$
                            ctx.getState().setState(UploadState.FAILED);
                        }
                    }
                    else if ( log.isDebugEnabled() ) {
                        log.debug(String.format("%s state %s, within expiration threshold, lastmod %s", p, ctx.getState(), lastMod)); //$NON-NLS-1$
                    }
                }
                catch ( NoSuchFileException e ) {
                    log.debug("Context was concurrently removed", e); //$NON-NLS-1$
                    try {
                        Files.walkFileTree(f, ChunkUploadUtil.FileDeleter.INSTANCE);
                    }
                    catch ( IOException ex ) {
                        log.debug("Failed ensure context removal", ex); //$NON-NLS-1$
                    }
                }
                catch (
                    FileshareException |
                    IOException e ) {
                    log.warn("Failed to open context, removing", e); //$NON-NLS-1$
                    try {
                        Files.walkFileTree(f, ChunkUploadUtil.FileDeleter.INSTANCE);
                    }
                    catch ( IOException ex ) {
                        log.warn("Failed to delete user/session directory", ex); //$NON-NLS-1$
                    }
                }
                return true;
            }).count() > 0;
        }
        catch ( ChunkUploadUtil.BreakException e ) {
            return;
        }

        if ( this.exit.get() ) {
            return;
        }

        if ( !foundOne ) {
            log.debug("No contexts found, deleting"); //$NON-NLS-1$
            try {
                Files.walkFileTree(f, ChunkUploadUtil.FileDeleter.INSTANCE);
            }
            catch ( IOException e ) {
                log.warn("Failed to delete user/session directory", e); //$NON-NLS-1$
            }
            return;
        }

    }


    private static boolean CONTEXT_FILTER ( Path t ) {
        return Files.isDirectory(t) && Files.isWritable(t) && t.getFileName().toString().startsWith("ctx-"); //$NON-NLS-1$
    }

}
