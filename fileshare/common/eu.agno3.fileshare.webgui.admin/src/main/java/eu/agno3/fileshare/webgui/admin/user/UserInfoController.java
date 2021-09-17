/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_userInfoController" )
@ApplicationScoped
public class UserInfoController {

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private UserSelectionBean userSelection;

    @Inject
    private UserRootSecurityLabelEditorBean userRootLabel;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;


    /**
     * @param u
     * @param label
     */
    public void updateSecurityLabel ( User u, String label ) {

        try {
            this.fsp.getUserService().updateUserLabel(u.getId(), label);
            this.userSelection.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
    }


    /**
     * @param subject
     * @param label
     * @return outcome
     */
    public String updateSubjectSecurityLabel ( Subject subject, String label ) {

        try {
            this.fsp.getSubjectService().setSubjectRootSecurityLabel(subject.getId(), label);
            this.userSelection.refreshSelection();
            this.userRootLabel.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * @param subject
     * @param label
     * @return null
     */
    public String updateSubjectSecurityLabelRecursive ( Subject subject, String label ) {
        try {
            this.fsp.getSubjectService().setSubjectRootSecurityLabelRecursive(subject.getId(), label);
            this.userSelection.refreshSelection();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Recusively set security label", StringUtils.EMPTY)); //$NON-NLS-1$
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }
}
