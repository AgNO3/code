/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.10.2014 by mbechler
 */
package eu.agno3.fileshare.security.internal;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.security.LocalUserService;
import eu.agno3.fileshare.security.PermissionsService;
import eu.agno3.fileshare.security.RoleMappingService;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.db.BaseUser;
import eu.agno3.runtime.security.db.impl.AbstractLocalSecurityInitializer;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( configurationPid = FileshareSecurityInitializerImpl.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class FileshareSecurityInitializerImpl extends AbstractLocalSecurityInitializer<BaseUser> {

    /**
     * 
     */
    public static final String PID = "auth.init"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileshareSecurityInitializerImpl.class);

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
    protected synchronized void setLocalUserService ( LocalUserService lus ) {
        super.setLocalUserService(lus);
    }


    protected synchronized void unsetLocalUserService ( LocalUserService lus ) {
        super.unsetLocalUserService(lus);
    }


    @Reference
    protected synchronized void setPermissionService ( PermissionsService ps ) {
        super.setPermissionService(ps);
    }


    protected synchronized void unsetPermissionService ( PermissionsService ps ) {
        super.unsetPermissionService(ps);
    }


    @Reference
    protected synchronized void setRoleMappingService ( RoleMappingService rms ) {
        super.setRoleMappingService(rms);
    }


    protected synchronized void unsetRoleMappingService ( RoleMappingService rms ) {
        super.unsetRoleMappingService(rms);
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {
        log.debug("Initializing authentication database"); //$NON-NLS-1$
        String initUserName = (String) ctx.getProperties().get(STATIC_USER);
        String initUserPassword = ConfigUtil.parseSecret(ctx.getProperties(), STATIC_USER_PASSWORD, null);
        String initUserPasswordHash = ConfigUtil.parseSecret(ctx.getProperties(), STATIC_USER_PASSWORD_HASH, null);
        String rolesStr = (String) ctx.getProperties().get(STATIC_USER_ROLES);
        boolean resetPassword = ConfigUtil.parseBoolean(ctx.getProperties(), "resetPasswordIfExists", false); //$NON-NLS-1$
        Set<String> roles = new HashSet<>();
        if ( rolesStr == null ) {
            roles.add(ADMIN_ROLE);
        }
        else {
            for ( String role : StringUtils.split(rolesStr, ',') ) {
                roles.add(role.trim());
            }
        }

        if ( !StringUtils.isBlank(initUserPasswordHash) ) {
            try {
                super.initialize(initUserName, SCryptResult.importFrom(initUserPasswordHash.trim()), roles, resetPassword);
            }
            catch ( IOException e ) {
                log.error("Failed to parse password hash", e); //$NON-NLS-1$
            }
        }
        else {
            super.initialize(initUserName, initUserPassword, roles, resetPassword);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.impl.AbstractLocalSecurityInitializer#createTransactionContext()
     */
    @Override
    protected EntityTransactionContext createTransactionContext () throws EntityTransactionException {
        return this.auth.start();
    }
}
