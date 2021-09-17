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

import eu.agno3.orchestrator.server.security.api.Permissions;
import eu.agno3.orchestrator.server.security.api.Roles;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = PermissionServiceDescriptor.NAMESPACE )
public interface PermissionService extends SOAPWebService {

    /**
     * @param em
     * @return all roles that have permissions assigned
     * @throws SecurityManagementException
     */
    @WebResult ( name = "roles" )
    Roles getDefinedRoles () throws SecurityManagementException;


    /**
     * @param em
     * @param role
     * @return all permission entries for the given role
     * @throws SecurityManagementException
     */
    @WebResult ( name = "permissions" )
    Permissions getRolePermissions ( @WebParam ( name = "role" ) String role ) throws SecurityManagementException;


    /**
     * @param em
     * @param role
     * @param permissions
     * @throws SecurityManagementException
     */
    void setRolePermissions ( @WebParam ( name = "role" ) String role, @WebParam ( name = "permissions" ) Permissions permissions )
            throws SecurityManagementException;

}
