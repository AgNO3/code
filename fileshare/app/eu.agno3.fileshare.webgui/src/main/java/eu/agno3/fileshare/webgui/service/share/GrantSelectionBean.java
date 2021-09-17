/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "grantSelectionBean" )
@ViewScoped
public class GrantSelectionBean extends AbstractSelectionBean<@Nullable UUID, @Nullable Grant, FileshareException> {

    /**
     * 
     */
    private static final long serialVersionUID = -9061933860337496869L;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#fetchObject(java.util.UUID)
     */
    @Override
    protected Grant fetchObject ( @Nullable UUID selection ) throws FileshareException {
        return this.fsp.getShareService().getGrant(selection);
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
        ExceptionHandler.handleException(e);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#getId(java.lang.Object)
     */
    @Override
    protected UUID getId ( @Nullable Grant obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.getId();
    }

}
