/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "app_fs_adm_userSelectionBean" )
public class UserSelectionBean extends AbstractSelectionBean<@Nullable UUID, @Nullable User, FileshareException> {

    private static final Logger log = Logger.getLogger(UserSelectionBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -8274744888610804644L;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    private SecurityLabel cachedSubjectRootLabel;

    private boolean subjectRootLoaded;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.base.AbstractSelectionBean#getId(java.lang.Object)
     */
    @Override
    protected UUID getId ( @Nullable User obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.getId();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#parseId(java.lang.String)
     */
    @Override
    protected @Nullable UUID parseId ( String id ) {
        return UUID.fromString(id);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.base.AbstractSelectionBean#fetchObject(java.util.UUID)
     */
    @Override
    protected User fetchObject ( @Nullable UUID selection ) throws FileshareException {
        return this.fsp.getUserService().getUser(selection);
    }


    /**
     * @return the cachedSubjectRoot
     */
    public SecurityLabel getSubjectRootLabel () {
        if ( !this.subjectRootLoaded ) {
            this.subjectRootLoaded = true;
            try {
                log.debug("loading subject root"); //$NON-NLS-1$
                this.cachedSubjectRootLabel = this.fsp.getSubjectService().getSubjectRootLabel(this.getSingleSelectionId());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                this.exceptionHandler.handleException(e);
                return null;
            }
        }
        return this.cachedSubjectRootLabel;
    }


    /**
     * 
     */
    public void refreshSubjectRootLabel () {
        this.subjectRootLoaded = false;
        this.cachedSubjectRootLabel = null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#refreshSelection()
     */
    @Override
    public void refreshSelection () {
        this.subjectRootLoaded = false;
        this.cachedSubjectRootLabel = null;
        super.refreshSelection();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#handleException(java.lang.Exception)
     */
    @Override
    protected void handleException ( Exception e ) {
        this.exceptionHandler.handleException(e);
    }
}
