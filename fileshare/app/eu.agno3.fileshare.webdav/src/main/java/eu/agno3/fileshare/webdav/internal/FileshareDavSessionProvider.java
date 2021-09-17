/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.Serializable;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;

import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.webdav.server.ExtendedDavSession;
import eu.agno3.runtime.webdav.server.impl.DefaultDavSessionProvider;


/**
 * @author mbechler
 *
 */
public class FileshareDavSessionProvider extends DefaultDavSessionProvider implements DavSessionProvider {

    /**
     * @param uaDetector
     * 
     */
    public FileshareDavSessionProvider ( UADetector uaDetector ) {
        super(uaDetector, true);
    }


    /**
     * @param uaDetector
     * @param rejectNonDAVClients
     */
    public FileshareDavSessionProvider ( UADetector uaDetector, boolean rejectNonDAVClients ) {
        super(uaDetector, rejectNonDAVClients);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.impl.DefaultDavSessionProvider#attachSession(org.apache.jackrabbit.webdav.WebdavRequest)
     */
    @Override
    public boolean attachSession ( WebdavRequest req ) throws DavException {
        boolean res = super.attachSession(req);

        if ( res && ( req.getDavSession() instanceof ExtendedDavSession ) ) {
            ExtendedDavSession sess = (ExtendedDavSession) req.getDavSession();
            sess.setAttribute(Constants.LAYOUT, (Serializable) req.getAttribute(Constants.LAYOUT));
        }

        return res;
    }
}
