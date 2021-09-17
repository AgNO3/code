/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.io.IOException;

import javax.xml.stream.XMLStreamWriter;


/**
 * @author mbechler
 *
 */
public interface StreamingContext extends AutoCloseable {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws IOException;


    /**
     * @throws IOException
     */
    void startMultiStatus () throws IOException;


    /**
     * 
     * @param xs
     * @throws IOException
     */
    void write ( XMLStreamable xs ) throws IOException;


    /**
     * @return the stream writer
     * @throws IOException
     */
    XMLStreamWriter getStreamWriter () throws IOException;
}
