/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.io.IOException;


/**
 * @author mbechler
 *
 */
public interface StreamingReport extends AutoCloseable {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws IOException;


    /**
     * 
     */
    void setFailed ();
}
