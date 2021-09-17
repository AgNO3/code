/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.exceptions.ContentException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InvalidEntityException;
import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.exceptions.UploadException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.notify.UploadNotificationData;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.service.ChunkUploadService;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.UploadService;
import eu.agno3.fileshare.service.UploadState;
import eu.agno3.fileshare.service.api.internal.BlockStorageService;
import eu.agno3.fileshare.service.api.internal.ContentFilter;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.EntityServiceInternal;
import eu.agno3.fileshare.service.api.internal.MailNotifier;
import eu.agno3.fileshare.service.api.internal.NotificationService;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.QuotaReservation;
import eu.agno3.fileshare.service.api.internal.QuotaServiceInternal;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.audit.SingleEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.vfs.VFSStoreHandle;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = UploadService.class, configurationPid = "upload", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class UploadServiceImpl implements UploadService {

    private static final Logger log = Logger.getLogger(UploadServiceImpl.class);

    private DefaultServiceContext ctx;
    private BlockStorageService blockStore;
    private AccessControlService accessControl;
    private NotificationService notifyService;
    private EntityServiceInternal entityService;
    private PolicyEvaluator policyEvaluator;
    private QuotaServiceInternal quota;
    private ChunkUploadService chunkUploadService;
    private Set<ContentFilter> contentFilters = Collections.synchronizedSet(new HashSet<>());
    private Set<String> requireContentFilters = Collections.synchronizedSet(new HashSet<>());
    private VFSServiceInternal vfs;

    private RecursiveModificationTimeTracker modTracker;


    @Activate
    protected synchronized void activate ( ComponentContext cc ) {
        this.parseConfig(cc.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext cc ) {
        this.parseConfig(cc.getProperties());
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.requireContentFilters = ConfigUtil.parseStringSet(cfg, "requiredFilters", Collections.EMPTY_SET); //$NON-NLS-1$
    }


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setBlockStorageService ( BlockStorageService bs ) {
        this.blockStore = bs;
    }


    protected synchronized void unsetBlockStorageService ( BlockStorageService bs ) {
        if ( this.blockStore == bs ) {
            this.blockStore = null;
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
    protected synchronized void setNotificationService ( NotificationService ns ) {
        this.notifyService = ns;
    }


    protected synchronized void unsetNotificationService ( NotificationService ns ) {
        if ( this.notifyService == ns ) {
            this.notifyService = null;
        }
    }


    @Reference
    protected synchronized void setEntityService ( EntityServiceInternal es ) {
        this.entityService = es;
    }


    protected synchronized void unsetEntityService ( EntityServiceInternal es ) {
        if ( this.entityService == es ) {
            this.entityService = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindContentFilter ( ContentFilter cf ) {
        this.contentFilters.add(cf);
    }


    protected synchronized void unbindContentFilter ( ContentFilter cf ) {
        this.contentFilters.remove(cf);
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
    protected synchronized void setQuotaService ( QuotaServiceInternal qs ) {
        this.quota = qs;
    }


    protected synchronized void unsetQuotaService ( QuotaServiceInternal qs ) {
        if ( this.quota == qs ) {
            this.quota = null;
        }
    }


    @Reference
    protected synchronized void setChunkUploadService ( ChunkUploadService cus ) {
        this.chunkUploadService = cus;
    }


    protected synchronized void unsetChunkUploadService ( ChunkUploadService cus ) {
        if ( this.chunkUploadService == cus ) {
            this.chunkUploadService = null;
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


    @Reference
    protected synchronized void setRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        this.modTracker = rmt;
    }


    protected synchronized void unsetRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        if ( this.modTracker == rmt ) {
            this.modTracker = null;
        }
    }


    private ChunkContext handleChunked ( HttpServletRequest req, HttpServletResponse resp, EntityKey target, boolean targetIsFile, VFSFileEntity f,
            ReadableByteChannel data, boolean replacing ) throws FileshareException {
        long chunkSize = this.chunkUploadService.getDefaultChunkSize();

        String chunkSizeSpec = req.getHeader("X-Upload-Chunk-Size"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(chunkSizeSpec) ) {
            try {
                Long pchunkSize = Long.valueOf(chunkSizeSpec);
                if ( pchunkSize > chunkSize ) {
                    chunkSize = pchunkSize;
                    if ( log.isDebugEnabled() ) {
                        log.debug("Have custom chunk size " + chunkSize); //$NON-NLS-1$
                    }
                }
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Failed to parse chunk size", e); //$NON-NLS-1$
            }
        }

        String range = req.getHeader("Content-Range"); //$NON-NLS-1$
        String token = req.getHeader("X-Upload-TransferId"); //$NON-NLS-1$
        String contHeader = req.getHeader("X-Upload-Continuation"); //$NON-NLS-1$
        boolean continuation = contHeader != null && Boolean.parseBoolean(contHeader);

        if ( StringUtils.isBlank(token) ) {
            log.debug("Client did not provide transfer id, generating a random one"); //$NON-NLS-1$
            token = UUID.randomUUID().toString();
        }

        resp.setHeader("X-Upload-TransferId", token); //$NON-NLS-1$
        resp.setHeader("X-Upload-Chunk-Size", String.valueOf(chunkSize)); //$NON-NLS-1$

        try {
            if ( StringUtils.isBlank(range) ) {
                log.debug("Non chunked upload"); //$NON-NLS-1$
                try {
                    ChunkContext cc;
                    if ( continuation ) {
                        cc = this.chunkUploadService.getChunkContext(token);
                    }
                    else {
                        cc = this.chunkUploadService.getOrCreateChunkContext(target, targetIsFile, token, f, chunkSize, replacing, false);
                    }
                    cc.storeInput(data, req.getContentLengthLong());
                    return cc;
                }
                catch ( EntityNotFoundException e ) {
                    return null;
                }
                catch ( IOException e ) {
                    throw new UploadException("Upload error", e); //$NON-NLS-1$
                }
            }

            return handleRangeUpload(target, f, targetIsFile, replacing, data, chunkSize, range, token, continuation);
        }
        catch ( ChunkUploadCanceledException e ) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw e;
        }
    }


    /**
     * @param target
     * @param f
     * @param replacing
     * @param targetIsFile
     * @param data
     * @param chunkSize
     * @param range
     * @param token
     * @param cc
     * @return
     * @throws UploadException
     * @throws FileshareException
     */
    private ChunkContext handleRangeUpload ( EntityKey target, VFSFileEntity f, boolean targetIsFile, boolean replacing, ReadableByteChannel data,
            long chunkSize, String range, String token, boolean continuation ) throws UploadException, FileshareException {
        log.debug("Have chunked upload"); //$NON-NLS-1$
        long curChunkSize = f.getFileSize();
        int rangeSep = range.indexOf('-', 6);
        int sizeSep = range.indexOf('/', 6);
        if ( !range.startsWith("bytes ") || rangeSep < 0 || sizeSep < 0 ) { //$NON-NLS-1$
            log.warn("Invalid range header: " + range); //$NON-NLS-1$
            throw new UploadException("Invalid content range"); //$NON-NLS-1$
        }

        try {
            long rangeStart = Long.parseLong(range.substring(6, rangeSep));
            long rangeEnd = Long.parseLong(range.substring(rangeSep + 1, sizeSep));
            long rangeLen = rangeEnd - rangeStart;
            if ( rangeLen <= 0 ) {
                throw new FileshareException("Invalid range: " + range); //$NON-NLS-1$
            }
            long size = Long.parseLong(range.substring(sizeSep + 1));
            f.setFileSize(size);

            if ( log.isDebugEnabled() ) {
                log.debug("Transfer id " + token); //$NON-NLS-1$
                log.debug(String.format("%d to %d (total: %d, chunk size: %d)", rangeStart, rangeEnd, size, chunkSize)); //$NON-NLS-1$
            }

            ChunkContext cc;
            if ( continuation ) {
                cc = this.chunkUploadService.getChunkContext(token);
            }
            else {
                cc = this.chunkUploadService.getOrCreateChunkContext(target, targetIsFile, token, f, chunkSize, replacing, false);
            }

            if ( cc.getChunkState().getTotalSize() != null && cc.getChunkState().getTotalSize() != size ) {
                throw new UploadException("File size changed"); //$NON-NLS-1$
            }

            long actualChunkSize = cc.getChunkState().getChunkSize();
            int chunkIdx = (int) ( rangeStart / actualChunkSize );
            int chunkOff = (int) ( rangeStart % actualChunkSize );
            if ( chunkOff != 0 ) {
                throw new UploadException("Data must start a chunk boundary, offset is " + chunkOff); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Chunk %d of %d", chunkIdx, cc.getChunkState().getNumChunks())); //$NON-NLS-1$
            }
            cc.storeChunk(chunkIdx, data, curChunkSize);
            return cc;
        }
        catch ( EntityNotFoundException e ) {
            return null;
        }
        catch ( IllegalArgumentException e ) {
            log.warn("Invalid range header: " + range, e); //$NON-NLS-1$
            throw new UploadException("Invalid content range"); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            throw new UploadException("Chunked upload failed", e); //$NON-NLS-1$
        }
    }


    @Override
    public VFSFileEntity create ( EntityKey targetId, VFSFileEntity f, ReadableByteChannel data, ServletRequest req, ServletResponse resp )
            throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(targetId).begin(true) ) {
            // preflight check before handling body to allow expect/continue
            VFSEntity target = v.load(targetId, VFSEntity.class);
            this.accessControl.checkAccess(v, target, GrantPermission.UPLOAD);
            if ( target == null ) {
                throw new EntityNotFoundException();
            }
            this.policyEvaluator.checkPolicy(v, target, req);
            this.quota.checkFreeSpace(target, f.getFileSize(), f.getFileSize());
        }
        f.setLocalName(ServiceUtil.normalizeFileName(f.getLocalName()));
        ChunkContext cc = handleChunked((HttpServletRequest) req, (HttpServletResponse) resp, targetId, false, f, data, false);
        return createInternal(targetId, f, req, cc);
    }


    /**
     * @param targetId
     * @param f
     * @param req
     * @param cc
     * @return
     * @throws InvalidEntityException
     * @throws FileshareException
     * @throws UploadException
     */
    private VFSFileEntity createInternal ( EntityKey targetId, VFSFileEntity f, ServletRequest req, ChunkContext cc )
            throws InvalidEntityException, FileshareException, UploadException {
        if ( cc == null || !cc.isComplete() ) {
            return null;
        }
        VFSStoreHandle handle = null;
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("CREATE"); //$NON-NLS-1$
            auditFileProperties(f, audit);
            try ( InputBuffer input = cc.getInputBuffer() ) {
                filterContent(f, cc, input);
                if ( f.getFileSize() < 0 ) {
                    f.setFileSize(input.getSize());
                }
                audit.builder().property("actualFileSize", f.getFileSize()); //$NON-NLS-1$

                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                      VFSContext v = this.vfs.getVFS(targetId).begin(tx) ) {
                    VFSContainerEntity target = v.load(targetId, VFSContainerEntity.class);
                    audit.builder().parentEntity(target);
                    this.accessControl.checkAccess(v, target, GrantPermission.UPLOAD);
                    this.policyEvaluator.checkPolicy(v, target, req);

                    User currentUser = this.accessControl.getCurrentUser(tx);
                    Grant g = this.accessControl.getTokenAuthGrant(v, target);

                    audit.builder().grant(g);

                    try ( QuotaReservation qres = this.quota.checkAndReserve(v, target, f.getFileSize(), 0) ) {
                        setupUploadProperties(tx, v, f, target, ServiceUtil.getDirectoryElementNames(v, target).keySet(), currentUser, g);

                        v.addChild(target, f);
                        v.save(f, target);

                        audit.builder().entity(f);
                        if ( !cc.getState().isValid() ) {
                            return null;
                        }
                        handle = v.storeContents(f, input);
                        if ( handle.getLength() != f.getFileSize() ) {
                            v.removeContents(f);
                            throw new UploadException(String.format("File size is %d but only %d written", f.getFileSize(), handle.getLength())); //$NON-NLS-1$
                        }

                        qres.commit(v, target);
                        tx.commit();
                    }

                    sendUploadNotification(v, target, f, currentUser, g);
                }
                this.chunkUploadService.removeChunkContext(cc.getMetadata().getReference());
                commit(handle);
                return f;
            }
            catch ( FileshareException e ) {
                log.debug("Exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(e);
                revert(handle);
                throw e;
            }
            catch ( IOException e ) {
                log.debug("Exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(AuditStatus.INTERNAL);
                revert(handle);
                throw new UploadException("Transfer failed", e); //$NON-NLS-1$
            }
            catch ( Exception e ) {
                log.warn("Unhandled exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(AuditStatus.INTERNAL);
                revert(handle);
                throw new UploadException("Unknown error in upload", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param f
     * @param audit
     */
    private static void auditFileProperties ( VFSFileEntity f, AuditContext<SingleEntityFileshareAuditBuilder> audit ) {
        audit.builder().property("originalFileSize", f.getFileSize()); //$NON-NLS-1$
        audit.builder().property("contentType", f.getContentType()); //$NON-NLS-1$
        audit.builder().property("fileName", f.getLocalName()); //$NON-NLS-1$
    }


    /**
     * @param target
     * @param f
     * @param g
     * @param currentUser
     * @throws FileshareException
     */
    private void sendUploadNotification ( VFSContext v, VFSContainerEntity target, VFSFileEntity f, User currentUser, Grant g )
            throws FileshareException {
        if ( target.getSendNotifications() && ( !this.accessControl.isOwner(v, target) ) ) {
            MailNotifier<UploadNotificationData> uploadNotification = this.notifyService.makeUploadNotification();
            UploadNotificationData notifyData = new UploadNotificationData();
            notifyData.setEntity(f);
            notifyData.setFullPath(StringUtils.join(this.entityService.getFullPath(v, f, false), '/'));
            notifyData.setRecipients(this.notifyService.getRecipientsForSubject(f.getOwner(), null));
            notifyData.setOwnerIsGroup(target.getOwner() instanceof Group);
            notifyData.setUploadingUser(currentUser);
            notifyData.setUploadingGrant(g);
            notifyData.setHideSensitive(this.ctx.getConfigurationProvider().getNotificationConfiguration().isHideSensitiveInformation());

            try {
                uploadNotification.notify(notifyData);
            }
            catch ( NotificationException e ) {
                log.warn("Failed to send upload notification", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public VFSFileEntity createOrReplace ( EntityKey targetId, VFSFileEntity f, ReadableByteChannel data, ServletRequest req, ServletResponse resp )
            throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(targetId).begin(tx) ) {
            // preflight check before handling body to allow expect/continue
            VFSEntity target = v.load(targetId, VFSEntity.class);
            boolean haveEdit = this.accessControl.hasAccess(v, target, GrantPermission.EDIT);
            if ( !haveEdit ) {
                this.accessControl.checkAccess(v, target, GrantPermission.EDIT_SELF);
            }
            if ( target == null ) {
                throw new EntityNotFoundException();
            }
            this.policyEvaluator.checkPolicy(v, target, req);

        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Internal error", e); //$NON-NLS-1$
        }
        f.setLocalName(ServiceUtil.normalizeFileName(f.getLocalName()));
        ChunkContext cc = handleChunked((HttpServletRequest) req, (HttpServletResponse) resp, targetId, false, f, data, true);
        return createOrReplaceInternal(targetId, f, req, cc);
    }


    /**
     * @param targetId
     * @param f
     * @param req
     * @param cc
     * @return
     * @throws InvalidEntityException
     * @throws FileshareException
     * @throws UploadException
     */
    private VFSFileEntity createOrReplaceInternal ( EntityKey targetId, VFSFileEntity f, ServletRequest req, ChunkContext cc )
            throws InvalidEntityException, FileshareException, UploadException {
        if ( cc == null || !cc.isComplete() ) {
            return null;
        }
        VFSStoreHandle handle = null;
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("CREATE_OR_REPLACE"); //$NON-NLS-1$
            auditFileProperties(f, audit);
            try ( InputBuffer input = cc.getInputBuffer() ) {
                filterContent(f, cc, input);

                if ( f.getFileSize() < 0 ) {
                    f.setFileSize(input.getSize());
                }

                audit.builder().property("actualFileSize", f.getFileSize()); //$NON-NLS-1$

                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                      VFSContext v = this.vfs.getVFS(targetId).begin(tx) ) {
                    VFSContainerEntity target = v.load(targetId, VFSContainerEntity.class);

                    audit.builder().parentEntity(target);

                    boolean haveEdit = this.accessControl.hasAccess(v, target, GrantPermission.EDIT);

                    if ( !haveEdit ) {
                        this.accessControl.checkAccess(v, target, GrantPermission.EDIT_SELF);
                    }

                    User currentUser = this.accessControl.getCurrentUser(tx);
                    Grant g = this.accessControl.getTokenAuthGrant(v, target);
                    this.policyEvaluator.checkPolicy(v, target, req);

                    audit.builder().grant(g);

                    if ( target == null ) {
                        throw new EntityNotFoundException();
                    }

                    Map<String, VFSEntity> names = ServiceUtil.getDirectoryElementNames(v, target);

                    boolean created = false;
                    VFSEntity foundEntity = names.get(f.getLocalName());
                    boolean doOverwrite = target.getAllowFileOverwrite() && foundEntity instanceof FileEntity
                            && ( haveEdit || this.accessControl.isUserCreatorWithPerm(v, foundEntity, currentUser, g, GrantPermission.EDIT_SELF) );

                    long sizeDiff = doOverwrite ? f.getFileSize() - ( (FileEntity) foundEntity ).getFileSize() : f.getFileSize();
                    try ( QuotaReservation qres = this.quota.checkAndReserve(v, target, sizeDiff, 0) ) {
                        if ( doOverwrite ) {
                            audit.builder().entity(foundEntity).action("REPLACE"); //$NON-NLS-1$
                            // already exists and overwrite is allowed
                            mergeReplaceProperties(v, (VFSFileEntity) foundEntity, f, currentUser, g);

                            v.save(foundEntity, target);
                            qres.commit(v, target);
                            v.commit();
                            handle = v.replaceContents((FileEntity) foundEntity, input);
                            if ( handle.getLength() != f.getFileSize() ) {
                                throw new UploadException(String.format("File size is %d but only %d written", f.getFileSize(), handle.getLength())); //$NON-NLS-1$
                            }
                            tx.commit();
                        }
                        else {
                            audit.builder().action("CREATE"); //$NON-NLS-1$
                            setupUploadProperties(tx, v, f, target, names.keySet(), currentUser, g);
                            v.addChild(target, f);
                            audit.builder().entity(f);
                            v.saveNoFlush(f);

                            if ( !cc.getState().isValid() ) {
                                return null;
                            }
                            handle = v.storeContents(f, input);
                            if ( handle.getLength() != f.getFileSize() ) {
                                v.removeContents(f);
                                throw new UploadException(String.format("File size is %d but only %d written", f.getFileSize(), handle.getLength())); //$NON-NLS-1$
                            }

                            v.refresh(target);
                            v.save(target);
                            created = true;
                            qres.commit(v, target);
                            v.commit();
                            tx.commit();
                        }
                    }

                    if ( created ) {
                        sendUploadNotification(v, target, f, currentUser, g);
                    }
                }
                this.chunkUploadService.removeChunkContext(cc.getMetadata().getReference());
                commit(handle);
                return f;
            }
            catch ( FileshareException e ) {
                log.debug("Exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(e);
                revert(handle);
                throw e;
            }
            catch ( IOException e ) {
                log.debug("Exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(AuditStatus.INTERNAL);
                revert(handle);
                throw new UploadException("Transfer failed", e); //$NON-NLS-1$
            }
            catch ( Exception e ) {
                log.warn("Unhandled exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(AuditStatus.INTERNAL);
                revert(handle);
                throw new UploadException("Unknown error in upload", e); //$NON-NLS-1$
            }

        }
    }


    /**
     * @param handle
     */
    void revert ( VFSStoreHandle handle ) {
        try {
            if ( handle != null ) {
                handle.revert();
            }
        }
        catch ( Exception ex ) {
            log.warn("Failed to restore upload data", ex); //$NON-NLS-1$
        }
    }


    /**
     * @param handle
     */
    void commit ( VFSStoreHandle handle ) {
        try {
            if ( handle != null ) {
                handle.commit();
            }
        }
        catch ( Exception ex ) {
            log.warn("Failed to commit upload data", ex); //$NON-NLS-1$
        }
    }


    /**
     * @param f
     * @param input
     * @throws ContentException
     * @throws IOException
     */
    private void filterContent ( VFSFileEntity f, ChunkContext cc, InputBuffer input ) throws ContentException, IOException {

        if ( cc.getState().getState() == UploadState.COMPLETE ) {
            log.debug("Already processed by filters"); //$NON-NLS-1$
            return;
        }
        cc.getState().setState(UploadState.PROCESSING);

        Set<String> appliedContentFilters = new HashSet<>();
        try ( SeekableByteChannel stream = input.getStream();
              SeekableByteChannel noClose = new NoCloseChannel(stream) ) {
            Set<ContentFilter> cfs = new HashSet<>(this.contentFilters);
            for ( ContentFilter cf : cfs ) {
                try {
                    stream.position(0);
                    cf.filterContent(f, noClose);
                    if ( !stream.isOpen() ) {
                        throw new ContentException(String.format("Input stream was closed in %s", cf.getClass().getName())); //$NON-NLS-1$
                    }
                    appliedContentFilters.add(cf.getId());
                }
                catch ( ContentException e ) {
                    throw e;
                }
                catch ( FileshareException e ) {
                    throw new ContentException("Content filter failed", e); //$NON-NLS-1$
                }
            }
        }

        if ( !appliedContentFilters.containsAll(this.requireContentFilters) ) {
            throw new ContentException("At least one of the required content filters was not applied"); //$NON-NLS-1$
        }

        cc.getState().setState(UploadState.COMPLETE);
    }


    @Override
    public VFSFileEntity replaceFile ( EntityKey targetFileId, VFSFileEntity f, ReadableByteChannel data, ServletRequest req, ServletResponse resp )
            throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(targetFileId).begin(tx) ) {
            // preflight check before handling body to allow expect/continue
            VFSFileEntity targetFile = v.load(targetFileId, VFSFileEntity.class);
            VFSContainerEntity e = v.getParent(targetFile);
            if ( !e.getAllowFileOverwrite() ) {
                throw new AccessDeniedException("Directory contents cannot be overwritten"); //$NON-NLS-1$
            }
            this.accessControl.checkAccess(v, targetFile, GrantPermission.EDIT);
            if ( targetFile == null ) {
                throw new EntityNotFoundException();
            }
            this.policyEvaluator.checkPolicy(v, targetFile, req);
            this.quota.checkFreeSpace(targetFile, f.getFileSize() - targetFile.getFileSize(), f.getFileSize());
            f.setLocalName(targetFile.getLocalName());
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Internal error", e); //$NON-NLS-1$
        }
        ChunkContext cc = handleChunked((HttpServletRequest) req, (HttpServletResponse) resp, targetFileId, true, f, data, true);
        return replaceFileInternal(targetFileId, f, req, cc);
    }


    /**
     * @param targetFileId
     * @param f
     * @param req
     * @param cc
     * @return
     * @throws FileshareException
     * @throws UploadException
     */
    private VFSFileEntity replaceFileInternal ( EntityKey targetFileId, VFSFileEntity f, ServletRequest req, ChunkContext cc )
            throws FileshareException, UploadException {
        if ( cc == null || !cc.isComplete() ) {
            return null;
        }
        VFSStoreHandle handle = null;
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("REPLACE"); //$NON-NLS-1$
            auditFileProperties(f, audit);
            try ( InputBuffer input = cc.getInputBuffer() ) {
                filterContent(f, cc, input);

                if ( f.getFileSize() < 0 ) {
                    f.setFileSize(input.getSize());
                }

                audit.builder().property("actualFileSize", f.getFileSize()); //$NON-NLS-1$

                VFSFileEntity targetFile;
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                      VFSContext v = this.vfs.getVFS(targetFileId).begin(tx) ) {
                    targetFile = v.load(targetFileId, VFSFileEntity.class);
                    User currentUser = this.accessControl.getCurrentUser(tx);
                    Grant g = this.accessControl.getTokenAuthGrant(v, targetFile);

                    audit.builder().entity(targetFile).grant(g);

                    VFSContainerEntity e = v.getParent(targetFile);
                    if ( !e.getAllowFileOverwrite() ) {
                        throw new AccessDeniedException("Directory contents cannot be overwritten"); //$NON-NLS-1$
                    }

                    if ( targetFile == null ) {
                        throw new EntityNotFoundException();
                    }

                    long sizeDiff = f.getFileSize() - targetFile.getFileSize();
                    try ( QuotaReservation qres = this.quota.checkAndReserve(v, e, sizeDiff, 0) ) {

                        this.accessControl.checkAccess(v, targetFile, GrantPermission.EDIT);
                        this.policyEvaluator.checkPolicy(v, targetFile, req);

                        mergeReplaceProperties(v, targetFile, f, currentUser, g);
                        v.save(targetFile);
                        qres.commit(v, e);
                        v.commit();
                        handle = v.replaceContents(targetFile, input);
                        if ( handle.getLength() != f.getFileSize() ) {
                            v.removeContents(targetFile);
                            throw new UploadException(String.format("File size is %d but only %d written", f.getFileSize(), handle.getLength())); //$NON-NLS-1$
                        }
                        tx.commit();
                    }
                }
                this.chunkUploadService.removeChunkContext(cc.getMetadata().getReference());
                commit(handle);
                return targetFile;
            }
            catch ( FileshareException e ) {
                log.debug("Exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(e);
                revert(handle);
                throw e;
            }
            catch ( IOException e ) {
                log.debug("Exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new UploadException("Transfer failed", e); //$NON-NLS-1$
            }
            catch ( Exception e ) {
                log.warn("Unhandled exception in upload handling", e); //$NON-NLS-1$
                cc.getState().setState(UploadState.FAILED);
                audit.builder().fail(AuditStatus.INTERNAL);
                revert(handle);
                throw new UploadException("Unknown error in upload", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.UploadService#retry(eu.agno3.fileshare.model.EntityKey,
     *      eu.agno3.fileshare.model.VFSFileEntity, eu.agno3.fileshare.service.ChunkContext,
     *      javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public VFSFileEntity retry ( EntityKey k, VFSFileEntity fe, ChunkContext chunkContext, ServletRequest request, ServletResponse response )
            throws IOException, FileshareException {
        if ( chunkContext.getChunkState().getTotalSize() != null ) {
            fe.setFileSize(chunkContext.getChunkState().getTotalSize());
        }
        else {
            fe.setFileSize(chunkContext.getStoredSize());
        }

        fe.setLocalName(ServiceUtil.normalizeFileName(fe.getLocalName()));

        try ( InputBuffer ib = chunkContext.getInputBuffer();
              ReadableByteChannel is = ib.getStream() ) {
            if ( chunkContext.getMetadata().isTargetFile() ) {
                return replaceFileInternal(chunkContext.getMetadata().getTarget(), fe, request, chunkContext);
            }
            else if ( chunkContext.getMetadata().isReplacing() ) {
                return createOrReplaceInternal(chunkContext.getMetadata().getTarget(), fe, request, chunkContext);
            }
            else {
                return createInternal(chunkContext.getMetadata().getTarget(), fe, request, chunkContext);
            }
        }
    }


    /**
     * @param targetFile
     * @param em
     * @param f
     * @param g
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws PolicyNotFoundException
     */
    private void mergeReplaceProperties ( VFSContext v, VFSFileEntity targetFile, VFSFileEntity f, User currentUser, Grant g )
            throws UserNotFoundException, AuthenticationException, PolicyNotFoundException {
        DateTime now = DateTime.now();
        targetFile.setFileSize(f.getFileSize());
        targetFile.setContentEncoding(f.getContentEncoding());
        targetFile.setContentType(f.getContentType());

        SecurityLabel securityLabel = f.getSecurityLabel();
        if ( securityLabel != null ) {
            Duration expDuration = this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getPolicy(securityLabel.getLabel())
                    .getDefaultExpirationDuration();
            if ( expDuration != null && f.getExpires() != null ) {
                DateTime newExpires = now.plus(expDuration).withTime(0, 0, 0, 0);
                if ( newExpires.isAfter(targetFile.getExpires()) ) {
                    f.setExpires(newExpires);
                }
            }
        }

        if ( this.ctx.getConfigurationProvider().getFrontendConfiguration().isAllowUserModificationTimes() && f.getContentLastModified() != null ) {
            targetFile.setContentLastModified(f.getContentLastModified());
        }
        else {
            targetFile.setContentLastModified(now);
        }
        ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, targetFile, g);
    }


    /**
     * @param f
     * @param em
     * @param target
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws PolicyNotFoundException
     * @throws InvalidEntityException
     */
    private void setupUploadProperties ( EntityTransactionContext tx, VFSContext v, VFSFileEntity f, VFSContainerEntity target,
            Set<String> elementNames, User currentUser, Grant g )
            throws AuthenticationException, UserNotFoundException, PolicyNotFoundException, InvalidEntityException {
        ServiceUtil.checkFileName(f.getLocalName());
        f.setLocalName(ServiceUtil.uniquifyLocalNameConflict(f.getLocalName(), elementNames, target, currentUser, g));
        f.setOwner(target.getOwner());

        if ( f.getSecurityLabel() == null ) {
            f.setSecurityLabel(ServiceUtil.deriveSecurityLabel(
                tx,
                this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration(),
                target,
                currentUser != null ? currentUser.getSecurityLabel() : null));
        }

        DateTime now = DateTime.now();

        Duration expDuration = this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getPolicy(f.getSecurityLabel().getLabel())
                .getDefaultExpirationDuration();
        if ( expDuration != null ) {
            f.setExpires(now.plus(expDuration).withTime(0, 0, 0, 0));

        }
        if ( !this.ctx.getConfigurationProvider().getFrontendConfiguration().isAllowUserModificationTimes() || f.getContentLastModified() == null ) {
            f.setContentLastModified(now);
        }
        ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, f, g);
        ServiceUtil.setCreated(currentUser, now, f, g);
        ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, target, g);
    }

}
