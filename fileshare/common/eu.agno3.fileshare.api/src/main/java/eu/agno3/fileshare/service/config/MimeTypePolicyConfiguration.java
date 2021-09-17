/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.Set;
import java.util.SortedSet;

import eu.agno3.fileshare.exceptions.DisallowedMimeTypeException;


/**
 * @author mbechler
 *
 */
public interface MimeTypePolicyConfiguration {

    /**
     * 
     * @return whether to allow users to change mime types
     */
    public boolean isAllowMimeTypeChanges ();


    /**
     * 
     * @return the allowed mime types
     */
    public SortedSet<String> getAllowedMimeTypes ();


    /**
     * 
     * @return the mime types that are specifically disallowed
     */
    public Set<String> getBlacklistedMimeTypes ();


    /**
     * @return the mime type to use when no filetype could be detected
     */
    public String getFallbackMimeType ();


    /**
     * @return whether to include user supplied data in file type detection
     */
    public boolean isUseUserSuppliedTypes ();


    /**
     * @param type
     * @param ignoreUnknown
     * @throws DisallowedMimeTypeException
     */
    void checkMimeType ( String type, boolean ignoreUnknown ) throws DisallowedMimeTypeException;
}
