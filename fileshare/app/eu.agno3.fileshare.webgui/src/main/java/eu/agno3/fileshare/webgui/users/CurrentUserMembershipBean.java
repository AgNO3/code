/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.users;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.VirtualGroup;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.subject.SubjectComparator;


/**
 * @author mbechler
 *
 */
@SessionScoped
@Named ( "currentUserMembershipBean" )
public class CurrentUserMembershipBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -33472318309634564L;
    private static final Logger log = Logger.getLogger(CurrentUserMembershipBean.class);

    private boolean groupsLoaded;
    private List<Group> currentUserGroups = Collections.EMPTY_LIST;
    private List<Group> invactiveUserGroups = Collections.EMPTY_LIST;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @return the currentUserGroups
     */
    public List<Group> getCurrentUserGroups () {
        ensureGroupsLoaded();
        return this.currentUserGroups;
    }


    /**
     * 
     * @return the inactive user groups
     */
    public List<Group> getInactiveUserGroups () {
        ensureGroupsLoaded();
        return this.invactiveUserGroups;
    }


    /**
     * 
     * @return the inactive group title
     */
    public String getInactiveTitle () {
        return FileshareMessages.format("hidden.num_inactiveFmt", numInactiveGroups()); //$NON-NLS-1$
    }


    /**
     * 
     * @return the inactive group message
     */
    public String getInactiveMessage () {
        return FileshareMessages.format("hidden.num_inactiveLongFmt", numInactiveGroups()); //$NON-NLS-1$
    }


    /**
     * 
     * @return the number of inactive groups
     */
    public int numInactiveGroups () {
        ensureGroupsLoaded();
        return this.invactiveUserGroups.size();
    }


    /**
     * 
     * @return whether the user has inactive groups
     */
    public boolean haveInactiveGroups () {
        ensureGroupsLoaded();
        return !this.invactiveUserGroups.isEmpty();
    }


    /**
     * 
     * @return whether the user has any groups
     */
    public boolean haveAnyGroups () {
        ensureGroupsLoaded();
        return ( !this.invactiveUserGroups.isEmpty() || !this.currentUserGroups.isEmpty() );
    }


    /**
     * 
     * 
     */
    public void refresh () {
        loadGroups();
    }


    private void ensureGroupsLoaded () {
        if ( this.groupsLoaded ) {
            return;
        }
        loadGroups();
    }


    /**
     * 
     */
    private void loadGroups () {
        try {
            log.debug("Loading group memberships"); //$NON-NLS-1$
            this.groupsLoaded = true;
            List<Group> res = new ArrayList<>(this.fsp.getUserService().getCurrentUserGroupClosure());
            Collections.sort(res, new SubjectComparator());

            this.currentUserGroups = res;
            this.invactiveUserGroups = new ArrayList<>();
            for ( Group g : this.currentUserGroups ) {
                if ( ! ( g instanceof VirtualGroup ) && !g.haveSubjectRoot() ) {
                    this.invactiveUserGroups.add(g);
                }
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }

}
