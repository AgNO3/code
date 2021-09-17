/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.authz;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;


/**
 * @author mbechler
 *
 */
@Component ( service = ObjectAccessControl.class, configurationPid = ObjectAccessControlImpl.PID )
public class ObjectAccessControlImpl implements ObjectAccessControl {

    /**
     * 
     */
    public static final String PID = "objectAccessControl"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ObjectAccessControlImpl.class);

    private boolean disabled = false;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        String disablesSpec = (String) ctx.getProperties().get("dev.disable"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(disablesSpec) && Boolean.parseBoolean(disablesSpec.trim()) ) {
            this.disabled = true;
        }
    }


    /**
     * @return the disabled
     */
    @Override
    public boolean isDisabled () {
        return this.disabled;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl#hasAccess(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String)
     */
    @Override
    public boolean hasAccess ( @Nullable StructuralObject obj, @Nullable String permission ) {
        if ( this.disabled ) {
            return true;
        }
        Subject s = SecurityUtils.getSubject();

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Checking permission %s of %s to %s ", permission, s.getPrincipal(), obj)); //$NON-NLS-1$
        }
        return true;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl#checkAccess(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String)
     */
    @Override
    public void checkAccess ( @Nullable StructuralObject obj, @Nullable String permission ) {
        if ( this.disabled ) {
            return;
        }
        if ( !this.hasAccess(obj, permission) ) {
            throw new UnauthorizedException();
        }
    }
}
