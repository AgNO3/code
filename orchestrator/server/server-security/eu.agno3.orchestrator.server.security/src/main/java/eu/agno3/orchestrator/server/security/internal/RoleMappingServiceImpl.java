/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.jws.WebService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.server.security.RoleMappingServerService;
import eu.agno3.orchestrator.server.security.api.Roles;
import eu.agno3.orchestrator.server.security.api.services.RoleMappingService;
import eu.agno3.orchestrator.server.security.api.services.RoleMappingServiceDescriptor;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.PermissionMapper;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.db.impl.AbstractRoleMappingService;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RoleMappingServerService.class, RoleMappingService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.server.security.api.services.RoleMappingService",
    targetNamespace = RoleMappingServiceDescriptor.NAMESPACE,
    serviceName = "roleMappingService" )
@WebServiceAddress ( "/security/roleMapping" )
public class RoleMappingServiceImpl extends AbstractRoleMappingService implements RoleMappingServerService, RoleMappingService {

    private Set<PermissionMapper> permissionMappers = new HashSet<>();
    private EntityTransactionService authEts;


    @Reference ( target = "(persistenceUnit=auth)" )
    protected synchronized void bindEntityTransactionService ( EntityTransactionService ets ) {
        this.authEts = ets;
    }


    protected synchronized void unbindEntityTransactionService ( EntityTransactionService ets ) {
        if ( this.authEts == ets ) {
            this.authEts = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void setPermissionMapper ( PermissionMapper pm ) {
        this.permissionMappers.add(pm);
    }


    protected synchronized void unsetPermissionMapper ( PermissionMapper pm ) {
        this.permissionMappers.remove(pm);
    }


    /**
     * 
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.RoleMappingService#getMappedRoles(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    @RequirePermissions ( "security:user:view:roles" )
    public Roles getMappedRoles ( ServiceStructuralObject service, UserPrincipal user ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.startReadOnly() ) {
            return new Roles(getMappedRoles(tx, user));
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to get mapped roles", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.RoleMappingService#setMappedRoles(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.runtime.security.principal.UserPrincipal, eu.agno3.orchestrator.server.security.api.Roles)
     */
    @Override
    @RequirePermissions ( "security:roles:modify" )
    public void setMappedRoles ( ServiceStructuralObject service, UserPrincipal user, Roles roles ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            setMappedRoles(tx, user, roles.getRoles());
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to get mapped roles", e); //$NON-NLS-1$
        }
    }


    @Override
    @RequirePermissions ( "security:roles:view" )
    public Roles getAvailableRoles ( ServiceStructuralObject service ) {
        Set<String> roles = new TreeSet<>();
        for ( PermissionMapper mapper : this.permissionMappers ) {
            roles.addAll(mapper.getDefinedRoles());
        }
        return new Roles(roles);
    }
}
