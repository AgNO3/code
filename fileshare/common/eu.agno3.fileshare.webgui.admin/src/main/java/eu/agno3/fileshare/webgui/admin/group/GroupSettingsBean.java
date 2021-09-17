/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;

import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "app_fs_adm_groupSettingsBean" )
public class GroupSettingsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3048969453453557311L;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    @Inject
    private GroupSelectionBean groupSelection;


    /**
     * 
     * @param ev
     */
    public void updateDisableNotification ( AjaxBehaviorEvent ev ) {

        try {
            @Nullable
            Group singleSelection = this.groupSelection.getSingleSelection();

            if ( singleSelection == null ) {
                return;
            }

            this.fsp.getGroupService().setNotificationDisabled(singleSelection.getId(), singleSelection.getDisableNotifications());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        this.groupSelection.refreshSelection();
    }


    /**
     * 
     * @param ev
     */
    public void updateNotificationAddress ( AjaxBehaviorEvent ev ) {
        try {
            @Nullable
            Group singleSelection = this.groupSelection.getSingleSelection();

            if ( singleSelection == null ) {
                return;
            }

            this.fsp.getGroupService().setNotificationOverride(singleSelection.getId(), singleSelection.getNotificationOverrideAddress());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        this.groupSelection.refreshSelection();
    }


    /**
     * @param ev
     */
    public void updateGroupLocale ( AjaxBehaviorEvent ev ) {
        try {
            @Nullable
            Group singleSelection = this.groupSelection.getSingleSelection();

            if ( singleSelection == null ) {
                return;
            }

            this.fsp.getGroupService().setGroupLocale(singleSelection.getId(), singleSelection.getGroupLocale());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        this.groupSelection.refreshSelection();
    }


    /**
     * 
     * @param ev
     */
    public void reset ( AjaxBehaviorEvent ev ) {
        this.groupSelection.refreshSelection();
    }
}
