/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.Map;

import eu.agno3.fileshare.exceptions.FileshareException;


/**
 * @author mbechler
 *
 */
public interface PreferenceService {

    /**
     * 
     * Access control:
     * - only gets the current users preferences
     * 
     * @return the current users stored preferences
     * @throws FileshareException
     */
    public Map<String, String> loadPreferences () throws FileshareException;


    /**
     * 
     * Access control:
     * - only affects the current users preferences
     * 
     * @param prefs
     * @return the new preferences
     * @throws FileshareException
     */
    public Map<String, String> savePreferences ( Map<String, String> prefs ) throws FileshareException;
}
