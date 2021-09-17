/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.service;


import java.util.Collection;

import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 * 
 */
public interface ServiceSystem extends SystemService {

    /**
     * 
     * @return a collection of all known services
     * @throws ServiceException
     */
    Collection<Service> getServices () throws ServiceException;


    /**
     * Fetch a service instance
     * 
     * Will fail if there are multiple instances
     * 
     * @param name
     * @return the service object
     * @throws ServiceException
     */
    Service getService ( String name ) throws ServiceException;


    /**
     * Fetch a service instance
     * 
     * @param name
     * @param instance
     *            instance identifier
     * @return the service object
     * @throws ServiceException
     */
    Service getService ( String name, String instance ) throws ServiceException;


    /**
     * @param name
     * @param instance
     * @return the new service instance
     * @throws ServiceException
     */
    Service createInstance ( String name, String instance ) throws ServiceException;

}
