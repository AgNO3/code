/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "app_fs_adm_groupsController" )
public class GroupsController {

    private static final Logger log = Logger.getLogger(GroupsController.class);

    @Inject
    private GroupsTableBean tableBean;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    @Inject
    private GroupMembersBean membersBean;


    /**
     * @param ctx
     * @return outcome
     */
    public String createGroup ( GroupCreateContext ctx ) {

        Long quota = null;
        if ( ctx.getQuotaEnabled() ) {
            quota = ctx.getQuotaSize() * (long) Math.pow(1000, ctx.getQuotaExponent());
        }

        try {
            Group g = this.fsp.getGroupService().createGroup(makeGroup(ctx), ctx.getCreateRoot());

            if ( ctx.getDisableNotifications() ) {
                this.fsp.getGroupService().setNotificationDisabled(g.getId(), ctx.getDisableNotifications());
            }
            if ( !StringUtils.isBlank(ctx.getNotificationOverrideAddress()) ) {
                this.fsp.getGroupService().setNotificationOverride(g.getId(), ctx.getNotificationOverrideAddress());
            }
            if ( ctx.getGroupLocale() != null ) {
                this.fsp.getGroupService().setGroupLocale(g.getId(), ctx.getGroupLocale());
            }

            if ( ctx.getQuotaEnabled() ) {
                this.fsp.getGroupService().updateGroupQuota(g.getId(), quota);
            }

            this.fsp.getSubjectService().setRoles(g.getId(), ctx.getRoles());

            this.tableBean.refresh();
            return DialogContext.closeDialog(g.getId());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param gs
     * @return outcome
     */
    public String deleteGroups ( GroupSelectionBean gs ) {
        if ( gs == null ) {
            return null;
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Deleting groups " + gs.getMultiSelection()); //$NON-NLS-1$
            }
            this.fsp.getGroupService().deleteGroups(gs.getMultiSelectionIds());
            gs.setMultiSelection((List<@Nullable Group>) null);
            this.tableBean.refresh();
            return DialogContext.closeDialog(null);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @return an empty group
     */
    public static Group makeGroup () {
        return new Group();
    }


    /**
     * @param ctx
     * @return
     */
    private static Group makeGroup ( GroupCreateContext ctx ) {
        Group g = new Group();
        g.setName(ctx.getGroupName());
        return g;
    }


    /**
     * @param gs
     * @param subjectIds
     * 
     */
    public void addGroupMembers ( GroupSelectionBean gs, Collection<UUID> subjectIds ) {
        if ( gs == null || subjectIds == null || subjectIds.isEmpty() ) {
            return;
        }

        Group g = gs.getSingleSelection();

        if ( g == null ) {
            return;
        }

        try {
            this.fsp.getGroupService().addMembers(g.getId(), new ArrayList<>(subjectIds));
            gs.refreshSelection();
            this.membersBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

    }


    /**
     * @param gs
     * @param memberId
     */
    public void removeGroupMember ( GroupSelectionBean gs, UUID memberId ) {
        if ( gs == null || memberId == null ) {
            return;
        }

        Group g = gs.getSingleSelection();

        if ( g == null ) {
            return;
        }

        try {
            this.fsp.getGroupService().removeMembers(g.getId(), Arrays.asList(memberId));
            gs.refreshSelection();
            this.membersBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
    }
}
