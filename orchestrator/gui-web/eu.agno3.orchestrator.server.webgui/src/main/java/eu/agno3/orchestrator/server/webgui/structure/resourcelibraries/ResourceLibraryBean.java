/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.Serializable;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "resourceLibraryBean" )
@ViewScoped
public class ResourceLibraryBean extends AbstractSelectionBean<@Nullable UUID, @Nullable ResourceLibrary, GuiWebServiceException>
        implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8867416167439603045L;

    @Inject
    private ServerServiceProvider ssp;


    /**
     * 
     * @return dialog close if successful, null otherwise
     */
    public String delete () {
        try {
            this.ssp.getService(ResourceLibraryService.class).delete(this.getSingleSelectionId());
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#fetchObject(java.util.UUID)
     */
    @Override
    protected ResourceLibrary fetchObject ( @Nullable UUID selection ) throws GuiWebServiceException {
        try {
            return this.ssp.getService(ResourceLibraryService.class).getById(selection);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#getId(java.lang.Object)
     */
    @Override
    protected UUID getId ( @Nullable ResourceLibrary obj ) {
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
     * @see eu.agno3.runtime.jsf.util.selection.AbstractSelectionBean#handleException(java.lang.Exception)
     */
    @Override
    protected void handleException ( Exception e ) {
        ExceptionHandler.handle(e);
    }
}
