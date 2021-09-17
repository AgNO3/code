/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.util.GrantComparator;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FilePermissionBean;


/**
 * @author mbechler
 *
 */
@Named ( "shareListBean" )
@ApplicationScoped
public class ShareListBean {

    private static final Logger log = Logger.getLogger(ShareListBean.class);

    private static final int GRANT_LIMIT = 5;

    @Inject
    private FilePermissionBean filePermissionBean;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @param e
     * @return a maximum of limit shares for the entity
     */
    public List<Grant> getFirstShares ( VFSEntity e ) {
        if ( e == null || !this.filePermissionBean.isOwner(e) || ( !e.hasGrants() && !e.hasParent() ) ) {
            return Collections.EMPTY_LIST;
        }

        try {
            log.trace("Loading first grants"); //$NON-NLS-1$
            return this.fsp.getShareService().getFirstGrants(e.getEntityKey(), GRANT_LIMIT);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * @return the grantLimit
     */
    public int getGrantLimit () {
        return GRANT_LIMIT;
    }


    /**
     * 
     * @param e
     * @param limit
     * @return number of grants that exceed the display limit
     */
    public int getExceedingDisplayLimit ( VFSEntity e, int limit ) {
        return Math.max(0, getGrantCount(e) - limit);
    }


    /**
     * 
     * @param e
     * @return number of total grants on an entity
     */
    public int getGrantCount ( VFSEntity e ) {

        if ( e == null || !this.filePermissionBean.isOwner(e) || ( !e.hasGrants() && !e.hasParent() ) ) {
            return 0;
        }

        try {
            log.trace("Loading grant count"); //$NON-NLS-1$
            return this.fsp.getShareService().getGrantCount(e.getEntityKey());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
            return 0;
        }
    }


    /**
     * 
     * @param e
     * @return a list of effective shares for the entity
     */
    public List<Grant> getAllShares ( VFSEntity e ) {

        if ( e == null || !this.filePermissionBean.isOwner(e) || ( !e.hasGrants() && !e.hasParent() ) ) {
            return Collections.EMPTY_LIST;
        }

        try {
            log.trace("Loading all grants"); //$NON-NLS-1$
            List<Grant> grants = new ArrayList<>(this.fsp.getShareService().getEffectiveGrants(e.getEntityKey()));
            Collections.sort(grants, new GrantComparator());
            return grants;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
            return Collections.EMPTY_LIST;
        }
    }
}
