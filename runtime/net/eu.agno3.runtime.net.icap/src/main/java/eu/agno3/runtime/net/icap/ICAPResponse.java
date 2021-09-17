/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface ICAPResponse extends AutoCloseable {

    /**
     * @return the statusCode
     */
    int getStatusCode ();


    /**
     * @return the respHeaders
     */
    Map<String, List<String>> getResponseHeaders ();


    /**
     * @return the headerParts
     */
    Map<String, String> getHeaderParts ();


    /**
     * @return the inputStream
     */
    InputStream getInputStream ();


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws IOException, ICAPException;

}