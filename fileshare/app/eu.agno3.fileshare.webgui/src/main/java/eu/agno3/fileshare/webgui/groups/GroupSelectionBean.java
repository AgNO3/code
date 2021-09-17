/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.groups;


import java.io.Serializable;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "userGroupSelectionBean" )
public class GroupSelectionBean extends AbstractSelectionBean<@Nullable UUID, @Nullable Group, FileshareException> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8274744888610804644L;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @param obj
     * @return the object id
     */
    @Override
    protected UUID getId ( @Nullable Group obj ) {
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


    @Override
    protected Group fetchObject ( @Nullable UUID selection ) throws FileshareException {
        return this.fsp.getGroupService().getGroup(selection);
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
