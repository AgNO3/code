/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.io.IOException;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;


/**
 * @author mbechler
 *
 */
public interface StreamingReportResource {

    /**
     * @param info
     * @return whether this resource can handle the given report in a streaming manner
     */
    boolean canStream ( ReportInfo info );


    /**
     * @param info
     * @param response
     * @throws DavException
     * @throws IOException
     */
    void streamReport ( ReportInfo info, StreamingWebdavResponse response ) throws DavException, IOException;

}
