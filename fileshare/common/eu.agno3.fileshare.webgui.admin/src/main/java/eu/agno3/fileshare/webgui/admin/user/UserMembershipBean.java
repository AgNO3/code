/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.SubjectComparator;
import eu.agno3.fileshare.webgui.admin.group.GroupAutocompleteBean;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_userMembershipBean" )
@ViewScoped
public class UserMembershipBean implements Serializable {

    private static final Logger log = Logger.getLogger(UserMembershipBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -7918727023942957729L;

    @Inject
    private UserSelectionBean userSelection;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    @Inject
    private UserController userController;

    @Inject
    private GroupAutocompleteBean autoComp;

    private List<Group> effectiveGroupModel;

    private List<Group> groupModel;


    /**
     * @return the group model
     */
    public List<Group> getGroupModel () {
        if ( this.groupModel == null ) {
            this.groupModel = makeGroupModel();
        }

        return this.groupModel;
    }


    /**
     * @return
     */
    private List<Group> makeGroupModel () {
        try {
            List<Group> res = new ArrayList<>(this.fsp.getUserService().getUserGroups(this.userSelection.getSingleSelectionId()));
            Collections.sort(res, new SubjectComparator());
            return res;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * 
     * @return the user's effective memberships
     */
    public List<Group> getEffectiveGroupModel () {
        if ( this.effectiveGroupModel == null ) {
            this.effectiveGroupModel = makeEffectiveGroupModel();
        }
        return this.effectiveGroupModel;
    }


    /**
     * @return
     */
    private List<Group> makeEffectiveGroupModel () {
        try {
            List<Group> res = this.fsp.getUserService().getUserGroupClosure(this.userSelection.getSingleSelectionId());
            Collections.sort(res, new SubjectComparator());
            return res;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * @param query
     * @return completion results
     */
    public List<GroupQueryResult> completeAddableGroups ( String query ) {
        try {
            return this.fsp.getGroupService().queryGroupsExcludingUserGroups(query, this.userSelection.getSingleSelectionId(), 20);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * @param ev
     */
    public void onGroupAddSelect ( SelectEvent ev ) {
        if ( ! ( ev.getObject() instanceof GroupQueryResult ) ) {
            log.debug("Not a group selected"); //$NON-NLS-1$
            return;
        }

        GroupQueryResult g = (GroupQueryResult) ev.getObject();
        this.addToGroups(Arrays.asList(g.getId()));
    }


    /**
     * @param ev
     */
    public void onGroupIdAddSelect ( SelectEvent ev ) {
        if ( ! ( ev.getObject() instanceof UUID ) ) {
            log.debug("Not a group selected"); //$NON-NLS-1$
            return;
        }

        UUID groupId = (UUID) ev.getObject();
        this.addToGroups(Arrays.asList(groupId));

    }


    /**
     * @param groupIds
     */
    public void addToGroups ( Collection<UUID> groupIds ) {
        this.userController.addToGroups(this.userSelection, new HashSet<>(groupIds));
        refresh();
        this.autoComp.setValue(null);
        this.autoComp.getComponent().setValue(null);
        this.autoComp.getComponent().setSubmittedValue(null);
        this.autoComp.getComponent().resetValue();
    }


    /**
     * 
     */
    private void refresh () {
        this.groupModel = makeGroupModel();
        this.effectiveGroupModel = makeEffectiveGroupModel();
    }


    /**
     * @param us
     * @param groupId
     * @return outcome
     */
    public String removeFromGroup ( UserSelectionBean us, UUID groupId ) {
        String res = this.userController.removeFromGroup(us, groupId);
        refresh();
        return res;
    }


    /**
     * @param us
     * @param groupIds
     * @return outcome
     */
    public String removeFromGroups ( UserSelectionBean us, Set<UUID> groupIds ) {
        String res = this.userController.removeFromGroups(us, groupIds);
        refresh();
        return res;
    }
}
