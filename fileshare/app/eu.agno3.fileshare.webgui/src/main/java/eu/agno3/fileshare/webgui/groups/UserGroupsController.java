/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.groups;


import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "userGroupsController" )
@ApplicationScoped
public class UserGroupsController {

    private static final Logger log = Logger.getLogger(UserGroupsController.class);

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @param gs
     * @return dialog close or null
     */
    public String activateGroup ( GroupSelectionBean gs ) {
        if ( gs == null || gs.getSingleSelection() == null ) {
            return null;
        }
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Enabling group " + gs.getSingleSelection()); //$NON-NLS-1$
            }
            this.fsp.getBrowseService().getOrCreateGroupRoot(gs.getSingleSelectionId());
            return DialogContext.closeDialog(true);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return null;
    }

}
