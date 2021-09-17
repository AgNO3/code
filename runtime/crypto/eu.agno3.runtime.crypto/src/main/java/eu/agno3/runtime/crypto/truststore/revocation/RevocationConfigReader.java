/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation;


import java.io.File;
import java.io.IOException;
import java.util.Properties;


/**
 * @author mbechler
 *
 */
public interface RevocationConfigReader {

    /**
     * @param props
     * @return parsed configuration
     */
    public RevocationConfig fromProperties ( Properties props );


    /**
     * 
     * @param config
     * @return Properties from configuration
     */
    public Properties toProperties ( RevocationConfig config );


    /**
     * @param revocationConfig
     * @param config
     * @throws IOException
     */
    public void toFile ( File revocationConfig, RevocationConfig config ) throws IOException;


    /**
     * @param revocationConfig
     * @return the revocation config
     * @throws IOException
     */
    public RevocationConfig fromFile ( File revocationConfig ) throws IOException;

}
