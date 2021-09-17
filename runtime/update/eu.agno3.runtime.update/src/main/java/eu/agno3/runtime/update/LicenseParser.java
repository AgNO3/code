/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.runtime.update;


import java.io.InputStream;


/**
 * @author mbechler
 *
 */
public interface LicenseParser {

    /**
     * @param licDataBytes
     * @return parsed and validated license
     * @throws LicensingException
     */
    License parseLicense ( byte[] licDataBytes ) throws LicensingException;


    /**
     * @param licData
     * @return parsed and validated license
     * @throws LicensingException
     */
    License parseLicense ( String licData ) throws LicensingException;


    /**
     * @param inputStream
     * @return pared and validated license
     * @throws LicensingException
     */
    License parseLicense ( InputStream inputStream ) throws LicensingException;

}
