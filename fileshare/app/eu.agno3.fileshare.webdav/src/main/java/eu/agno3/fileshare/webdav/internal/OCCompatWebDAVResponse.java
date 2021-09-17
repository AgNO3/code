/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.WebdavResponse;

import eu.agno3.runtime.webdav.server.impl.DefaultWebdavResponseImpl;


/**
 * @author mbechler
 *
 */
public class OCCompatWebDAVResponse extends DefaultWebdavResponseImpl implements WebdavResponse {

    /**
     * @param httpResponse
     * @param noCache
     */
    public OCCompatWebDAVResponse ( HttpServletResponse httpResponse, boolean noCache ) {
        super(httpResponse, noCache);
    }


    /**
     * @param httpResponse
     */
    public OCCompatWebDAVResponse ( HttpServletResponse httpResponse ) {
        super(httpResponse);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.impl.DefaultWebdavResponseImpl#getXmlResponseContentType()
     */
    @Override
    protected String getXmlResponseContentType () {
        return "application/xml; charset=utf-8"; //$NON-NLS-1$
    }

}
