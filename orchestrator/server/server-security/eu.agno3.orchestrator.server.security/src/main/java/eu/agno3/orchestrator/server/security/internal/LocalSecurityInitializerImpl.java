/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import java.util.Collections;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.security.LocalSecurityInitializer;
import eu.agno3.orchestrator.server.security.LocalUserServerService;
import eu.agno3.orchestrator.server.security.PermissionsServerService;
import eu.agno3.orchestrator.server.security.RoleMappingServerService;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.db.BaseUser;
import eu.agno3.runtime.security.db.impl.AbstractLocalSecurityInitializer;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true, configurationPid = LocalSecurityInitializerImpl.PID )
public class LocalSecurityInitializerImpl extends AbstractLocalSecurityInitializer<BaseUser> implements LocalSecurityInitializer {

    /**
     * 
     */
    public static final String PID = "server.auth.init"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LocalSecurityInitializerImpl.class);

    private EntityTransactionService auth;


    @Reference ( target = "(persistenceUnit=auth)" )
    protected synchronized void bindEntityTransactionService ( EntityTransactionService ets ) {
        this.auth = ets;
    }


    protected synchronized void unbindEntityTransactionService ( EntityTransactionService ets ) {
        if ( this.auth == ets ) {
            this.auth = null;
        }
    }


    @Reference
    protected synchronized void setLocalUserService ( LocalUserServerService lus ) {
        super.setLocalUserService(lus);
    }


    protected synchronized void unsetLocalUserService ( LocalUserServerService lus ) {
        super.unsetLocalUserService(lus);
    }


    @Reference
    protected synchronized void setPermissionService ( PermissionsServerService ps ) {
        super.setPermissionService(ps);
    }


    protected synchronized void unsetPermissionService ( PermissionsServerService ps ) {
        super.unsetPermissionService(ps);
    }


    @Reference
    protected synchronized void setRoleMappingService ( RoleMappingServerService rms ) {
        super.setRoleMappingService(rms);
    }


    protected synchronized void unsetRoleMappingService ( RoleMappingServerService rms ) {
        super.unsetRoleMappingService(rms);
    }


    @Override
    protected EntityTransactionContext createTransactionContext () throws EntityTransactionException {
        return this.auth.start();
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        log.debug("Initializing authentication database"); //$NON-NLS-1$
        String initUserName = (String) ctx.getProperties().get(STATIC_USER);
        String initUserPassword = (String) ctx.getProperties().get(STATIC_USER_PASSWORD);
        initialize(initUserName, initUserPassword, Collections.singleton(ADMIN_ROLE), true);
    }
}
