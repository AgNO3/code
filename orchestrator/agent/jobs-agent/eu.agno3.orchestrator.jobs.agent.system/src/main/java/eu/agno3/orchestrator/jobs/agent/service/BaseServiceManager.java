/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service;


import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupUnit;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;


/**
 * @author mbechler
 *
 */
public interface BaseServiceManager {

    /**
     * 
     * @return the handeled service type
     */
    String getServiceType ();


    /**
     * 
     * @param instanceId
     * @throws ServiceManagementException
     */
    void enable ( UUID instanceId ) throws ServiceManagementException;


    /**
     * @param instanceId
     * @throws ServiceManagementException
     * 
     */
    void disable ( UUID instanceId ) throws ServiceManagementException;


    /**
     * 
     * @param instanceId
     * @throws ServiceManagementException
     */
    void restart ( UUID instanceId ) throws ServiceManagementException;


    /**
     * @param instanceId
     * @throws ServiceManagementException
     */
    void stop ( UUID instanceId ) throws ServiceManagementException;


    /**
     * @param instanceId
     * @throws ServiceManagementException
     */
    void start ( UUID instanceId ) throws ServiceManagementException;


    /**
     * 
     * @param instanceId
     * @return the runtime status of the target service
     * @throws ServiceManagementException
     */
    public ServiceRuntimeStatus getRuntimeStatus ( UUID instanceId ) throws ServiceManagementException;


    /**
     * @throws ServiceManagementException
     * 
     */
    public void runMonitoring () throws ServiceManagementException;


    /**
     * @return the user the service runs as
     * @throws ServiceManagementException
     */
    UserPrincipal getServicePrincipal () throws ServiceManagementException;


    /**
     * @return the group the service runs as
     * @throws ServiceManagementException
     */
    GroupPrincipal getGroupPrincipal () throws ServiceManagementException;


    /**
     * @param string
     * @throws ServiceManagementException
     */
    void forceReloadAll ( String string ) throws ServiceManagementException;


    /**
     * @param cfg
     * @return the backup units for this service
     * @throws BackupException
     */
    List<BackupUnit> getBackupUnits ( @NonNull ConfigurationInstance cfg ) throws BackupException;


    /**
     * @param storageAlias
     * @return an override path, or null
     */
    Path getOverrideStoragePath ( String storageAlias );

}