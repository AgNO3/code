/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
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
public interface ICAPConnection extends AutoCloseable {

    /**
     * @throws IOException
     */
    @Override
    void close () throws IOException;


    /**
     * @return the discovered ICAP options
     * @throws ICAPException
     * @throws IOException
     */
    ICAPOptions getOptions () throws IOException, ICAPException;


    /**
     * @param is
     * @param icapHeaders
     * @param reqHeader
     * @param resHeader
     * @param preview
     * @return the response
     * @throws IOException
     * @throws ICAPException
     */
    ICAPResponse respmod ( InputStream is, Map<String, List<String>> icapHeaders, byte[] reqHeader, byte[] resHeader, boolean preview )
            throws IOException, ICAPException;


    /**
     * @param reqBody
     * @param icapHeaders
     * @param reqHeader
     * @param preview
     * @return the response
     * @throws ICAPException
     * @throws IOException
     */
    ICAPResponse reqmod ( InputStream reqBody, Map<String, List<String>> icapHeaders, byte[] reqHeader, boolean preview )
            throws IOException, ICAPException;


    /**
     * @return whether this connection is okay
     */
    boolean check ();


    /**
     * @throws ICAPException
     * @throws IOException
     * 
     */
    void ensureConnected () throws IOException, ICAPException;


    /**
     * @param req
     * @throws ICAPException
     * @throws ICAPScannerException
     */
    void scan ( ICAPScanRequest req ) throws ICAPScannerException, ICAPException;

}
