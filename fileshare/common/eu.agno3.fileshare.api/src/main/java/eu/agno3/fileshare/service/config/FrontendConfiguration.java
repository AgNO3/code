/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.net.URI;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public interface FrontendConfiguration {

    /**
     * 
     * @return whether the frontend URI is reliable
     */
    boolean isWebFrontendURIReliable ();


    /**
     * @return the canonical URI for the web frontend
     */
    URI getWebFrontendURI ();


    /**
     * @return the duration for which a download link should be valid
     */
    Duration getIntentTimeout ();


    /**
     * @return whether to allow users to override a files last content modification time
     */
    boolean isAllowUserModificationTimes ();


    /**
     * @return the duration for which incomplete uploads (without activity) are stored for non user accounts
     */
    Duration getTokenIncompleteExpireDuration ();


    /**
     * @return the duration for which incomplete uploads (without activity) are stored for user accounts
     */
    Duration getUserIncompleteExpireDuration ();


    /**
     * @return the maximum size (bytes) of incomplete upload storage for non user accounts
     */
    Long getPerSessionIncompleteSizeLimit ();


    /**
     * @return the maximum size (bytes) of incomplete upload storage for user accounts
     */
    Long getPerUserIncompleteSizeLimit ();


    /**
     * @return the default timezone to use
     */
    DateTimeZone getDefaultTimeZone ();


    /**
     * @return whether to enable compression of downloaded files
     */
    boolean isEnableCompression ();


    /**
     * 
     * @return whether audit logging is available
     */
    boolean isAuditEnabled ();

}
