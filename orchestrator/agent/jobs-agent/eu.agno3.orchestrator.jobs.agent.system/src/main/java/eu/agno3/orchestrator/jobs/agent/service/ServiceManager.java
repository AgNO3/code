/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service;


import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface ServiceManager extends SystemService {

    /**
     * 
     * @param service
     * @param managerType
     * @return the service manager for the service
     * @throws ServiceManagementException
     */
    <T extends BaseServiceManager> T getServiceManager ( StructuralObjectReference service, Class<T> managerType ) throws ServiceManagementException;


    /**
     * 
     * @param serviceType
     * @param managerType
     * @return the service manager for the service type
     * @throws ServiceManagementException
     */
    <T extends BaseServiceManager> T getSingletonServiceManager ( String serviceType, Class<T> managerType ) throws ServiceManagementException;


    /**
     * 
     * @param service
     * @throws ServiceManagementException
     */
    void restartService ( StructuralObjectReference service ) throws ServiceManagementException;


    /**
     * 
     * @param service
     * @throws ServiceManagementException
     */
    void enableService ( StructuralObjectReference service ) throws ServiceManagementException;


    /**
     * 
     * @param service
     * @throws ServiceManagementException
     */
    void disableService ( StructuralObjectReference service ) throws ServiceManagementException;


    /**
     * @param string
     *            refreshes the configuration of all active services
     */
    void forceReloadAll ( String string );


    /**
     * @param targetService
     * @throws ServiceManagementException
     */
    void stopService ( StructuralObjectReference targetService ) throws ServiceManagementException;


    /**
     * @param service
     * @throws ServiceManagementException
     */
    void startService ( StructuralObjectReference service ) throws ServiceManagementException;


    /**
     * @param ref
     * @return whether a service manager exists
     */
    boolean hasServiceManagerFor ( StructuralObjectReference ref );


    /**
     * @param service
     * @param managerType
     * @return whether the manager for the service is of the given type
     * @throws ServiceManagementException
     */
    <T extends BaseServiceManager> boolean isServiceManagerType ( StructuralObjectReference service, Class<T> managerType )
            throws ServiceManagementException;

}
