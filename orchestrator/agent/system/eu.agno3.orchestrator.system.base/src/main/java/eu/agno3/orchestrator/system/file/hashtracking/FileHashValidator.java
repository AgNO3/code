/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 5, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.file.hashtracking;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface FileHashValidator {

    /**
     * 
     * @return paths for which hash validation failed
     * @throws IOException
     */
    public Map<Path, ValidationResult> getMismatchingEntries () throws IOException;
}
