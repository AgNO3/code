/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;


/**
 * Copied from InMemoryServiceRegistryDaoImpl, remove warning
 * 
 * @author mbechler
 *
 */
public class StaticServiceRegistryDAO implements ServiceRegistryDao {

    @NotNull
    private List<RegisteredService> registeredServices = new ArrayList<>();


    @Override
    public boolean delete ( final RegisteredService registeredService ) {
        return this.registeredServices.remove(registeredService);
    }


    @Override
    public RegisteredService findServiceById ( final long id ) {
        for ( final RegisteredService r : this.registeredServices ) {
            if ( r.getId() == id ) {
                return r;
            }
        }

        return null;
    }


    @Override
    public List<RegisteredService> load () {
        return this.registeredServices;
    }


    @Override
    public RegisteredService save ( final RegisteredService registeredService ) {
        if ( registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE ) {
            ( (AbstractRegisteredService) registeredService ).setId(findHighestId() + 1);
        }

        this.registeredServices.remove(registeredService);
        this.registeredServices.add(registeredService);

        return registeredService;
    }


    /**
     * @param registeredServices
     */
    public void setRegisteredServices ( final List<RegisteredService> registeredServices ) {
        this.registeredServices = registeredServices;
    }


    /**
     * This isn't super-fast but we don't expect thousands of services.
     *
     * @return the highest service id in the list of registered services
     */
    private long findHighestId () {
        long id = 0;

        for ( final RegisteredService r : this.registeredServices ) {
            if ( r.getId() > id ) {
                id = r.getId();
            }
        }

        return id;
    }

}
