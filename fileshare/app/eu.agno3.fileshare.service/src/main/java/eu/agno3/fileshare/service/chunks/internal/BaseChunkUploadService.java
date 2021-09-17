/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.ChunkedUploadInfo;
import eu.agno3.fileshare.model.tokens.AccessToken;
import eu.agno3.fileshare.model.tokens.AnonymousGrantToken;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.service.ChunkUploadService;
import eu.agno3.fileshare.service.ConfigurationProvider;
import eu.agno3.fileshare.service.QuotaService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public abstract class BaseChunkUploadService implements ChunkUploadService, Runnable {

    private static final Logger log = Logger.getLogger(BaseChunkUploadService.class);

    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[\\.-a-z0-9]+$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private DefaultServiceContext sctx;
    private AccessControlService accessControl;
    private PolicyEvaluator policyEvaluator;
    private QuotaService quotaService;
    private VFSServiceInternal vfs;
    private ChunkConfiguration config;

    private ScheduledExecutorService cleanupExecutor;

    protected final AtomicBoolean exit = new AtomicBoolean();


    /**
     * 
     */
    public BaseChunkUploadService () {
        super();
    }


    protected void startCleanup () {
        this.exit.set(false);
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.cleanupExecutor.scheduleAtFixedRate(this, 0, this.config.getCleanupInterval(), TimeUnit.SECONDS);
    }


    protected void stopCleanup () {
        if ( this.cleanupExecutor != null ) {
            this.exit.set(true);
            this.cleanupExecutor.shutdown();
            try {
                this.cleanupExecutor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted while waiting for executor to finish", e); //$NON-NLS-1$
            }
            this.cleanupExecutor = null;
        }
    }


    @Reference
    protected synchronized void setConfig ( ChunkConfiguration cfg ) {
        this.config = cfg;
    }


    protected synchronized void unsetConfig ( ChunkConfiguration cfg ) {
        if ( this.config == cfg ) {
            this.config = null;
        }
    }


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }


    @Reference
    protected synchronized void setPolicyEvaluator ( PolicyEvaluator pe ) {
        this.policyEvaluator = pe;
    }


    protected synchronized void unsetPolicyEvaluator ( PolicyEvaluator pe ) {
        if ( this.policyEvaluator == pe ) {
            this.policyEvaluator = null;
        }
    }


    @Reference
    protected synchronized void setQuotaService ( QuotaService qs ) {
        this.quotaService = qs;
    }


    protected synchronized void unsetQuotaService ( QuotaService qs ) {
        if ( this.quotaService == qs ) {
            this.quotaService = null;
        }
    }


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    protected EntityKey parseEntityKey ( String key ) {
        return this.vfs.parseEntityKey(key);
    }


    protected ConfigurationProvider getConfiguration () {
        return this.sctx.getConfigurationProvider();
    }


    protected abstract boolean contextExists ( String ref ) throws UserNotFoundException, AuthenticationException, IOException;


    protected abstract ChunkContext loadContext ( String ref ) throws FileshareException, IOException;


    /**
     * 
     * @param userId
     * @return
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    protected String getUserId () throws AuthenticationException, UserNotFoundException {
        if ( this.accessControl.isUserAuthenticated() ) {
            User currentUser = this.accessControl.getCurrentUserCachable();
            return currentUser.getId().toString();
        }
        else if ( this.accessControl.isTokenAuth() ) {
            AccessToken tokenAuthValue = this.accessControl.getTokenAuthValue();
            if ( ! ( tokenAuthValue instanceof AnonymousGrantToken ) ) {
                throw new AuthenticationException("Not a grant token"); //$NON-NLS-1$
            }
            String tokenId = Hex.encodeHexString( ( (AnonymousGrantToken) tokenAuthValue ).getNonce());
            return "tok-" + tokenId; //$NON-NLS-1$
        }
        throw new AuthenticationException("Not authenticated"); //$NON-NLS-1$
    }


    /**
     * @param token
     * @throws IOException
     */
    protected void checkToken ( String token ) throws IOException {
        if ( StringUtils.isEmpty(token) || !TOKEN_PATTERN.matcher(token).matches() ) {
            throw new IOException("Token value is not allowed"); //$NON-NLS-1$
        }
    }


    @Override
    public ChunkedUploadInfo getChunkInfo ( String ref ) throws IOException, FileshareException {
        if ( !contextExists(ref) ) {
            throw new ChunkUploadCanceledException();
        }

        ChunkContext context = getChunkContext(ref);
        try ( VFSContext v = this.vfs.getVFS(context.getMetadata().getTarget()).begin(true) ) {
            VFSEntity persistentTarget = v.load(context.getMetadata().getTarget());
            this.accessControl.checkAccess(v, persistentTarget, GrantPermission.UPLOAD);
            return ChunkUploadUtil.createFromContext(context, persistentTarget);
        }

    }


    @Override
    public ChunkContext getChunkContext ( String ref ) throws FileshareException, IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Get context " + ref); //$NON-NLS-1$
        }
        if ( !contextExists(ref) ) {
            throw new ChunkUploadCanceledException();
        }
        ChunkContext cc = loadContext(ref);
        if ( !cc.getState().isValid() ) {
            throw new ChunkUploadCanceledException();
        }
        try ( VFSContext v = this.vfs.getVFS(cc.getMetadata().getTarget()).begin(true) ) {
            VFSEntity persistentTarget = v.load(cc.getMetadata().getTarget());
            this.accessControl.checkAccess(v, persistentTarget, GrantPermission.UPLOAD);
            return cc;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.ChunkUploadService#getIncompleteChunkInfo()
     */
    @Override
    public List<ChunkedUploadInfo> getIncompleteChunkInfo () throws FileshareException {
        List<ChunkedUploadInfo> res = new ArrayList<>();
        try {
            for ( ChunkContext ctx : getIncompleteContexts() ) {
                try ( VFSContext v = this.vfs.getVFS(ctx.getMetadata().getTarget()).begin(true) ) {
                    VFSEntity persistentTarget = v.load(ctx.getMetadata().getTarget());
                    if ( !this.accessControl.hasAccess(v, persistentTarget, GrantPermission.UPLOAD) ) {
                        log.debug("Skipping target without permissions"); //$NON-NLS-1$
                        continue;
                    }

                    res.add(ChunkUploadUtil.createFromContext(ctx, persistentTarget));
                }
                catch ( FileshareException e ) {
                    log.warn("Failed to get entity for chunk ", e); //$NON-NLS-1$
                }
            }
        }
        catch ( IOException e ) {
            log.warn("Context enumeration failed", e); //$NON-NLS-1$
        }

        return res;
    }


    /**
     * @param f
     * @param contextPath
     * @throws IOException
     * @throws FileshareException
     */
    protected void checkChunkQuota ( long size, String ref ) throws IOException, FileshareException {
        Long limit;
        if ( this.accessControl.isUserAuthenticated() ) {
            limit = this.sctx.getConfigurationProvider().getFrontendConfiguration().getPerUserIncompleteSizeLimit();
        }
        else {
            limit = this.sctx.getConfigurationProvider().getFrontendConfiguration().getPerSessionIncompleteSizeLimit();
        }

        if ( limit == null ) {
            return;
        }

        if ( size > limit ) {
            throw new QuotaExceededException(limit, size - limit);
        }

        long used = getIncompleteSize();

        if ( log.isDebugEnabled() ) {
            log.trace(String.format("Currently used %d of %d", used, limit)); //$NON-NLS-1$
        }

        ensureFreeSpace(ref, size, used, limit);
    }


    protected abstract long getIncompleteSize ();


    protected abstract void ensureFreeSpace ( String ref, long alloc, long used, long limit ) throws FileshareException, IOException;


    protected abstract boolean ensureAndCheck ( VFSFileEntity f, String token ) throws AuthenticationException, UserNotFoundException, IOException;


    protected abstract ChunkContext initContext ( String token, EntityKey target, Properties makeCommonProperties )
            throws IOException, UserNotFoundException, AuthenticationException;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#getDefaultChunkSize()
     */
    @Override
    public long getDefaultChunkSize () {
        return this.config.getDefaultChunkSize();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#getMaximumChunkSize()
     */
    @Override
    public long getMaximumChunkSize () {
        return this.config.getMaximumChunkSize();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#getOptimalChunkCount()
     */
    @Override
    public int getOptimalChunkCount () {
        return this.config.getOptimalChunkCount();
    }


    /**
     * @return the uploadTimeout
     */
    protected Duration getUploadTimeout () {
        return this.config.getUploadTimeout();
    }


    protected abstract void runCleanup ();


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        try {
            log.debug("Running cleanup"); //$NON-NLS-1$
            runCleanup();
            log.debug("Finished running cleanup"); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.warn("Exception in cleanup", e); //$NON-NLS-1$
        }
    }


    @Override
    public ChunkContext getOrCreateChunkContext ( EntityKey target, boolean targetIsFile, String token, VFSFileEntity f, Long chunkSize,
            boolean replacing, boolean externalSource ) throws IOException, FileshareException {

        try ( VFSContext v = this.vfs.getVFS(target).begin(true) ) {
            VFSEntity persistentTarget = v.load(target);
            this.accessControl.checkAccess(v, persistentTarget, GrantPermission.UPLOAD);

            if ( persistentTarget instanceof ContainerEntity && f != null && !replacing ) {
                this.quotaService.checkFreeSpace(persistentTarget, f.getFileSize(), f.getFileSize());
            }

            if ( f != null ) {
                checkChunkQuota(f.getFileSize(), token);
            }

            boolean created = ensureAndCheck(f, token);
            ChunkContext ctx;
            if ( created ) {
                ctx = initContext(
                    token,
                    target,
                    ChunkUploadUtil.makeCommonProperties(
                        target,
                        targetIsFile,
                        token,
                        f,
                        chunkSize != null ? chunkSize : getDefaultChunkSize(),
                        replacing,
                        externalSource));
            }
            else {
                ctx = loadContext(token);
            }
            return ctx;
        }
    }

}