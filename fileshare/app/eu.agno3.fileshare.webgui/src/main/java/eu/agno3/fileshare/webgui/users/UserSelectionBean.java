/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.users;


import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "userSelectionBean" )
@ViewScoped
public class UserSelectionBean extends AbstractSelectionBean<@Nullable UUID, @Nullable User, FileshareException> {

    /**
     * 
     */
    private static final long serialVersionUID = -5650836392470441994L;

    @Inject
    private FileshareServiceProvider fsp;


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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#handleException(java.lang.Exception)
     */
    @Override
    protected void handleException ( Exception e ) {
        ExceptionHandler.handleException(e);
    }

}
