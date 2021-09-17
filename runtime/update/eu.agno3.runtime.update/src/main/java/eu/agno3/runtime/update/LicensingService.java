/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2016 by mbechler
 */
package eu.agno3.runtime.update;


import java.nio.file.Path;
import java.util.Set;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface LicensingService {

    /**
     * @return new license
     */
    License refreshLicense ();


    /**
     * @return the time the license expires/expired
     */
    DateTime getExpirationDate ();


    /**
     * 
     * @return whether this is a demo license
     */
    boolean isDemoLicense ();


    /**
     * 
     * @param serviceType
     * @return whether the license is valid
     */
    boolean isLicenseValid ( String serviceType );


    /**
     * 
     * @param serviceType
     * @throws LicensingException
     */
    void checkLicenseValid ( String serviceType ) throws LicensingException;


    /**
     * 
     * @param serviceType
     * @param key
     * @param val
     * @param def
     * @return whether the value is within the limits
     */
    boolean withinLicenseLimit ( String serviceType, String key, long val, Long def );


    /**
     * @param serviceType
     * @param key
     * @param def
     * @return the license limit if the license is valid and specifies it, def otherwise
     */
    Long getLicenseLimit ( String serviceType, String key, Long def );


    /**
     * 
     * @param serviceType
     * @param key
     * @param val
     * @param def
     * @throws LicensingException
     */
    void checkWithinLicenseLimit ( String serviceType, String key, long val, Long def ) throws LicensingException;


    /**
     * @return whether the license is going to expire soon
     */
    boolean warnExpiration ();


    /**
     * @return the license data
     */
    License getLicense ();


    /**
     *
     * @return to path to the license file
     */
    Path getLicensePath ();


    /**
     * @param data
     * @param serviceTypes
     * @throws LicensingException
     */
    void checkValid ( byte[] data, Set<String> serviceTypes ) throws LicensingException;

}
