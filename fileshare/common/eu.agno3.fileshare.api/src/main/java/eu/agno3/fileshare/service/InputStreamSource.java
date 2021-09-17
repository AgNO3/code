/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.io.IOException;
import java.io.InputStream;


/**
 * @author mbechler
 *
 */
public interface InputStreamSource {

    /**
     * 
     * @return an input stream from the beginning of data
     * @throws IOException
     */
    InputStream getInputStream () throws IOException;
}
