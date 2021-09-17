/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_subjectSelectionBean" )
@ViewScoped
public class SubjectSelectionBean extends AbstractSelectionBean<@Nullable UUID, @Nullable Subject, FileshareException> {

    /**
     * 
     */
    private static final long serialVersionUID = 5386820054739900149L;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#fetchObject(java.util.UUID)
     */
    @Override
    protected Subject fetchObject ( @Nullable UUID selection ) throws FileshareException {
        return this.fsp.getSubjectService().getSubject(selection);
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
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#handleException(java.lang.Exception)
     */
    @Override
    protected void handleException ( Exception e ) {
        this.exceptionHandler.handleException(e);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#getId(java.lang.Object)
     */
    @Override
    protected UUID getId ( @Nullable Subject obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.getId();
    }

}
