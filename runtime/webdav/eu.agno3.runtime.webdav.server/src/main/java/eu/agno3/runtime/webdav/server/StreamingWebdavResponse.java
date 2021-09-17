/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import org.apache.jackrabbit.webdav.WebdavResponse;


/**
 * @author mbechler
 *
 */
public interface StreamingWebdavResponse extends WebdavResponse {

    /**
     * @return a streaming output context
     */
    StreamingContext getStreamingContext ();

}
