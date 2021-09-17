/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.service.FileDownloadFilter;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.DeliveryService;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.runtime.http.ua.UADetector;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentType;


/**
 * @author mbechler
 *
 */
@Component ( service = FileDownloadFilter.class )
public class FileDownloadFilterImpl extends PathMatchingFilter implements FileDownloadFilter {

    /**
     * 
     */
    private static final String ARCHIVE_PREFIX = "archive-"; //$NON-NLS-1$
    private static final String DOWNLOAD = "download"; //$NON-NLS-1$
    private static final String DOWNLOAD_ALL = "download-all"; //$NON-NLS-1$
    private static final String VIEW = "view"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileDownloadFilterImpl.class);

    private static final String FILES_PREFIX = "/files"; //$NON-NLS-1$

    private static final Set<String> ACCESS_TYPES = new HashSet<>();

    static {
        ACCESS_TYPES.add(VIEW);
        ACCESS_TYPES.add(DOWNLOAD);
        ACCESS_TYPES.add(DOWNLOAD_ALL);
    }

    private DefaultServiceContext ctx;

    private DeliveryService delivery;
    private VFSServiceInternal vfs;
    private UADetector uaDetector;


    /**
     * 
     */
    public FileDownloadFilterImpl () {
        setName("FileDownloadFilter"); //$NON-NLS-1$
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
    protected synchronized void setDeliveryService ( DeliveryService ds ) {
        this.delivery = ds;
    }


    protected synchronized void unsetDeliveryService ( DeliveryService ds ) {
        if ( this.delivery == ds ) {
            this.delivery = null;
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
    protected synchronized void setUADetector ( UADetector uad ) {
        this.uaDetector = uad;
    }


    protected synchronized void unsetUADetector ( UADetector uad ) {
        if ( this.uaDetector == uad ) {
            this.uaDetector = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.servlet.AdviceFilter#doFilterInternal(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws ServletException, IOException {

        req.setAttribute("bypass-error-handler", true); //$NON-NLS-1$
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        String reqPath = getRequestPath(httpReq);

        int prefixLen = FILES_PREFIX.length() + 1;
        if ( reqPath.length() < prefixLen ) {
            super.doFilterInternal(req, resp, chain);
            return;
        }

        String realPath = reqPath.substring(prefixLen);
        String[] segments = StringUtils.split(realPath, "/", 3); //$NON-NLS-1$

        if ( segments == null || ( !DOWNLOAD_ALL.equals(segments[ 0 ]) && segments.length < 3 ) ) {
            super.doFilterInternal(req, resp, chain);
            return;
        }

        String accessType = segments[ 0 ];

        boolean isArchive = accessType.startsWith(ARCHIVE_PREFIX);
        if ( !ACCESS_TYPES.contains(accessType) && !isArchive ) { // $NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Invalid access type " + accessType); //$NON-NLS-1$
            }
            super.doFilterInternal(req, resp, chain);
            return;
        }

        ReadableUserAgent ua = this.uaDetector.parse(httpReq);
        if ( log.isDebugEnabled() ) {
            log.debug("User agent is " + ua); //$NON-NLS-1$
        }
        if ( ua != null && ua.getType() == UserAgentType.ROBOT ) {
            httpResp.sendError(403, "Bots disallowed"); //$NON-NLS-1$
            return;
        }

        if ( DOWNLOAD_ALL.equals(segments[ 0 ]) ) {
            doDeliverMulti(httpReq, httpResp);
            return;
        }

        EntityKey id;
        try {
            id = this.vfs.parseEntityKey(segments[ 1 ]);
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Failed to parse id", e); //$NON-NLS-1$
            super.doFilterInternal(req, resp, chain);
            return;
        }

        String fileName = segments[ 2 ];
        int querySep = fileName.indexOf('?');
        if ( querySep >= 0 ) {
            fileName = fileName.substring(0, querySep);
        }

        if ( isArchive ) {
            doDeliverArchive(req, resp, chain, httpReq, httpResp, accessType, id, fileName);
        }
        else {
            doDeliverFile(httpReq, httpResp, accessType, id, fileName);
        }
    }


    /**
     * @param httpReq
     * @return
     */
    private static String getRequestPath ( HttpServletRequest httpReq ) {
        String contextPath = httpReq.getContextPath();
        String requestUri = httpReq.getRequestURI();
        if ( StringUtils.startsWithIgnoreCase(requestUri, contextPath) ) {
            String path = requestUri.substring(contextPath.length());
            return ( !StringUtils.isEmpty(path) ? path : "/" ); //$NON-NLS-1$
        }
        return requestUri;
    }


    /**
     * @param req
     * @param resp
     * @param chain
     * @param httpReq
     * @param httpResp
     * @param accessType
     * @param id
     * @param fileName
     * @throws ServletException
     * @throws IOException
     */
    void doDeliverArchive ( ServletRequest req, ServletResponse resp, FilterChain chain, HttpServletRequest httpReq, HttpServletResponse httpResp,
            String accessType, EntityKey id, String fileName ) throws ServletException, IOException {
        String at = accessType.substring(ARCHIVE_PREFIX.length());
        ArchiveType type;
        try {
            type = ArchiveType.valueOf(at);
        }
        catch ( IllegalArgumentException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Invalid archive type " + at, e); //$NON-NLS-1$
            }
            super.doFilterInternal(req, resp, chain);
            return;
        }
        this.delivery.doDeliverDirectory(httpReq, httpResp, id, fileName, type);
    }


    /**
     * @param httpReq
     * @param httpResp
     * @param archiveType
     * @throws IOException
     */
    private void doDeliverMulti ( HttpServletRequest httpReq, HttpServletResponse httpResp ) throws IOException {
        Set<EntityKey> entityIds = new HashSet<>();
        String[] entities = httpReq.getParameterValues("entity"); //$NON-NLS-1$
        String[] archiveArg = httpReq.getParameterValues("type"); //$NON-NLS-1$
        if ( entities == null ) {
            httpResp.sendError(404);
            return;
        }

        for ( String entity : entities ) {
            entityIds.add(this.vfs.parseEntityKey(entity));
        }

        log.debug("Multi file delivery " + entityIds); //$NON-NLS-1$

        ArchiveType archiveType = ArchiveType.ZIP;

        if ( archiveArg != null && archiveArg.length == 1 ) {
            try {
                archiveType = ArchiveType.valueOf(archiveArg[ 0 ]);
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Invalid archive type, falling back to ZIP", e); //$NON-NLS-1$
            }
        }
        this.delivery.doDeliverMultiple(httpReq, httpResp, entityIds, archiveType);
    }


    /**
     * @param httpReq
     * @param httpResp
     * @param accessType
     * @param id
     * @param fileName
     * @throws IOException
     */
    private void doDeliverFile ( HttpServletRequest httpReq, HttpServletResponse httpResp, String accessType, EntityKey id, String fileName )
            throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Called %s on %s/%s", accessType, id, fileName)); //$NON-NLS-1$
        }

        switch ( accessType ) {
        case VIEW:
            this.delivery.doDeliverFile(httpReq, httpResp, id, fileName, "inline"); //$NON-NLS-1$
            break;
        case DOWNLOAD:
            this.delivery.doDeliverFile(httpReq, httpResp, id, fileName, "attachment"); //$NON-NLS-1$
            break;
        }
    }

}
