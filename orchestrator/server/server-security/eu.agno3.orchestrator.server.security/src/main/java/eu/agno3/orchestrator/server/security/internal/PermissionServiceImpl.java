/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import javax.jws.WebService;
import javax.persistence.EntityManagerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.security.PermissionsServerService;
import eu.agno3.orchestrator.server.security.api.Permissions;
import eu.agno3.orchestrator.server.security.api.Roles;
import eu.agno3.orchestrator.server.security.api.services.PermissionService;
import eu.agno3.orchestrator.server.security.api.services.PermissionServiceDescriptor;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.db.impl.AbstractPermissionService;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    PermissionsServerService.class, PermissionService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.server.security.api.services.PermissionService",
    targetNamespace = PermissionServiceDescriptor.NAMESPACE,
    serviceName = "permission" )
@WebServiceAddress ( "/security/permission" )
public class PermissionServiceImpl extends AbstractPermissionService implements PermissionsServerService, PermissionService {

    private EntityManagerFactory entityManagerFactory;


    @Reference ( target = "(persistenceUnit=auth)" )
    protected synchronized void bindEntityManagerFactory ( EntityManagerFactory emf ) {
        this.entityManagerFactory = emf;
    }


    protected synchronized void unbindEntityManagerFactory ( EntityManagerFactory emf ) {
        if ( this.entityManagerFactory == emf ) {
            this.entityManagerFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.PermissionService#getDefinedRoles()
     */
    @Override
    @RequirePermissions ( "security:role:list:defined" )
    public Roles getDefinedRoles () throws SecurityManagementException {
        return new Roles(this.getDefinedRoles(this.entityManagerFactory.createEntityManager()));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.PermissionService#getRolePermissions(java.lang.String)
     */
    @Override
    @RequirePermissions ( "security:permission:view" )
    public Permissions getRolePermissions ( String role ) throws SecurityManagementException {
        return new Permissions(this.getRolePermissions(this.entityManagerFactory.createEntityManager(), role));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.PermissionService#setRolePermissions(java.lang.String,
     *      eu.agno3.orchestrator.server.security.api.Permissions)
     */
    @Override
    @RequirePermissions ( "security:permission:modify" )
    public void setRolePermissions ( String role, Permissions permissions ) throws SecurityManagementException {
        this.setRolePermissions(this.entityManagerFactory.createEntityManager(), role, permissions.getPermissions());
    }
}
