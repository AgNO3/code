/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit;


import java.util.Set;

import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface SystemUpdateManager extends SystemService {

    /**
     * @param pl
     * @return the availabvle updates
     * @throws PackageKitException
     * 
     */
    Set<PackageUpdate> checkForUpdates ( PackageKitProgressListener pl ) throws PackageKitException;


    /**
     * @param pl
     * @return installed software
     * @throws PackageKitException
     */
    Set<PackageId> getInstalledSoftware ( PackageKitProgressListener pl ) throws PackageKitException;


    /**
     * @param pkgs
     * @param pl
     * @throws PackageKitException
     */
    void prepareUpdates ( Set<PackageId> pkgs, PackageKitProgressListener pl ) throws PackageKitException;


    /**
     * @param pkgs
     * @param pl
     * @throws PackageKitException
     */
    void installUpdates ( Set<PackageId> pkgs, PackageKitProgressListener pl ) throws PackageKitException;


    /**
     * @param pl
     * @throws PackageKitException
     */
    void repair ( PackageKitProgressListener pl ) throws PackageKitException;


    /**
     * @param id
     * @param url
     * @param pl
     * @throws PackageKitException
     */
    void setRepositoryLocation ( String id, String url ) throws PackageKitException;


    /**
     * 
     * @param newRepo
     * @param pl
     * @return the old repository
     * @throws PackageKitException
     */
    String switchRepository ( String newRepo ) throws PackageKitException;

}
