/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.service;


/**
 * @author mbechler
 * 
 */
public interface Service {

    /**
     * 
     * @return the current service state
     */
    ServiceState getState ();


    /**
     * Ensure the service instance is started
     * 
     * Does not do anything if the service is already started
     * 
     * @return whether the service was started
     * @throws ServiceException
     *             if the service cannot be started
     */
    boolean start () throws ServiceException;


    /**
     * Ensure the service instance is stopped
     * 
     * Does not do anything if the service is already stopped
     * 
     * @return whether the service was stopped
     * @throws ServiceException
     *             if the service cannot be stopped
     */
    boolean stop () throws ServiceException;


    /**
     * Reload the service instance if possible
     * 
     * If reloading is not possible, a restart will be performed.
     * Does not do anything if the service is not running.
     * 
     * @return whether the service was reloaded
     * @throws ServiceException
     */
    boolean reload () throws ServiceException;


    /**
     * Restart the service instance
     * 
     * Does not do anything if the service is not running.
     * 
     * @return whether the service was restarted
     * @throws ServiceException
     *             if the restart fails
     */
    boolean restart () throws ServiceException;


    /**
     * Restart the service instance, without waiting for completation
     * 
     * @return whether the service restart was triggered
     * @throws ServiceException
     * 
     */
    boolean restartNoWait () throws ServiceException;


    /**
     * Ensure that the service will be started on boot
     * 
     * @throws ServiceException
     */
    void enableOnBoot () throws ServiceException;


    /**
     * Ensure that the service won't be started on boot
     * 
     * @throws ServiceException
     * 
     */
    void disableOnBoot () throws ServiceException;


    /**
     * @return the instance id
     */
    String getInstanceId ();


    /**
     * @return the service name
     */
    String getServiceName ();

}
