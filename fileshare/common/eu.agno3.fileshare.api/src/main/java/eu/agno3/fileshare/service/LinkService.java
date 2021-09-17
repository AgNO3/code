/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.Set;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;


/**
 * @author mbechler
 *
 */
public interface LinkService {

    /**
     * @param file
     * @param query
     * @param overrideBase
     * @param anon
     * @return the download link
     * @throws FileshareException
     */
    String makeDownloadLink ( VFSFileEntity file, String query, String overrideBase, boolean anon ) throws FileshareException;


    /**
     * @param entities
     * @param archiveType
     * @param query
     * @param overrideBase
     * @return the download all link
     * @throws FileshareException
     */
    String makeDownloadAllLink ( Set<VFSEntity> entities, ArchiveType archiveType, String query, String overrideBase ) throws FileshareException;


    /**
     * @param file
     * @param archiveType
     * @param query
     * @param overrideBase
     * @param anon
     * @return the directory archive downloadl link
     * @throws FileshareException
     */
    String makeDirectoryArchiveLink ( VFSContainerEntity file, ArchiveType archiveType, String query, String overrideBase, boolean anon )
            throws FileshareException;


    /**
     * @param file
     * @param query
     * @param overrideBase
     * @param makeTokenArg
     * @return the backend view link (do not use this directly)
     * @throws FileshareException
     */
    String makeBackendViewLink ( VFSFileEntity file, String query, String overrideBase ) throws FileshareException;


    /**
     * @param file
     * @param query
     * @param overrideBase
     * @return the frontend view link
     * @throws FileshareException
     */
    String makeFrontendViewLink ( VFSFileEntity file, String query, String overrideBase ) throws FileshareException;


    /**
     * @param ce
     * @param tokenQuery
     * @param overrideBase
     * @return directory view link
     * @throws FileshareException
     */
    String makeDirectoryViewLink ( VFSContainerEntity ce, String tokenQuery, String overrideBase ) throws FileshareException;


    /**
     * @param path
     * @param overrideBase
     * @return a link relative to the context root
     */
    String makeGenericLink ( String path, String overrideBase );

}
