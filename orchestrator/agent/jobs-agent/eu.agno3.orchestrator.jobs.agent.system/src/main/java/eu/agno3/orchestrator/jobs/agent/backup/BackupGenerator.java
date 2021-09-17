/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup;


import java.io.OutputStream;
import java.nio.file.Path;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface BackupGenerator extends SystemService {

    /**
     * 
     * @param service
     * @param os
     * @throws BackupException
     */
    public void backup ( ServiceStructuralObject service, OutputStream os ) throws BackupException;


    /**
     * 
     * @param service
     * @param source
     * @throws BackupException
     */
    public void restore ( ServiceStructuralObject service, Path source ) throws BackupException;

}
