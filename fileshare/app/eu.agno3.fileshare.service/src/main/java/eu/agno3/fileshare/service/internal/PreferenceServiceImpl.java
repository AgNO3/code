/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.PreferenceService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PreferenceServiceInternal;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    PreferenceService.class, PreferenceServiceInternal.class
} )
public class PreferenceServiceImpl implements PreferenceService, PreferenceServiceInternal {

    private DefaultServiceContext ctx;

    private UserServiceInternal userService;

    private AccessControlService accessControl;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setUserService ( UserServiceInternal us ) {
        this.userService = us;
    }


    protected synchronized void unsetUserService ( UserServiceInternal us ) {
        if ( this.userService == us ) {
            this.userService = null;
        }
    }


    @Reference
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.PreferenceService#loadPreferences()
     */
    @Override
    public Map<String, String> loadPreferences () throws FileshareException {

        if ( !this.accessControl.isUserAuthenticated() ) {
            return Collections.EMPTY_MAP;
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return new HashMap<>(this.userService.getCurrentUser(tx).getPreferences());
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to load preferences", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.PreferenceService#savePreferences(java.util.Map)
     */
    @Override
    public Map<String, String> savePreferences ( Map<String, String> prefs ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return prefs;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User u = this.userService.getCurrentUser(tx);
            u.setPreferences(prefs);
            em.persist(u);
            em.flush();
            tx.commit();
            return u.getPreferences();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to save preferences", e); //$NON-NLS-1$
        }

    }
}
