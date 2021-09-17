/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2016 by mbechler
 */
package eu.agno3.runtime.update;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface License {

    /**
     * 
     * @return the license id
     */
    UUID getLicenseId ();


    /**
     * 
     * @return the issue date
     */
    DateTime getIssueDate ();


    /**
     * @return license description
     */
    String getDescription ();


    /**
     * 
     * @return the license subject
     */
    String getLicensedTo ();


    /**
     * @return the expiration date
     */
    DateTime getExpirationDate ();


    /**
     * 
     * @return the license limits
     */
    Map<String, Long> getLicenseLimits ();


    /**
     * @return the service types this license applies to
     */
    Set<String> getServiceTypes ();


    /**
     * @return binary data of the license
     */
    byte[] getRawData ();

}
