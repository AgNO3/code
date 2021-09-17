/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup;


import java.nio.file.Path;
import java.util.SortedSet;
import java.util.zip.ZipFile;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;


/**
 * @author mbechler
 * @param <TUnit>
 *
 */
public interface BackupUnitGenerator <TUnit extends BackupUnit> {

    /**
     * @return the handled unit type
     */
    Class<TUnit> getUnitType ();


    /**
     * @param service
     * @param unit
     * @param tempDir
     * @throws BackupException
     */
    void backup ( ServiceStructuralObject service, TUnit unit, Path tempDir ) throws BackupException;


    /**
     * @param service
     * @param unit
     * @param data
     * @param prefix
     * @param unitFiles
     * @throws BackupException
     */
    void restore ( ServiceStructuralObject service, TUnit unit, ZipFile data, String prefix, SortedSet<String> unitFiles ) throws BackupException;

}
