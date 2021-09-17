/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class DictionaryLoader {

    private static final Logger log = Logger.getLogger(DictionaryLoader.class);

    private static final String DEFAULT_DICT_PATH = "/usr/share/agno3-dictionary/passwords.dict"; //$NON-NLS-1$


    /**
     * 
     * @return a matcher of all available dictionaries
     */
    public DictionaryMatcher getMatcher () {

        Path p = Paths.get(DEFAULT_DICT_PATH);

        if ( !Files.exists(p) || !Files.isReadable(p) ) {
            log.warn("No dictionary found at " + p); //$NON-NLS-1$
            return new DictionaryMatcher(Collections.<Dictionary> emptyList());
        }

        try {
            return new DictionaryMatcher(p);
        }
        catch ( IOException e ) {
            log.error("Failed to load dictionary " + p, e); //$NON-NLS-1$
            return new DictionaryMatcher(Collections.<Dictionary> emptyList());
        }
    }

}
