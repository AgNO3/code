/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.hashtracking;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface FileHashTracker extends SystemService {

    /**
     * @param p
     * @param hash
     * @throws IOException
     */
    void updateHash ( Path p, byte[] hash ) throws IOException;


    /**
     * @param p
     * @param hash
     * @return whether the hash matches with the saved one (if none is saved return true)
     * @throws IOException
     */
    boolean checkHash ( Path p, byte[] hash ) throws IOException;


    /**
     * @param p
     * @throws IOException
     */
    void removeHash ( Path p ) throws IOException;


    /**
     * @return the known file hashes
     * @throws IOException
     */
    Map<Path, byte[]> listHashes () throws IOException;
}
