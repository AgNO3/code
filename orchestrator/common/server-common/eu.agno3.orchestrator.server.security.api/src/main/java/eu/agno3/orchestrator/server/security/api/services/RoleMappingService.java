/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.api.services;


import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.server.security.api.Roles;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = RoleMappingServiceDescriptor.NAMESPACE )
public interface RoleMappingService extends SOAPWebService {

    /**
     * @param service
     * @param user
     * @return the locally defined role mapping for the given user (excluding roles contributed by external
     *         authentication)
     * @throws SecurityManagementException
     */
    @WebResult ( name = "roles" )
    Roles getMappedRoles ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "user" ) UserPrincipal user)
            throws SecurityManagementException;


    /**
     * 
     * @param service
     * @param user
     * @param roles
     * @throws SecurityManagementException
     */
    void setMappedRoles ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "user" ) UserPrincipal user,
            @WebParam ( name = "roles" ) Roles roles) throws SecurityManagementException;


    /**
     * @param service
     * @return the known roles
     */
    @WebResult ( name = "roles" )
    Roles getAvailableRoles ( @WebParam ( name = "service" ) ServiceStructuralObject service);
}
