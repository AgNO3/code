/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 5, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.file.hashtracking.internal;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.file.hashtracking.FileHashTracker;
import eu.agno3.orchestrator.system.file.hashtracking.FileHashValidator;
import eu.agno3.orchestrator.system.file.hashtracking.ValidationResult;
import eu.agno3.orchestrator.system.file.util.FileHashUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = FileHashValidator.class )
public class FileHashValidatorImpl implements FileHashValidator {

    private static final Logger log = Logger.getLogger(FileHashValidatorImpl.class);
    private FileHashTracker hashTracker;


    @Reference
    protected synchronized void setHashTracker ( FileHashTracker fht ) {
        this.hashTracker = fht;
    }


    protected synchronized void unsetHashTracker ( FileHashTracker fht ) {
        if ( this.hashTracker == fht ) {
            this.hashTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.orchestrator.system.file.hashtracking.FileHashValidator#getMismatchingEntries()
     */
    @Override
    public Map<Path, ValidationResult> getMismatchingEntries () throws IOException {
        Map<Path, ValidationResult> res = new HashMap<>();
        for ( Entry<Path, byte[]> entry : this.hashTracker.listHashes().entrySet() ) {
            Path f = entry.getKey();
            if ( !Files.exists(f) ) {
                res.put(f, ValidationResult.MISSING);
                continue;
            }
            try {
                byte[] sha512 = FileHashUtil.sha512(f);
                if ( !MessageDigest.isEqual(sha512, entry.getValue()) ) {
                    res.put(f, ValidationResult.HASH_MISMATCH);
                }
            }
            catch (
                IOException |
                NoSuchAlgorithmException e ) {
                log.warn("Failed to calculate file hash", e); //$NON-NLS-1$
                res.put(f, ValidationResult.ERROR);
            }
        }
        return res;
    }

}
