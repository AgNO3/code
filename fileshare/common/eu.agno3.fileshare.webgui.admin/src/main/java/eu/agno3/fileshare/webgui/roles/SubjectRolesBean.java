/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.roles;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SelectableDataModel;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.SubjectSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_subjectRolesBean" )
@ViewScoped
public class SubjectRolesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2857217077071732401L;

    private static final Logger log = Logger.getLogger(SubjectRolesBean.class);

    private RoleDataModel model;

    @Inject
    private RolesBean rolesBean;

    @Inject
    private SubjectSelectionBean subjectSelection;

    private List<String> selectedRoles;

    private Set<String> customRoles = new HashSet<>();

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    private List<String> effectiveRoles;

    private String addCustom;


    /**
     * @return the addCustom
     */
    public String getAddCustom () {
        return this.addCustom;
    }


    /**
     * @param addCustom
     *            the addCustom to set
     */
    public void setAddCustom ( String addCustom ) {
        this.addCustom = addCustom;
    }


    /**
     * 
     * @return null
     */
    public String doAddCustom () {
        if ( StringUtils.isBlank(this.addCustom) ) {
            return null;
        }

        try {
            this.fsp.getSubjectService().addRole(this.subjectSelection.getSingleSelectionId(), this.addCustom);
            refresh();
            this.addCustom = null;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param role
     * @return null
     */
    public String removeCustom ( String role ) {
        try {
            this.fsp.getSubjectService().removeRole(this.subjectSelection.getSingleSelectionId(), role);
            refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @return the available role model
     */
    public SelectableDataModel<String> getModel () {
        if ( this.model == null ) {
            this.model = makeModel();
        }
        return this.model;
    }


    /**
     * @return
     */
    private RoleDataModel makeModel () {
        return new RoleDataModel(this.rolesBean.getAvailableRoles());
    }


    /**
     * @return the user's applied roles
     */
    public String[] getSubjectRoles () {
        if ( this.selectedRoles == null ) {
            this.selectedRoles = makeSubjectRoles();
            this.customRoles = new HashSet<>(this.selectedRoles);
        }

        return this.selectedRoles.toArray(new String[] {});
    }


    /**
     * @return
     */
    private List<String> makeSubjectRoles () {
        Subject singleSelection = this.subjectSelection.getSingleSelection();
        if ( singleSelection != null ) {
            List<String> res = new ArrayList<>(singleSelection.getRoles());
            Collections.sort(res);
            return res;
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * 
     * @param role
     * @return whether this is a custom role
     */
    public boolean isCustomRole ( String role ) {
        getSubjectRoles();
        return this.customRoles.contains(role);
    }


    private void refresh () {
        this.subjectSelection.refreshSelection();
        this.selectedRoles = makeSubjectRoles();
        this.effectiveRoles = makeEffectiveRoleModel();
        this.customRoles = new HashSet<>(this.selectedRoles);
    }


    /**
     * @param selection
     */
    public void setSubjectRoles ( String[] selection ) {
        // ignore
    }


    /**
     * @return the users effective roles
     */
    public List<String> getEffectiveRoleModel () {
        if ( this.effectiveRoles == null ) {
            this.effectiveRoles = makeEffectiveRoleModel();
        }
        return this.effectiveRoles;
    }


    /**
     * @return
     * @throws FileshareException
     */
    private List<String> makeEffectiveRoleModel () {
        try {
            List<String> roles = new ArrayList<>(this.fsp.getSubjectService().getEffectiveRoles(this.subjectSelection.getSingleSelectionId()));
            Collections.sort(roles);
            return roles;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * 
     * @param ev
     */
    public void onSelect ( SelectEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Adding role " + ev.getObject()); //$NON-NLS-1$
        }

        try {
            this.fsp.getSubjectService().addRole(this.subjectSelection.getSingleSelectionId(), (String) ev.getObject());
            refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

    }


    /**
     * 
     * @param ev
     */
    public void onUnselect ( UnselectEvent ev ) {

        if ( log.isDebugEnabled() ) {
            log.debug("Removing role " + ev.getObject()); //$NON-NLS-1$
        }

        try {
            this.fsp.getSubjectService().removeRole(this.subjectSelection.getSingleSelectionId(), (String) ev.getObject());
            refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
    }
}
