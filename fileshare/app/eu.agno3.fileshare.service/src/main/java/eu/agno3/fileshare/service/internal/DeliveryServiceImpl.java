/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.EofException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.exceptions.NamingConflictException;
import eu.agno3.fileshare.exceptions.TokenValidationException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.tokens.AccessToken;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.service.LinkService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.DeliveryService;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.archive.internal.ArchiveContext;
import eu.agno3.fileshare.service.archive.internal.TarArchiveContext;
import eu.agno3.fileshare.service.archive.internal.ZipArchiveContext;
import eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder;
import eu.agno3.fileshare.service.audit.MultiEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.audit.SingleEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.config.ViewPolicyConfiguration;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.util.FilenameUtil;
import eu.agno3.fileshare.vfs.RequestRange;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.http.ua.UACapability;
import eu.agno3.runtime.http.ua.UADetector;

import net.sf.uadetector.ReadableUserAgent;


/**
 * @author mbechler
 *
 */
@Component ( service = DeliveryService.class )
public class DeliveryServiceImpl implements DeliveryService {

    /**
     * 
     */
    private static final String INLINE = "inline"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String TOKEN_PARAM = "token"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
    private static final String CONTENT_DISPOSITION = "Content-Disposition"; //$NON-NLS-1$
    private static final String X_DOWNLOAD_OPTIONS = "X-Download-Options"; //$NON-NLS-1$
    private static final String STRICT_CSP = "default-src 'self'"; //$NON-NLS-1$

    private static final Set<String> COMPRESSED_TYPES = new HashSet<>(Arrays.asList(
        "image/jpeg", //$NON-NLS-1$
        "image/gif", //$NON-NLS-1$
        "image/png", //$NON-NLS-1$

        "audio/aac", //$NON-NLS-1$
        "audio/mp4", //$NON-NLS-1$
        "audio/mpeg", //$NON-NLS-1$
        "audio/ogg", //$NON-NLS-1$
        "audio/webm", //$NON-NLS-1$

        "video/mpeg", //$NON-NLS-1$
        "video/mp4", //$NON-NLS-1$
        "video/ogg", //$NON-NLS-1$
        "video/webm", //$NON-NLS-1$
        "video/x-m4v", //$NON-NLS-1$
        "video/x-msvideo", //$NON-NLS-1$

        "application/zip", //$NON-NLS-1$
        "application/x-gzip", //$NON-NLS-1$
        "application/x-bzip2" //$NON-NLS-1$
    ));

    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'") //$NON-NLS-1$
            .withZoneUTC().withLocale(Locale.US);

    private static final Logger log = Logger.getLogger(DeliveryServiceImpl.class);
    private static final int BUFFER_SIZE = 65535;

    private DefaultServiceContext ctx;

    private AccessControlService accessControl;
    private LinkService linkService;

    private PolicyEvaluator policyEvaluator;

    private UADetector uaDetector;
    private VFSServiceInternal vfs;


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
    protected synchronized void setUADetector ( UADetector uad ) {
        this.uaDetector = uad;
    }


    protected synchronized void unsetUADetector ( UADetector uad ) {
        if ( this.uaDetector == uad ) {
            this.uaDetector = null;
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
    protected synchronized void setLinkService ( LinkService ls ) {
        this.linkService = ls;
    }


    protected synchronized void unsetLinkService ( LinkService ls ) {
        if ( this.linkService == ls ) {
            this.linkService = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.DeliveryService#doDeliverFile(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, eu.agno3.fileshare.model.EntityKey, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void doDeliverFile ( HttpServletRequest req, HttpServletResponse resp, EntityKey id, String fileName, String disposition )
            throws IOException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            SingleEntityFileshareAuditBuilder builder = audit.builder();
            builder.access(this.accessControl).action("DOWNLOAD"); //$NON-NLS-1$

            try ( VFSContext v = this.vfs.getVFS(id).begin(true) ) {
                VFSFileEntity file = v.load(id, VFSFileEntity.class);

                if ( file == null ) {
                    builder.fail(AuditStatus.VALIDATION);
                    log.debug("File not found " + id); //$NON-NLS-1$
                    resp.sendError(404);
                    return;
                }

                builder.entity(file);
                builder.parentEntity(v.getParent(file));

                String decode = FilenameUtil.decodeFileName(fileName);
                if ( !"file".equals(decode) && !decode.equals(file.getLocalName()) ) { //$NON-NLS-1$
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Filename mismatch: from url '%s' from file '%s'", decode, file.getLocalName())); //$NON-NLS-1$
                    }
                    builder.fail(AuditStatus.VALIDATION);
                    resp.sendError(404);
                    return;
                }

                if ( !this.checkValidToken(makeFileVerifyLocationUrl(v, file, req), req, resp) ) {
                    builder.fail(AuditStatus.UNAUTHENTICATED);
                    return;
                }

                if ( !this.accessControl.hasAccess(v, file, GrantPermission.READ) ) {
                    builder.fail(AuditStatus.UNAUTHORIZED);
                    resp.sendError(403, "Forbidden, the link you followed is no longer valid"); //$NON-NLS-1$
                    return;
                }

                if ( this.accessControl.isTokenAuth() ) {
                    try {
                        builder.grant(this.accessControl.getTokenAuthGrant(v, file));
                    }
                    catch ( GrantAuthenticationRequiredException e ) {
                        resp.sendError(403, "Forbidden, the grant is not authenticated"); //$NON-NLS-1$
                        return;
                    }
                }

                PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(v, file, req);
                if ( violation != null ) {
                    builder.policyViolation(violation);
                    resp.sendError(403, this.policyEvaluator.getViolationMessage(violation, getLocale(req)));
                    return;
                }

                setDefaultSafetyHeaders(req, resp);
                resp.setContentType(file.getContentType());
                resp.setHeader("Last-Modified", RFC1123_DATE_TIME_FORMATTER.print(file.getLastModified())); //$NON-NLS-1$
                resp.setHeader(
                    "Accept-Ranges", //$NON-NLS-1$
                    "bytes"); //$NON-NLS-1$

                if ( !this.makeContentDisposition(req, resp, file, disposition) ) {
                    builder.fail(AuditStatus.VALIDATION);
                    return;
                }

                RequestRange range = parseRange(req, file.getFileSize());
                if ( range != null ) {
                    audit.builder().property("rangeStart", range.getStart()); //$NON-NLS-1$
                    audit.builder().property("rangeEnd", range.getEnd()); //$NON-NLS-1$
                }

                transferFile(req, resp, v, file, range, builder);
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw new IOException("File transfer failed", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.DeliveryService#deliverDAV(eu.agno3.fileshare.vfs.VFSContext,
     *      eu.agno3.fileshare.model.VFSFileEntity, javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, java.io.OutputStream)
     */
    @Override
    public void deliverDAV ( VFSContext v, VFSFileEntity file, HttpServletRequest req, HttpServletResponse resp, OutputStream outputStream )
            throws IOException, FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            SingleEntityFileshareAuditBuilder builder = audit.builder();
            builder.access(this.accessControl).action("DOWNLOAD").entity(file).parentEntity(v.getParent(file)); //$NON-NLS-1$
            try {
                this.accessControl.checkAccess(v, file, GrantPermission.READ);
                this.policyEvaluator.checkPolicy(v, file, req);
                RequestRange range = parseRange(req, file.getFileSize());
                if ( range != null ) {
                    audit.builder().property("rangeStart", range.getStart()); //$NON-NLS-1$
                    audit.builder().property("rangeEnd", range.getEnd()); //$NON-NLS-1$
                }
                transferFile(req, resp, v, file, range, builder);
            }
            catch ( FileshareException e ) {
                builder.fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                builder.fail(AuditStatus.INTERNAL);
                throw e;
            }
        }
    }


    /**
     * @param req
     * @return
     */
    private static Locale getLocale ( HttpServletRequest req ) {
        Enumeration<Locale> locales = req.getLocales();
        Locale l = Locale.ROOT;
        if ( locales.hasMoreElements() ) {
            l = locales.nextElement();
        }
        return l;
    }


    /**
     * @param resp
     * @param file
     * @param range
     */
    protected void transferFile ( HttpServletRequest req, HttpServletResponse resp, VFSContext v, VFSFileEntity file, RequestRange range,
            AbstractFileshareAuditBuilder<?, ?> builder ) {

        try ( VFSContentHandle ch = v.getContents(file, range) ) {

            long actualSize = file.getFileSize();

            if ( ch.haveStoredSize() ) {
                actualSize = ch.getStoredSize();
            }

            if ( range == null ) {
                doFullTransfer(req, resp, file, actualSize, ch);
            }
            else {
                doRangeTransfer(req, resp, file, range, actualSize, ch);
            }
        }
        catch (
            EofException |
            ClosedChannelException e ) {
            builder.fail(AuditStatus.INTERNAL);
            log.debug("remote closed connection", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            builder.fail(AuditStatus.INTERNAL);
            log.error("Failed to deliver file", e); //$NON-NLS-1$
        }
    }


    /**
     * @param req
     * @param resp
     * @param file
     * @param range
     * @param ch
     * @throws IOException
     */
    protected void doRangeTransfer ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity file, RequestRange range, long actualSize,
            VFSContentHandle ch ) throws IOException {
        long length = range.getEnd() - range.getStart() + 1;

        if ( range.getEnd() > actualSize ) {
            resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        resp.setStatus(206);
        resp.setContentLengthLong(length);
        String contentRangeHeader = String.format("bytes %d-%d/%d", range.getStart(), range.getEnd(), actualSize); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug("Sending content range: " + contentRangeHeader); //$NON-NLS-1$
        }
        resp.setHeader("Content-Range", contentRangeHeader); //$NON-NLS-1$
        if ( !"HEAD".equals(req.getMethod()) ) { //$NON-NLS-1$
            long transferred = ch.transferTo(resp.getOutputStream(), range.getStart(), length, new byte[BUFFER_SIZE]);
            if ( transferred != length ) {
                log.warn(String.format("Range transfer size mismatch, expected %d have %d", length, transferred)); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param req
     * @param resp
     * @param file
     * @param ch
     * @throws IOException
     */
    protected void doFullTransfer ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity file, long actualSize, VFSContentHandle ch )
            throws IOException {
        boolean gzip = this.ctx.getConfigurationProvider().getFrontendConfiguration().isEnableCompression() && shouldGZIP(req, file);
        if ( !gzip ) {
            resp.setContentLengthLong(actualSize);
        }
        else {
            log.debug("Sending gzipped"); //$NON-NLS-1$
            resp.setHeader(
                "Content-Encoding", //$NON-NLS-1$
                "gzip"); //$NON-NLS-1$
        }
        if ( !"HEAD".equals(req.getMethod()) ) { //$NON-NLS-1$
            OutputStream os = resp.getOutputStream();
            long transferred;
            if ( gzip ) {
                try ( GZIPOutputStream gzos = new GZIPOutputStream(os) ) {
                    transferred = ch.transferTo(gzos, new byte[BUFFER_SIZE]);
                }
            }
            else {
                transferred = ch.transferTo(os, new byte[BUFFER_SIZE]);
            }
            if ( transferred != actualSize ) {
                log.warn(String.format("File size mismatch, expected %d have %d", file.getFileSize(), transferred)); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param req
     * @param v
     * @return
     */
    static boolean shouldGZIP ( HttpServletRequest req, VFSFileEntity v ) {
        if ( shouldCompress(v) ) {
            return containsGzip(req.getHeader("Accept-Encoding")); //$NON-NLS-1$
        }
        return false;
    }


    /**
     * @param gzip
     * @param header
     * @return
     */
    static boolean containsGzip ( String header ) {
        if ( !StringUtils.isBlank(header) ) {
            String[] values = StringUtils.split(header, ',');
            for ( String value : values ) {
                if ( "gzip".equalsIgnoreCase(value.trim()) ) { //$NON-NLS-1$
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * @param req
     * 
     */
    private static RequestRange parseRange ( HttpServletRequest req, long totalLength ) {
        String rangeHeader = req.getHeader("Range"); //$NON-NLS-1$

        if ( StringUtils.isBlank(rangeHeader) ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Got range request: " + rangeHeader); //$NON-NLS-1$
        }

        int rangeSepPos = rangeHeader.indexOf('=');
        if ( rangeSepPos >= 0 ) {
            // starts with a unit
            String unit = rangeHeader.substring(0, rangeSepPos);

            if ( !"bytes".equals(unit) ) { //$NON-NLS-1$
                log.debug("Invalid range unit " + unit); //$NON-NLS-1$
                return null;
            }
            rangeHeader = rangeHeader.substring(rangeSepPos + 1);
        }

        String parts[] = StringUtils.split(rangeHeader, ',');

        if ( parts == null || parts.length == 0 ) {
            return null;
        }

        long start = 0;
        long end = totalLength - 1;

        try {
            // TODO: we only process the first part, this should be sufficient for most cases
            String part = parts[ 0 ];

            if ( part.charAt(0) == '-' ) {
                // suffix range specification
                long suffixLength = Long.parseLong(part.substring(1));
                start = Math.max(0, end - suffixLength);
            }
            else {
                int rangePos = part.indexOf('-');

                if ( rangePos >= 0 ) {
                    // this is a limited range
                    start = Math.max(0, Long.parseLong(part.substring(0, rangePos)));

                    if ( rangePos != part.length() - 1 ) {
                        end = Math.min(end, Long.parseLong(part.substring(rangePos + 1)));
                    }
                }
                else {
                    // this is a offset range
                    start = Math.max(0, Long.parseLong(part));
                }

                if ( start >= end ) {
                    return null;
                }
            }
        }
        catch ( NumberFormatException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to parse range header " + rangeHeader, e); //$NON-NLS-1$
            }
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Range is %s - %s", start, end)); //$NON-NLS-1$
        }

        return new RequestRange(start, end);

    }


    /**
     * @param parent
     * @param resp
     * @param req
     * @return
     * @throws IOException
     * @throws TokenValidationException
     * 
     */
    private boolean checkValidToken ( String verifyLocation, HttpServletRequest req, HttpServletResponse resp ) throws IOException {

        if ( this.accessControl.isTokenAuth() ) {
            log.debug("Running with token authentication"); //$NON-NLS-1$

            if ( this.accessControl.isUserAuthenticated() && !validateIntent() ) {
                log.debug("Also running with session authentication, intent not verified"); //$NON-NLS-1$

                if ( verifyLocation != null ) {
                    resp.setHeader("Location", verifyLocation); //$NON-NLS-1$
                    resp.setStatus(302);
                }
                return false;
            }
        }
        else if ( !this.accessControl.haveIntent() ) {
            if ( verifyLocation != null ) {
                log.debug("Do not have intent"); //$NON-NLS-1$
                resp.setHeader("Location", verifyLocation); //$NON-NLS-1$
                resp.setStatus(302);
            }
            return false;
        }

        return true;
    }


    /**
     * @param tokenAuthValue
     */
    private boolean validateIntent () {

        AccessToken tokenAuthValue = this.accessControl.getTokenAuthValue();
        if ( !tokenAuthValue.isWithIntent() ) {
            return false;
        }

        if ( tokenAuthValue.getWithIntentSessionId() != null ) {
            return tokenAuthValue.getWithIntentSessionId().equals(this.accessControl.getSessionId());
        }
        return false;

    }


    /**
     * @param e
     * @param req
     * @return
     * @throws FileshareException
     * @throws GrantAuthenticationRequiredException
     */
    private String makeFileVerifyLocationUrl ( VFSContext v, VFSFileEntity e, HttpServletRequest req ) throws FileshareException {
        TokenGrant tokenAuthGrant = null;
        try {
            tokenAuthGrant = this.accessControl.getTokenAuthGrant(v, e);
        }
        catch ( GrantAuthenticationRequiredException ex ) {
            log.warn("Failed to get token auth grant as password authentication is required", ex); //$NON-NLS-1$
        }
        return this.linkService.makeFrontendViewLink(
            e,
            String.format(
                "&grant=%s&token=%s", //$NON-NLS-1$
                tokenAuthGrant != null ? tokenAuthGrant.getId().toString() : StringUtils.EMPTY,
                req.getParameter(TOKEN_PARAM) != null ? req.getParameter(TOKEN_PARAM) : StringUtils.EMPTY),
            req.getContextPath());
    }


    /**
     * @param req
     * @param resp
     * @param disposition
     * @throws IOException
     */
    private boolean makeContentDisposition ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity file, String disposition )
            throws IOException {

        String rawUa = this.uaDetector.getUA(req);
        ReadableUserAgent ua = this.uaDetector.parse(req);
        String mimeType = file.getContentType();
        ViewPolicyConfiguration viewPolicyConfig = this.ctx.getConfigurationProvider().getViewPolicyConfig();
        if ( ua == null || !INLINE.equals(disposition) || ! ( viewPolicyConfig.isViewable(mimeType) || viewPolicyConfig.isSafe(mimeType) ) ) {
            // could not detect user agent, user requested attachment anyways or file is not declared viewable
            // -> force download
            log.debug("Not viewable, or attachment requested"); //$NON-NLS-1$
            setCSPHeader(req, resp, ua, rawUa, makeDefaultCSPHeader(req));
            forceDownload(resp, file, true);
            return true;
        }

        boolean relaxedCSP = viewPolicyConfig.getRelaxedCSPMimeTypes().contains(mimeType);

        if ( this.uaDetector.hasCapability(UACapability.CSP10, ua, rawUa) && viewPolicyConfig.getNoSandboxMimeTypes().contains(mimeType) ) {
            // browser supports real CSP, sandboxing is disabled for this type
            return withCSPDisposition(req, resp, file, ua, rawUa, true, relaxedCSP);
        }
        else if ( this.uaDetector.hasCapability(UACapability.CSP10, ua, rawUa)
                && this.uaDetector.hasCapability(UACapability.CSP10_SANDBOXING, ua, rawUa) ) {
            // browser supports real CSP and CSP sandboxing, everything is alright
            return withCSPDisposition(req, resp, file, ua, rawUa, false, relaxedCSP);
        }
        else if ( this.uaDetector.hasCapability(UACapability.CSP10_SANDBOXING, ua, rawUa)
                && !this.uaDetector.hasCapability(UACapability.CSP10, ua, rawUa) ) {
            // browser supports sandboxing, but no real CSP (IE)
            // should make sure that no html documents are processed by the browser
            // so that no external resources are pulled
            return noCSPDisposition(req, resp, file, ua, rawUa);
        }
        else if ( this.uaDetector.hasCapability(UACapability.CSP10, ua, rawUa)
                && !this.uaDetector.hasCapability(UACapability.CSP10_SANDBOXING, ua, rawUa)
                && this.uaDetector.hasCapability(UACapability.FRAME_SANDBOXING, ua, rawUa) ) {
            // browser supports CSP and iframe sandboxing but not CSP sandboxing
            // need to check that document is really embedded into iframe sandbox
            return noCSPSandboxDisposition(req, resp, file, ua, rawUa, relaxedCSP);
        }
        else if ( viewPolicyConfig.isSafe(mimeType) && INLINE.equals(disposition) ) {
            // is a safe type, deliver anyways
            setCSPHeader(req, resp, ua, rawUa, makeDefaultCSPHeader(req));
            transferInline(resp, file);
            return true;
        }
        else {
            // browser supports neither CSP nor sandboxing -> force download
            return fallbackDisposition(req, resp, file, ua, rawUa);
        }
    }


    /**
     * @param req
     * @param resp
     * @param file
     * @param ua
     * @param rawUa
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private boolean noCSPDisposition ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity file, ReadableUserAgent ua, String rawUa )
            throws UnsupportedEncodingException, IOException {
        log.debug("no real CSP support"); //$NON-NLS-1$
        setCSPHeader(req, resp, ua, rawUa, makeInlineCSPHeader(ua, rawUa, req, false, false));

        String contentType = resp.getContentType();
        ViewPolicyConfiguration viewPolicyConfig = this.ctx.getConfigurationProvider().getViewPolicyConfig();
        if ( !viewPolicyConfig.isSafe(contentType) && !viewPolicyConfig.getRelaxedCSPMimeTypes().contains(contentType) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Denying access as this is not a safe type " + contentType); //$NON-NLS-1$
            }
            resp.sendError(403, "You browser does not support the necessary security features to access this resource"); //$NON-NLS-1$
            return false;
        }

        transferInline(resp, file);
        return true;
    }


    /**
     * @param req
     * @param resp
     * @param file
     * @param ua
     * @param rawUa
     * @return
     * @throws UnsupportedEncodingException
     */
    private boolean withCSPDisposition ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity file, ReadableUserAgent ua, String rawUa,
            boolean disableSandbox, boolean relaxed ) throws UnsupportedEncodingException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("proper support (disable sandbox = %s)", disableSandbox)); //$NON-NLS-1$
        }
        setCSPHeader(req, resp, ua, rawUa, makeInlineCSPHeader(ua, rawUa, req, disableSandbox, relaxed));
        transferInline(resp, file);
        return true;
    }


    /**
     * @param req
     * @param resp
     * @param file
     * @param ua
     * @param rawUa
     * @return
     * @throws UnsupportedEncodingException
     */
    private boolean noCSPSandboxDisposition ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity file, ReadableUserAgent ua,
            String rawUa, boolean relaxed ) throws UnsupportedEncodingException {
        log.debug("Don't have CSP sandbox support"); //$NON-NLS-1$

        String referer = req.getHeader("Referer"); //$NON-NLS-1$

        if ( StringUtils.isBlank(referer) ) {
            log.debug("No referer, force download"); //$NON-NLS-1$
            forceDownload(resp, file, false);
            return true;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Referer is " + referer); //$NON-NLS-1$
        }
        setCSPHeader(req, resp, ua, rawUa, makeInlineCSPHeader(ua, rawUa, req, false, relaxed));

        if ( !isValidSandboxReferer(req, file, referer) ) {
            log.debug("Not a valid referer, forcing download"); //$NON-NLS-1$
            forceDownload(resp, file, false);
            return true;
        }

        transferInline(resp, file);
        return true;
    }


    /**
     * @param req
     * @param resp
     * @param file
     * @param ua
     * @param rawUa
     * @return
     * @throws UnsupportedEncodingException
     */
    private boolean fallbackDisposition ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity file, ReadableUserAgent ua, String rawUa )
            throws UnsupportedEncodingException {

        if ( log.isDebugEnabled() ) {
            log.debug("unsupported " + ua); //$NON-NLS-1$
            log.debug("csp10 " + this.uaDetector.hasCapability(UACapability.CSP10, ua, rawUa)); //$NON-NLS-1$
            log.debug("csp10_sandbox " + this.uaDetector.hasCapability(UACapability.CSP10_SANDBOXING, ua, rawUa)); //$NON-NLS-1$
            log.debug("frame_sandbox " + this.uaDetector.hasCapability(UACapability.FRAME_SANDBOXING, ua, rawUa)); //$NON-NLS-1$
        }
        setCSPHeader(req, resp, ua, rawUa, makeDefaultCSPHeader(req));
        forceDownload(resp, file, true);
        return true;
    }


    /**
     * @param req
     * @param file
     * @param referer
     * @return
     * @throws UnsupportedEncodingException
     */
    private boolean isValidSandboxReferer ( HttpServletRequest req, VFSFileEntity file, String referer ) throws UnsupportedEncodingException {
        String localServerBase = makeLocalServerBase(req);

        String views[] = new String[] {
            "actions/viewFile.xhtml", //$NON-NLS-1$
            "actions/viewFileDialog.xhtml", //$NON-NLS-1$
        };

        for ( String view : views ) {
            String expected = String.format(
                "%s%s?file=%s", //$NON-NLS-1$
                localServerBase,
                view,
                file.getEntityKey());
            if ( log.isDebugEnabled() ) {
                log.debug("Expected referer is " + expected); //$NON-NLS-1$
            }
            if ( referer.startsWith(expected) ) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param resp
     * @param file
     */
    private static void transferInline ( HttpServletResponse resp, VFSFileEntity file ) {
        resp.setHeader(CONTENT_DISPOSITION, String.format("inline;%s", ServiceUtil.encodeDispositionFilename(file.getLocalName()))); //$NON-NLS-1$
    }


    /**
     * @return
     * @throws UnsupportedEncodingException
     */
    private String makeInlineCSPHeader ( ReadableUserAgent ua, String rawUa, HttpServletRequest req, boolean disableSandbox, boolean relaxed )
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        sb.append(STRICT_CSP);

        if ( relaxed ) {
            // otherwise chrome won't be able to load media files in the sandbox
            String filePath = makeLocalServerBase(req);
            if ( filePath.charAt(filePath.length() - 1) == '/' ) {
                filePath = filePath.substring(0, filePath.length() - 1);
            }
            sb.append(String.format("; media-src %s/files/; object-src %s/files/;", filePath, filePath)); //$NON-NLS-1$
        }

        if ( this.uaDetector.hasCapability(UACapability.CSP10, ua, rawUa) && !disableSandbox ) {
            // we can enable scripting if CSP is in place
            // this is needed for e.g. pdf.js
            sb.append("; sandbox allow-scripts"); //$NON-NLS-1$
        }
        return sb.toString();
    }


    /**
     * @param req
     * @return
     * @throws UnsupportedEncodingException
     */
    private String makeLocalServerBase ( HttpServletRequest req ) throws UnsupportedEncodingException {
        try {
            String path = req.getContextPath();
            if ( StringUtils.isBlank(path) ) {
                path = "/"; //$NON-NLS-1$
            }
            else if ( path.charAt(path.length() - 1) != '/' ) {
                path = path + "/"; //$NON-NLS-1$
            }
            URI mod = new URI(
                req.isSecure() ? "https" : //$NON-NLS-1$
                        "http", //$NON-NLS-1$
                null,
                makeLocalServerName(req),
                ( req.isSecure() && req.getServerPort() != 443 ) || ( !req.isSecure() && req.getServerPort() != 80 ) ? req.getServerPort() : -1,
                path,
                null,
                null);
            return mod.toASCIIString();
        }
        catch ( URISyntaxException e ) {
            log.warn("Failed to build URI", e); //$NON-NLS-1$
            return this.ctx.getConfigurationProvider().getFrontendConfiguration().getWebFrontendURI().toString();
        }
    }


    /**
     * @param req
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String makeLocalServerName ( HttpServletRequest req ) throws UnsupportedEncodingException {
        return URLEncoder.encode(IDN.toASCII(req.getServerName()), UTF_8);
    }


    /**
     * @param resp
     * @param file
     */
    private static void forceDownload ( HttpServletResponse resp, VFSFileEntity file, boolean forceContentType ) {
        if ( forceContentType ) {
            // usually we force the content type for legacy browsers
            resp.setContentType(APPLICATION_OCTET_STREAM);
        }
        else {
            resp.setContentType(file.getContentType());
        }
        resp.setHeader(CONTENT_DISPOSITION, String.format("attachment;%s", ServiceUtil.encodeDispositionFilename(file.getLocalName()))); //$NON-NLS-1$
        resp.setHeader(X_DOWNLOAD_OPTIONS, "noopen"); //$NON-NLS-1$
    }


    /**
     * @param resp
     * @param ua
     * @throws UnsupportedEncodingException
     */
    private static void setDefaultSafetyHeaders ( HttpServletRequest req, HttpServletResponse resp ) throws UnsupportedEncodingException {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
        resp.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        resp.setHeader("Expires", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
        resp.setHeader("X-Content-Type-Options", "nosniff"); //$NON-NLS-1$ //$NON-NLS-2$
        resp.setHeader("X-Permitted-Cross-Domain-Policies", "none-this-response"); //$NON-NLS-1$//$NON-NLS-2$
    }


    /**
     * @param req
     * @param resp
     * @param ua
     * @param cspValue
     * @throws UnsupportedEncodingException
     */
    private void setCSPHeader ( HttpServletRequest req, HttpServletResponse resp, ReadableUserAgent ua, String rawUa, String cspValue )
            throws UnsupportedEncodingException {

        if ( ua != null && this.uaDetector.hasCapability(UACapability.CSP10_STANDARD_HEADER, ua, rawUa) ) {
            resp.setHeader("Content-Security-Policy", cspValue); //$NON-NLS-1$
        }
        else if ( ua != null && this.uaDetector.hasCapability(UACapability.CSP10_EXPERIMENTAL_HEADER, ua, rawUa) ) {
            resp.setHeader("X-Content-Security-Policy", cspValue); //$NON-NLS-1$
        }
        else if ( ua != null && this.uaDetector.hasCapability(UACapability.CSP10_WEBKIT_HEADER, ua, rawUa) ) {
            resp.setHeader("X-WebKit-CSP", cspValue); //$NON-NLS-1$
        }
        else {
            setAllCSPHeaders(req, resp, cspValue);
        }
    }


    /**
     * @param req
     * @param resp
     * @param ua
     * @param cspValue
     * @throws UnsupportedEncodingException
     */
    private static void setAllCSPHeaders ( HttpServletRequest req, HttpServletResponse resp, String cspValue ) throws UnsupportedEncodingException {
        resp.setHeader("Content-Security-Policy", cspValue); //$NON-NLS-1$
        resp.setHeader("X-Content-Security-Policy", cspValue); //$NON-NLS-1$
        resp.setHeader("X-WebKit-CSP", cspValue); //$NON-NLS-1$
    }


    /**
     * @param req
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String makeDefaultCSPHeader ( HttpServletRequest req ) {
        return STRICT_CSP;
    }


    /**
     * 
     * @param req
     * @param resp
     * @param id
     * @param fileName
     * @param type
     * @throws IOException
     */
    @Override
    public void doDeliverDirectory ( HttpServletRequest req, HttpServletResponse resp, EntityKey id, String fileName, ArchiveType type )
            throws IOException {
        try ( AuditContext<MultiEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(MultiEntityFileshareAuditBuilder.class) ) {
            MultiEntityFileshareAuditBuilder builder = audit.builder();
            builder.access(this.accessControl).action("DOWNLOAD_FOLDER"); //$NON-NLS-1$
            builder.property("archiveType", type.name()); //$NON-NLS-1$

            try ( VFSContext v = this.vfs.getVFS(id).begin(true) ) {
                VFSContainerEntity parent = v.load(id, VFSContainerEntity.class);

                if ( parent == null || fileName == null || !fileName.endsWith("." + type.getExtension()) ) { //$NON-NLS-1$
                    builder.fail(AuditStatus.VALIDATION);
                    resp.sendError(404);
                    return;
                }

                builder.parentEntity(parent);

                if ( !this.checkValidToken(makeArchiveVerifyLocationUrl(v, parent, type, req), req, resp) ) { // $NON-NLS-1$
                    builder.fail(AuditStatus.UNAUTHENTICATED);
                    return;
                }

                if ( !this.accessControl.hasAccess(v, parent, GrantPermission.READ) ) {
                    builder.fail(AuditStatus.UNAUTHORIZED);
                    resp.sendError(403, "Forbidden, the link you followed is no longer valid"); //$NON-NLS-1$
                    return;
                }

                if ( this.accessControl.isTokenAuth() ) {
                    try {
                        builder.grant(this.accessControl.getTokenAuthGrant(v, parent));
                    }
                    catch ( GrantAuthenticationRequiredException e ) {
                        resp.sendError(403, "Forbidden, the grant is not authenticated"); //$NON-NLS-1$
                        return;
                    }
                }

                PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(v, parent, req);
                if ( violation != null ) {
                    builder.policyViolation(violation);
                    resp.sendError(403, this.policyEvaluator.getViolationMessage(violation, getLocale(req)));
                    return;
                }

                Stack<String> pathStack = new Stack<>();

                ReadableUserAgent ua = this.uaDetector.parse(req);
                setCSPHeader(req, resp, ua, this.uaDetector.getUA(req), makeDefaultCSPHeader(req));

                resp.setContentType(type.getMimeType()); // $NON-NLS-1$
                resp.setHeader(CONTENT_DISPOSITION, "attachment;" + //$NON-NLS-1$
                        ServiceUtil.encodeDispositionFilename(getArchiveFileName(parent) + '.' + type.getExtension()));

                setDefaultSafetyHeaders(req, resp);
                resp.setHeader(X_DOWNLOAD_OPTIONS, "noopen"); //$NON-NLS-1$

                try ( OutputStream fos = resp.getOutputStream();
                      BufferedOutputStream bos = new BufferedOutputStream(fos);
                      ArchiveContext cc = createArchiveContext(type, bos) ) {

                    if ( !StringUtils.isEmpty(parent.getLocalName()) ) {
                        pathStack.push(parent.getLocalName());
                    }

                    addArchiveChildren(v, parent, pathStack, cc, req, builder);
                }
                catch ( IOException e ) {
                    builder.fail(AuditStatus.INTERNAL);
                    throw e;
                }

                catch ( Exception e ) {
                    builder.fail(AuditStatus.INTERNAL);
                    log.error("Failed to deliver directory archive file", e); //$NON-NLS-1$
                    resp.sendError(500);
                }
            }
            catch ( FileshareException e ) {
                builder.fail(e);
                log.warn("Failed to deliver directory file", e); //$NON-NLS-1$
                resp.sendError(500);
            }
        }
    }


    /**
     * @param bos
     * @return
     * @throws IOException
     */
    protected ArchiveContext createArchiveContext ( ArchiveType at, BufferedOutputStream bos ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Archive type is " + at); //$NON-NLS-1$
        }
        switch ( at ) {
        case TAR:
        case TBZ2:
        case TGZ:
            return new TarArchiveContext(bos, BUFFER_SIZE, at);
        case ZIP:
        case ZIPU:
            return new ZipArchiveContext(bos, BUFFER_SIZE, at);
        default:
            throw new IOException("Unsupported archive type " + at.name()); //$NON-NLS-1$

        }

    }


    @Override
    public void doDeliverMultiple ( HttpServletRequest req, HttpServletResponse resp, Set<EntityKey> entityIds, ArchiveType type )
            throws IOException {
        try ( AuditContext<MultiEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(MultiEntityFileshareAuditBuilder.class) ) {
            MultiEntityFileshareAuditBuilder builder = audit.builder();
            builder.access(this.accessControl).action("DOWNLOAD_MULTI"); //$NON-NLS-1$
            builder.property("archiveType", type.name()); //$NON-NLS-1$

            try ( VFSContext v = this.vfs.getVFS(entityIds).begin(true) ) {
                List<VFSEntity> entities = new LinkedList<>();

                if ( !this.checkValidToken(null, req, resp) ) {
                    builder.fail(AuditStatus.UNAUTHENTICATED);
                    resp.sendError(403, "Intent not verified"); //$NON-NLS-1$
                    return;
                }

                Set<VFSContainerEntity> parents = new HashSet<>();

                for ( EntityKey entityId : entityIds ) {
                    VFSEntity entity = v.load(entityId);

                    builder.entity(entity);

                    if ( !this.accessControl.hasAccess(v, entity, GrantPermission.READ) ) {
                        builder.fail(AuditStatus.UNAUTHORIZED);
                        resp.sendError(403, "Forbidden, the link you followed is no longer valid"); //$NON-NLS-1$
                        return;
                    }

                    PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(v, entity, req);
                    if ( violation != null ) {
                        builder.policyViolation(violation);
                        resp.sendError(403, this.policyEvaluator.getViolationMessage(violation, getLocale(req)));
                        return;
                    }

                    entities.add(entity);
                    parents.add(v.getParent(entity));
                }

                if ( parents.size() == 1 ) {
                    builder.parentEntity(parents.iterator().next());
                }

                if ( !entities.isEmpty() && this.accessControl.isTokenAuth() ) {
                    try {
                        builder.grant(this.accessControl.getTokenAuthGrant(v, entities.get(0)));
                    }
                    catch ( GrantAuthenticationRequiredException e ) {
                        resp.sendError(403, "Forbidden, the grant is not authenticated"); //$NON-NLS-1$
                        return;
                    }
                }

                Stack<String> pathStack = new Stack<>();

                resp.setContentType(type.getMimeType()); // $NON-NLS-1$

                ReadableUserAgent ua = this.uaDetector.parse(req);
                setCSPHeader(req, resp, ua, this.uaDetector.getUA(req), makeDefaultCSPHeader(req));

                setDefaultSafetyHeaders(req, resp);
                resp.setHeader(X_DOWNLOAD_OPTIONS, "noopen"); //$NON-NLS-1$
                String fileName = "files"; //$NON-NLS-1$
                resp.setHeader(CONTENT_DISPOSITION, String.format("attachment;filename=\"%s.%s\"", fileName, type.getExtension())); //$NON-NLS-1$

                try ( OutputStream fos = resp.getOutputStream();
                      BufferedOutputStream bos = new BufferedOutputStream(fos);
                      ArchiveContext cc = createArchiveContext(type, bos) ) {
                    Set<String> names = new HashSet<>();
                    for ( VFSEntity e : entities ) {
                        builder.entity(e);
                        String localName = e.getLocalName();
                        if ( names.contains(localName) ) {
                            localName = ServiceUtil.findFreeName(localName, names, true, null);
                            if ( localName == null ) {
                                throw new NamingConflictException(localName, StringUtils.EMPTY);
                            }
                        }
                        names.add(localName);

                        if ( e instanceof VFSFileEntity ) {
                            VFSFileEntity ve = (VFSFileEntity) e;
                            cc.addEntry(makePath(pathStack, localName), v, ve, shouldCompress(ve));
                        }
                        else if ( e instanceof VFSContainerEntity ) {
                            pathStack.push(localName);
                            addArchiveChildren(v, (VFSContainerEntity) e, pathStack, cc, req, builder);
                            pathStack.pop();
                        }
                    }
                }
                catch ( IOException e ) {
                    builder.fail(AuditStatus.INTERNAL);
                    throw e;
                }
                catch ( Exception e ) {
                    builder.fail(AuditStatus.INTERNAL);
                    log.error("Failed to deliver zip file", e); //$NON-NLS-1$
                    resp.sendError(500);
                }
            }
            catch ( FileshareException e ) {
                builder.fail(e);
                log.warn("Failed to deliver zip file", e); //$NON-NLS-1$
                resp.sendError(500);
            }
        }
    }


    /**
     * @param e
     * @return
     */
    private static boolean shouldCompress ( VFSFileEntity e ) {
        if ( e.getContentType() != null && COMPRESSED_TYPES.contains(e.getContentType()) ) {
            return false;
        }
        return true;
    }


    /**
     * @param parent
     * @param string
     * @param req
     * @return
     * @throws FileshareException
     */
    private String makeArchiveVerifyLocationUrl ( VFSContext v, VFSContainerEntity parent, ArchiveType archiveType, HttpServletRequest req )
            throws FileshareException {
        TokenGrant tokenAuthGrant = null;
        try {
            tokenAuthGrant = this.accessControl.getTokenAuthGrant(v, parent);
        }
        catch ( GrantAuthenticationRequiredException ex ) {
            log.warn("Failed to get token auth grant as password authentication is required", ex); //$NON-NLS-1$
        }
        String type = getType(v, parent, tokenAuthGrant);

        return this.linkService.makeDirectoryViewLink(
            parent,
            String.format(
                "&grant=%s&token=%s%s", //$NON-NLS-1$
                tokenAuthGrant != null ? tokenAuthGrant.getId().toString() : StringUtils.EMPTY,
                req.getParameter(TOKEN_PARAM) != null ? req.getParameter(TOKEN_PARAM) : StringUtils.EMPTY,
                type),
            req.getContextPath());
    }


    /**
     * @param v
     * @param parent
     * @param tokenAuthGrant
     * @return
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    private String getType ( VFSContext v, VFSContainerEntity parent, TokenGrant tokenAuthGrant )
            throws AuthenticationException, UserNotFoundException {
        String type;
        if ( this.accessControl.isOwner(v, parent) ) {
            if ( this.accessControl.getCurrentUserCachable().equals(parent.getOwner()) ) {
                type = "&type=dir&rootType=user-root"; //$NON-NLS-1$
            }
            else {
                type = "&type=dir&rootType=group-root"; //$NON-NLS-1$
            }
        }
        else if ( tokenAuthGrant != null ) {
            if ( parent.equals(tokenAuthGrant.getEntity()) ) {
                type = "&type=share-root&rootType=share-root"; //$NON-NLS-1$
            }
            else {
                type = "&type=dir&rootType=share-root"; //$NON-NLS-1$
            }
        }
        else {
            type = StringUtils.EMPTY;
        }
        return type;
    }


    /**
     * @param parent
     * @param entries
     * @param inputs
     * @param pathStack
     * @param os
     * @param builder
     * @throws IOException
     * @throws FileshareException
     */
    private void addArchiveChildren ( VFSContext v, VFSContainerEntity parent, Stack<String> pathStack, ArchiveContext cc, ServletRequest req,
            MultiEntityFileshareAuditBuilder builder ) throws IOException, FileshareException {
        for ( VFSEntity child : v.getChildren(parent) ) {

            if ( this.policyEvaluator.isPolicyFulfilled(v, child, req) != null ) {
                continue;
            }

            builder.entity(child);

            if ( child instanceof VFSFileEntity ) {
                VFSFileEntity ce = (VFSFileEntity) child;
                cc.addEntry(makePath(pathStack, child.getLocalName()), v, ce, shouldCompress(ce));
            }
            else if ( child instanceof VFSContainerEntity ) {
                pathStack.push(child.getLocalName());
                try {
                    addArchiveChildren(v, (VFSContainerEntity) child, pathStack, cc, req, builder);
                }
                finally {
                    pathStack.pop();
                }
            }
        }
    }


    /**
     * @param parent
     * @return
     */
    private static String getArchiveFileName ( VFSContainerEntity parent ) {
        if ( !StringUtils.isBlank(parent.getLocalName()) ) {
            return parent.getLocalName();
        }

        if ( parent.getOwner() instanceof User ) {
            return ( (User) parent.getOwner() ).getPrincipal().getUserName();
        }
        else if ( parent.getOwner() instanceof Group ) {
            return ( (Group) parent.getOwner() ).getName();
        }

        return "files"; //$NON-NLS-1$
    }


    /**
     * @param pathStack
     * @param localName
     */
    private static String makePath ( Stack<String> pathStack, String localName ) {
        if ( pathStack.isEmpty() ) {
            return localName;
        }
        return StringUtils.join(pathStack, '/') + '/' + localName;
    }

}
