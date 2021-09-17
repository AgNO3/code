/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.SelectEvent;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.SubjectAutoCompleteBean;
import eu.agno3.fileshare.webgui.admin.SubjectComparator;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_groupMembersBean" )
@ViewScoped
public class GroupMembersBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7918727023942957729L;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    @Inject
    private GroupSelectionBean groupSelection;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private GroupsController groupsController;

    @Inject
    private SubjectAutoCompleteBean autoComp;

    private List<Subject> membersModel;


    /**
     * @return the group model
     */
    public List<Subject> getMembersModel () {
        if ( this.membersModel == null ) {
            this.membersModel = makeMembersModel();
        }

        return this.membersModel;
    }


    /**
     * 
     * @return null
     */
    public String refresh () {
        this.membersModel = null;
        return null;
    }


    /**
     * @return
     */
    private List<Subject> makeMembersModel () {
        try {
            List<Subject> res = new ArrayList<>(this.fsp.getGroupService().getMembers(this.groupSelection.getSingleSelectionId()));
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
    public List<SubjectQueryResult> completeAddableSubjects ( String query ) {
        try {
            return this.fsp.getSubjectService().querySubjectsExcludingMembers(query, this.groupSelection.getSingleSelectionId(), 20);
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
    public void onSubjectAddSelect ( SelectEvent ev ) {
        if ( ! ( ev.getObject() instanceof SubjectQueryResult ) ) {
            return;
        }

        SubjectQueryResult g = (SubjectQueryResult) ev.getObject();
        this.addToGroups(Arrays.asList(g.getId()));
    }


    /**
     * @param ev
     */
    public void onSubjectIdAddSelect ( SelectEvent ev ) {
        if ( ! ( ev.getObject() instanceof UUID ) ) {
            return;
        }

        UUID groupId = (UUID) ev.getObject();
        this.addToGroups(Arrays.asList(groupId));

    }


    /**
     * @param groupIds
     */
    public void addToGroups ( Collection<UUID> groupIds ) {
        this.groupsController.addGroupMembers(this.groupSelection, new HashSet<>(groupIds));
        this.autoComp.setValue(null);
        this.autoComp.getComponent().setValue(null);
        this.autoComp.getComponent().setSubmittedValue(null);
        this.autoComp.getComponent().resetValue();
        this.membersModel = makeMembersModel();
    }
}
