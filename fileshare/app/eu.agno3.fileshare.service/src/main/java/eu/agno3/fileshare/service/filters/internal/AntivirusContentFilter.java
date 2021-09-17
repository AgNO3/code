/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.filters.internal;


import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.ContentException;
import eu.agno3.fileshare.exceptions.ContentVirusException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.api.internal.ContentFilter;
import eu.agno3.runtime.net.icap.ICAPConnectionPool;
import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPScanRequest;
import eu.agno3.runtime.net.icap.ICAPScannerException;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = ContentFilter.class, configurationPid = "antivirus", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class AntivirusContentFilter implements ContentFilter {

    private static final Logger log = Logger.getLogger(AntivirusContentFilter.class);

    private ICAPConnectionPool icapPool;


    @Reference
    protected synchronized void setICAPConnectionPool ( ICAPConnectionPool icp ) {
        this.icapPool = icp;
    }


    protected synchronized void unsetICAPConnectionPool ( ICAPConnectionPool icp ) {
        if ( this.icapPool == icp ) {
            this.icapPool = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.ContentFilter#getId()
     */
    @Override
    public String getId () {
        return "antivirus"; //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.ContentFilter#filterContent(eu.agno3.fileshare.model.VFSFileEntity,
     *      java.nio.channels.SeekableByteChannel)
     */
    @Override
    public void filterContent ( VFSFileEntity f, SeekableByteChannel data ) throws FileshareException {

        UserPrincipal up = SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);
        HttpServletRequest httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        if ( log.isDebugEnabled() ) {
            log.debug("AV scanning " + f); //$NON-NLS-1$
        }

        doScan(f, data, up, httpRequest, true);

    }


    /**
     * @param f
     * @param data
     * @param up
     * @param httpRequest
     * @throws ContentException
     * @throws ContentVirusException
     */
    private void doScan ( VFSFileEntity f, SeekableByteChannel data, UserPrincipal up, HttpServletRequest httpRequest, boolean retry )
            throws ContentException, ContentVirusException {
        try ( InputStream inputStream = Channels.newInputStream(data) ) {
            ICAPScanRequest req = new ICAPScanRequest(inputStream, f.getFileSize());
            req.setContentType(f.getContentType());
            req.setFileName(f.getLocalName());

            if ( up != null ) {
                req.setClientUser(up.toString());
            }

            if ( httpRequest != null ) {
                req.setClientIp(httpRequest.getRemoteAddr());
            }

            this.icapPool.scan(req);
            return;
        }
        catch (
            IOException |
            ICAPException e ) {
            if ( retry ) {
                log.debug("Scanning failed, retrying", e); //$NON-NLS-1$
                doScan(f, data, up, httpRequest, false);
                return;
            }
            throw new ContentException("Checking the file against the antivirus failed", e); //$NON-NLS-1$
        }
        catch ( ICAPScannerException e ) {
            log.debug("Found virus", e); //$NON-NLS-1$
            throw new ContentVirusException(e.getSignature(), "File rejected by antivirus", e); //$NON-NLS-1$
        }
    }
}
