/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

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
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService;
import eu.agno3.fileshare.service.chunks.internal.ChunkConfiguration;
import eu.agno3.fileshare.service.chunks.internal.ChunkUploadUtil;
import eu.agno3.fileshare.service.chunks.internal.PropertiesMetaImpl;

import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.SmbTreeHandle;
import jcifs.smb.SmbFile;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ChunkUploadService.class
}, configurationPid = "chunks.smb", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SMBChunkUploadService extends BaseChunkUploadService {

    /**
     * 
     */
    private static final String CTX_PATTERN = "ctx-*"; //$NON-NLS-1$
    private static final String CHUNK_PROPERTIES = "chunk.properties"; //$NON-NLS-1$
    private static final String CHUNK_PROPERTIES_REOWN = "chunk.properties.reown"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(SMBChunkUploadService.class);

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


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        try ( SmbResource root = getUploadRoot() ) {
            root.mkdirs();
        }
        catch ( IOException e ) {
            log.warn("Failed to create upload directory", e); //$NON-NLS-1$
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
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    private SmbResource getUserPath () throws AuthenticationException, UserNotFoundException, CIFSException {
        return getUploadRoot().resolve(getUserId() + '/');
    }


    private SmbResource getUploadRoot () throws CIFSException {
        return this.clientProvider.getRoot().resolve("uploads/"); //$NON-NLS-1$
    }


    protected SmbResource getContextPath ( String token ) throws UserNotFoundException, AuthenticationException, IOException {
        checkToken(token);
        try ( SmbResource userPath = getUserPath() ) {
            if ( !userPath.exists() ) {
                userPath.mkdir();
            }

            return userPath.resolve(String.format(
                "ctx-%s/", //$NON-NLS-1$
                token != null ? token : StringUtils.EMPTY));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService#ensureAndCheck(eu.agno3.fileshare.model.VFSFileEntity,
     *      java.lang.String)
     */
    @Override
    protected boolean ensureAndCheck ( VFSFileEntity f, String token ) throws AuthenticationException, UserNotFoundException, IOException {
        try ( SmbResource contextPath = getContextPath(token) ) {
            if ( !contextPath.exists() ) {
                contextPath.mkdir();
                return true;
            }
            return false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService#contextExists(java.lang.String)
     */
    @Override
    protected boolean contextExists ( String ref ) throws UserNotFoundException, AuthenticationException, IOException {
        try ( SmbResource contextPath = getContextPath(ref) ) {
            return contextPath.exists();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService#initContext(java.lang.String,
     *      eu.agno3.fileshare.model.EntityKey, java.util.Properties)
     */
    @Override
    protected ChunkContext initContext ( String token, EntityKey target, Properties props )
            throws IOException, UserNotFoundException, AuthenticationException {
        if ( log.isDebugEnabled() ) {
            log.debug("Initializing context for " + token); //$NON-NLS-1$
        }

        try ( SmbResource ctxp = getContextPath(token) ) {
            try ( SmbResource propsFile = ctxp.resolve(CHUNK_PROPERTIES);
                  OutputStream fos = propsFile.openOutputStream() ) {
                props.store(fos, "Chunk info"); //$NON-NLS-1$
            }

            PropertiesMetaImpl meta = new PropertiesMetaImpl(target, props); // $NON-NLS-1$
            SMBChunkContextImpl ctx = new SMBChunkContextImpl(meta, ctxp);
            return ctx;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService#loadContext(java.lang.String)
     */
    @Override
    protected ChunkContext loadContext ( String ref ) throws FileshareException, IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Loading context for " + ref); //$NON-NLS-1$
        }

        try ( SmbResource ctxp = getContextPath(ref) ) {
            return loadContext(ctxp);
        }
    }


    /**
     * @param ctxp
     * @return
     * @throws IOException
     * @throws CIFSException
     */
    private ChunkContext loadContext ( SmbResource ctxp ) throws IOException, CIFSException {
        Properties props = new Properties();
        try ( SmbResource propsFile = ctxp.resolve(CHUNK_PROPERTIES);
              InputStream fis = propsFile.openInputStream() ) {
            props.load(fis);
        }

        EntityKey entityKey = parseEntityKey(props.getProperty("target-id")); //$NON-NLS-1$
        PropertiesMetaImpl meta = new PropertiesMetaImpl(entityKey, props);
        SMBChunkContextImpl ctx = new SMBChunkContextImpl(meta, ctxp);
        return ctx;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#removeChunkContext(java.lang.String)
     */
    @Override
    public void removeChunkContext ( String ref ) throws FileshareException, IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Removing context for " + ref); //$NON-NLS-1$
        }

        try ( SmbResource contextPath = getContextPath(ref) ) {
            contextPath.delete();
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#reownContext(java.lang.String)
     */
    @Override
    public ChunkContext reownContext ( String ref ) throws IOException, FileshareException {
        try ( SmbResource contextPath = getContextPath(ref) ) {

            if ( !contextPath.exists() ) {
                throw new ChunkUploadCanceledException();
            }

            ChunkContext oldCtx = getChunkContext(ref);
            if ( oldCtx.getMetadata().isExternalSource() ) {
                throw new UnsupportedOperationException("Cannot re-own context from external source"); //$NON-NLS-1$
            }

            try ( SmbTreeHandle f = ( (SmbFile) contextPath ).getTreeHandle() ) {
                if ( !f.isSMB2() ) {
                    log.debug("No SMB2 support, not reowning context"); //$NON-NLS-1$
                    return getChunkContext(ref);
                }
            }

            UUID newId = UUID.randomUUID();
            String newIdStr = newId.toString();
            Properties props = new Properties();
            try ( SmbResource propsFile = contextPath.resolve(CHUNK_PROPERTIES);
                  InputStream fs = propsFile.openInputStream() ) { // $NON-NLS-1$
                props.load(fs);
                props.setProperty("reference", newIdStr); //$NON-NLS-1$
            }

            try ( SmbResource newPropsFile = contextPath.resolve(CHUNK_PROPERTIES_REOWN);
                  OutputStream os = newPropsFile.openOutputStream() ) { // $NON-NLS-1$
                props.store(os, "Reowned context"); //$NON-NLS-1$
            }

            try ( SmbResource newContextPath = getContextPath(newIdStr) ) {
                // move context
                contextPath.renameTo(newContextPath);
                try ( SmbResource reownProps = newContextPath.resolve(CHUNK_PROPERTIES_REOWN);
                      SmbResource tgtProps = newContextPath.resolve(CHUNK_PROPERTIES) ) {
                    reownProps.renameTo(tgtProps, true);
                }
                catch ( IOException e ) {
                    // try to restore
                    newContextPath.renameTo(contextPath);
                    throw e;
                }
                return getChunkContext(newIdStr);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#getIncompleteContexts()
     */
    @Override
    public List<ChunkContext> getIncompleteContexts () throws FileshareException, IOException {
        List<ChunkContext> res = new LinkedList<>();
        try ( SmbResource userPath = getUserPath() ) {
            if ( !userPath.exists() ) {
                return res;
            }
            try ( CloseableIterator<SmbResource> contexts = userPath.children(CTX_PATTERN) ) {
                while ( contexts.hasNext() ) {
                    try ( SmbResource ctx = contexts.next() ) {
                        ChunkContext cc = loadContext(ctx);
                        res.add(cc);
                    }
                }
            }
        }
        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadService#getNumIncompleteContexts()
     */
    @Override
    public int getNumIncompleteContexts () throws FileshareException {
        int n = 0;
        try ( SmbResource userPath = getUserPath() ) {
            if ( !userPath.exists() ) {
                return 0;
            }
            try ( CloseableIterator<SmbResource> contexts = userPath.children(CTX_PATTERN) ) {
                while ( contexts.hasNext() ) {
                    try ( SmbResource ctx = contexts.next() ) {
                        n++;
                    }
                }
            }
        }
        catch ( CIFSException e ) {
            log.warn("Failed to enumerate contexts", e); //$NON-NLS-1$
        }
        return n;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService#getIncompleteSize()
     */
    @Override
    protected long getIncompleteSize () {
        long size = 0;
        try ( SmbResource userPath = getUserPath() ) {
            if ( !userPath.exists() ) {
                return 0;
            }
            try ( CloseableIterator<SmbResource> contexts = getUserPath().children(CTX_PATTERN) ) {
                while ( contexts.hasNext() ) {
                    try ( SmbResource ctx = contexts.next();
                          SmbResource data = ctx.resolve("data") ) { //$NON-NLS-1$
                        if ( data.exists() ) {
                            // for simplicity/efficiency we are only counting the data file here
                            size += data.length();
                        }
                    }
                }

            }
        }
        catch (
            CIFSException |
            UserNotFoundException |
            AuthenticationException e ) {
            log.warn("Failed to enumerate context size", e); //$NON-NLS-1$
        }
        return size;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService#ensureFreeSpace(java.lang.String, long,
     *      long, long)
     */
    @Override
    protected void ensureFreeSpace ( String ref, long uploadSize, long used, long limit ) throws IOException, FileshareException {
        long curContextSize = 0;
        try ( SmbResource ctx = getContextPath(ref);
              SmbResource data = ctx.resolve("data") ) { //$NON-NLS-1$
            if ( data.exists() ) {
                curContextSize = data.length();
            }

            if ( used + uploadSize - curContextSize > limit ) {
                long exzess = limit + curContextSize - uploadSize;

                if ( !makeFreeSpace(getUserPath(), ctx, exzess) ) {
                    throw new QuotaExceededException();
                }
            }
        }

    }


    /**
     * @param userPath
     * @param currentContext
     * @param needed
     * @return
     * @throws IOException
     * @throws FileshareException
     */
    private boolean makeFreeSpace ( SmbResource userPath, SmbResource currentContext, long needed ) throws FileshareException, IOException {
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

            if ( ! ( ctx instanceof SMBChunkContextImpl ) ) {
                continue;
            }

            SMBChunkContextImpl fc = (SMBChunkContextImpl) ctx;
            try ( SmbResource ctxPath = fc.getContextPath() ) {
                if ( currentContext.equals(ctxPath) ) {
                    continue;
                }

                long size;
                try ( SmbResource data = ctxPath.resolve("data") ) { //$NON-NLS-1$
                    size = data.length();
                }
                removeChunkContext(ctx.getMetadata().getReference());
                freed += size;
            }
        }

        return freed >= needed;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkUploadService#runCleanup()
     */
    @Override
    protected void runCleanup () {

        try ( SmbResource root = getUploadRoot();
              CloseableIterator<SmbResource> chldren = root.children() ) {
            while ( chldren.hasNext() ) {
                try ( SmbResource userDir = chldren.next() ) {
                    if ( !userDir.isDirectory() ) {
                        continue;
                    }

                    if ( this.exit.get() ) {
                        throw new ChunkUploadUtil.BreakException();
                    }

                    DateTime expireBefore;
                    if ( userDir.getName().startsWith("tok-") ) { //$NON-NLS-1$
                        expireBefore = DateTime.now().minus(getConfiguration().getFrontendConfiguration().getTokenIncompleteExpireDuration());
                    }
                    else {
                        expireBefore = DateTime.now().minus(getConfiguration().getFrontendConfiguration().getUserIncompleteExpireDuration());
                    }

                    try ( CloseableIterator<SmbResource> userContexts = userDir.children(CTX_PATTERN) ) {
                        boolean haveValidContext = false;
                        while ( userContexts.hasNext() ) {
                            try ( SmbResource userContextDir = userContexts.next() ) {
                                ChunkContext ctx = loadContext(userContextDir);
                                DateTime lastMod = ctx.getLastModified();

                                if ( ctx.getState().getState() != UploadState.UPLOADING && lastMod.isBefore(expireBefore) ) {
                                    try {
                                        userContextDir.delete();
                                    }
                                    catch ( IOException ex ) {
                                        log.warn("Failed to delete user/session directory", ex); //$NON-NLS-1$
                                    }
                                }
                                else if ( ctx.getState().getState() == UploadState.UPLOADING || ctx.getState().getState() == UploadState.INITIAL ) {
                                    haveValidContext = true;
                                    long diff = System.currentTimeMillis() - lastMod.getMillis();
                                    if ( log.isDebugEnabled() ) {
                                        log.debug(String.format("Is currently uploading %s last write %d ms ago", userContextDir, diff)); //$NON-NLS-1$
                                    }

                                    if ( diff > this.getUploadTimeout().getMillis() ) {
                                        log.debug("Upload timed out"); //$NON-NLS-1$
                                        ctx.getState().setState(UploadState.FAILED);
                                    }
                                }
                                else {
                                    haveValidContext = true;
                                    if ( log.isDebugEnabled() ) {
                                        log.debug(String.format(
                                            "%s state %s, within expiration threshold, lastmod %s", //$NON-NLS-1$
                                            userContextDir,
                                            ctx.getState().getState(),
                                            lastMod));
                                    }
                                }
                            }
                        }

                        if ( this.exit.get() ) {
                            return;
                        }

                        if ( !haveValidContext ) {
                            try {
                                userDir.delete();
                            }
                            catch ( IOException e ) {
                                getLog().warn("Failed to remove user dir " + userDir, e); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
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

}
