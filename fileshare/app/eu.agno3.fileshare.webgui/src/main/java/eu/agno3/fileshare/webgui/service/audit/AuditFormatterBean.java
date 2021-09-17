/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.audit;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.audit.BaseFileshareEvent;
import eu.agno3.fileshare.model.audit.EntityFileshareEvent;
import eu.agno3.fileshare.model.audit.MoveEntityFileshareEvent;
import eu.agno3.fileshare.model.audit.MultiEntityFileshareEvent;
import eu.agno3.fileshare.model.audit.SingleEntityFileshareEvent;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.share.SharePermissionsBean;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
@Named ( "auditFormatterBean" )
@ApplicationScoped
public class AuditFormatterBean {

    /**
     * 
     */
    private static final String THIS_ENTITY_NAME = "audit.this-entity-name"; //$NON-NLS-1$
    private static final String BLANK_ICON = "ui-icon-blank"; //$NON-NLS-1$
    private static final String LINK_GRANT_ICON = "ui-icon-link"; //$NON-NLS-1$
    private static final String MAIL_GRANT_ICON = "ui-icon-mail-open"; //$NON-NLS-1$
    private static final String SUBJECT_GRANT_ICON = "ui-icon-person"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String MOVE_AWAY = "MOVE_AWAY"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String MOVE_HERE = "MOVE_HERE"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String LIST_SEP = ", "; //$NON-NLS-1$

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @param localId
     * @param ev
     * @return formatted action
     */
    public static String formatAction ( EntityKey localId, MapEvent ev ) {
        return getAction(localId, ev);
    }


    /**
     * @param localId
     * @param ev
     * @return
     */
    private static String getAction ( EntityKey localId, MapEvent ev ) {
        String action = (String) ev.get(BaseFileshareEvent.ACTION);

        if ( MoveEntityFileshareEvent.MOVE_ACTION.equals(action) ) {
            String localIdStr = localId.toString();
            if ( ev.get(MoveEntityFileshareEvent.TARGET_ID).equals(localIdStr) ) {
                action = MOVE_HERE;
            }
            else {
                action = MOVE_AWAY;
            }
        }
        return action;
    }


    /**
     * @param localId
     * @param ev
     * @return the action icon
     */
    @SuppressWarnings ( "unchecked" )
    public static String getActionIcon ( EntityKey localId, MapEvent ev ) {
        Map<String, Object> props = (Map<String, Object>) ev.get(BaseFileshareEvent.PROPERTIES);
        if ( props == null ) {
            props = Collections.EMPTY_MAP;
        }
        switch ( getAction(localId, ev) ) {
        case EntityFileshareEvent.DOWNLOAD_ACTION:
        case EntityFileshareEvent.DOWNLOAD_FOLDER_ACTION:
        case EntityFileshareEvent.DOWNLOAD_MULTI_ACTION:
            return "ui-icon-download"; //$NON-NLS-1$
        case EntityFileshareEvent.CREATE_FOLDER_ACTION:
            return "ui-icon-folder-open"; //$NON-NLS-1$
        case EntityFileshareEvent.CREATE_ACTION:
            return "ui-icon-plusthick"; //$NON-NLS-1$
        case EntityFileshareEvent.REPLACE_ACTION:
        case EntityFileshareEvent.CREATE_OR_REPLACE_ACTION:
            return "ui-icon-pencil"; //$NON-NLS-1$
        case EntityFileshareEvent.DELETE_ACTION:
        case EntityFileshareEvent.DELETE_OUTER_ACTION:
        case EntityFileshareEvent.REVOKE_GRANT_ACTION:
        case EntityFileshareEvent.GRANT_EXPIRE_ACTION:
        case EntityFileshareEvent.EXPIRE_ACTION:
            return "ui-icon-trash"; //$NON-NLS-1$
        case EntityFileshareEvent.SHARE_SUBJECT_ACTION:
            return SUBJECT_GRANT_ICON;
        case EntityFileshareEvent.SHARE_MAIL_ACTION:
            return MAIL_GRANT_ICON;
        case EntityFileshareEvent.RECREATE_SHARE_LINK_ACTION:
        case EntityFileshareEvent.SHARE_LINK_ACTION:
            return LINK_GRANT_ICON;
        case EntityFileshareEvent.GRANT_SET_EXPIRY_ACTION:
        case EntityFileshareEvent.GRANT_SET_PERMISSIONS_ACTION:
            return getGrantTypeIcon(props);
        case EntityFileshareEvent.SET_SECURITY_LABEL_ACTION:
        case EntityFileshareEvent.SET_SECURITY_LABEL_RECURSIVE_ACTION:
            return "ui-icon-key"; //$NON-NLS-1$
        case EntityFileshareEvent.SET_MIMETYPE_ACTION:
        case EntityFileshareEvent.SET_EXPIRY_ACTION:
        case EntityFileshareEvent.UNSET_EXPIRY_ACTION:
        case EntityFileshareEvent.SET_ALLOW_FILEOVERRIDE_ACTION:
        case EntityFileshareEvent.UNSET_ALLOW_FILEOVERRIDE_ACTION:
        case EntityFileshareEvent.SET_SENDNOTIFICATIONS_ACTION:
        case EntityFileshareEvent.UNSET_SENDNOTIFICATIONS_ACTION:
        case EntityFileshareEvent.GRANT_SET_COMMENT_ACTION:
        case EntityFileshareEvent.RENAME_ACTION:
            return "ui-icon-gear"; //$NON-NLS-1$
        case MOVE_HERE:
            return "ui-icon-clipboard"; //$NON-NLS-1$
        case MOVE_AWAY:
            return "ui-icon-scissors"; //$NON-NLS-1$
        default:
            return BLANK_ICON;
        }

    }


    /**
     * @param props
     * @return
     */
    private static String getGrantTypeIcon ( Map<String, Object> props ) {
        switch ( (String) props.get(BaseFileshareEvent.TYPE) ) {
        case BaseFileshareEvent.TYPE_SUBJECT:
            return SUBJECT_GRANT_ICON;
        case BaseFileshareEvent.TYPE_MAIL:
            return MAIL_GRANT_ICON;
        case BaseFileshareEvent.TYPE_LINK:
            return LINK_GRANT_ICON;
        default:
            return BLANK_ICON;
        }
    }


    /**
     * @param localId
     * @param ev
     * @return whether the referenced entity is the local entity
     */
    public boolean isLocalEntity ( EntityKey localId, MapEvent ev ) {
        Object entityId = getEntityIdMatch(localId, ev);
        if ( entityId == null ) {
            return false;
        }
        return entityId.equals(localId);
    }


    /**
     * 
     * @param localId
     * @param ev
     * @return formatted entity name
     */
    @SuppressWarnings ( "unchecked" )
    public String getEntityName ( EntityKey localId, MapEvent ev ) {
        String localIdString = localId.toString();
        if ( isSingleEntityEvent(ev) ) {
            if ( !isLocalEntity(localId, ev) ) {
                return (String) ev.get(SingleEntityFileshareEvent.TARGET_ENTITY_NAME);
            }
            return FileshareMessages.get(THIS_ENTITY_NAME);
        }
        else if ( isMultiEntityEvent(ev) ) {

            if ( ev.get(MultiEntityFileshareEvent.TARGET_PARENT_ID) != null
                    && ev.get(MultiEntityFileshareEvent.TARGET_PARENT_ID).equals(localIdString) ) {
                return FileshareMessages.get(THIS_ENTITY_NAME);
            }

            if ( ev.get(MultiEntityFileshareEvent.TARGET_PARENT_NAME) != null ) {
                return (String) ev.get(MultiEntityFileshareEvent.TARGET_PARENT_NAME);
            }

            List<String> entityIds = (List<String>) ev.get(MultiEntityFileshareEvent.TARGET_ENTITY_IDS);
            List<String> fileNames = (List<String>) ev.get(MultiEntityFileshareEvent.TARGET_ENTITY_NAMES);
            List<String> thisRootNames = new LinkedList<>();
            for ( int i = 0; i < Math.min(entityIds.size(), fileNames.size()); i++ ) {
                if ( entityIds.get(i).equals(localIdString) ) {
                    thisRootNames.add(fileNames.get(i));
                }
            }
            return StringUtils.join(thisRootNames, LIST_SEP);
        }
        else if ( isMoveEntityEvent(ev) ) {
            if ( ev.get(MoveEntityFileshareEvent.TARGET_ID) != null && ev.get(MoveEntityFileshareEvent.TARGET_ID).equals(localIdString) ) {
                // display source for target
                return StringUtils.join((List<String>) ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_NAMES), LIST_SEP);
            }
            else if ( ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_PARENT_IDS) != null
                    && ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_NAMES) != null ) {

                List<String> sourceIds = (List<String>) ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_IDS);
                if ( sourceIds != null && sourceIds.contains(localIdString) ) {
                    return FileshareMessages.get(THIS_ENTITY_NAME);
                }
                List<String> parentIds = (List<String>) ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_PARENT_IDS);
                List<String> fileNames = (List<String>) ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_NAMES);
                List<String> thisRootNames = new LinkedList<>();
                for ( int i = 0; i < Math.min(parentIds.size(), fileNames.size()); i++ ) {
                    if ( parentIds.get(i).equals(localIdString) ) {
                        thisRootNames.add(fileNames.get(i));
                    }
                }
                return StringUtils.join(thisRootNames, LIST_SEP);
            }

            return (String) ev.get(MoveEntityFileshareEvent.TARGET_NAME);
        }

        return null;
    }


    /**
     * 
     * @param localId
     * @param ev
     * @return extra information to display
     */
    @SuppressWarnings ( "unchecked" )
    public static String getExtraInfo ( EntityKey localId, MapEvent ev ) {
        Map<String, Object> props = (Map<String, Object>) ev.get(BaseFileshareEvent.PROPERTIES);
        if ( props == null ) {
            props = Collections.EMPTY_MAP;
        }
        switch ( getAction(localId, ev) ) {
        case EntityFileshareEvent.SHARE_SUBJECT_ACTION:
            return formatSubjectShareDetail(props);
        case EntityFileshareEvent.SHARE_MAIL_ACTION:
            return formatMailShareDetail(props);
        case EntityFileshareEvent.SHARE_LINK_ACTION:
        case EntityFileshareEvent.RECREATE_SHARE_LINK_ACTION:
            return formatLinkShareDetail(props);
        case EntityFileshareEvent.GRANT_SET_EXPIRY_ACTION:
        case EntityFileshareEvent.GRANT_SET_PERMISSIONS_ACTION:
            return getGrantExtraInfo(props);
        case EntityFileshareEvent.RENAME_ACTION:
            return formatRenameExtraInfo(ev, props);
        case EntityFileshareEvent.EXPIRE_ACTION:
            return FileshareMessages.get("audit.expired"); //$NON-NLS-1$
        case EntityFileshareEvent.SET_SECURITY_LABEL_ACTION:
        case EntityFileshareEvent.SET_SECURITY_LABEL_RECURSIVE_ACTION:
            return formatLabelChangeExtraInfo(ev, props);
        case MOVE_AWAY:
            return formatMoveAwayExtraInfo(ev);
        case MOVE_HERE:
            return formatMoveHereExtraInfo(ev);
        case EntityFileshareEvent.SET_MIMETYPE_ACTION:
        case EntityFileshareEvent.SET_EXPIRY_ACTION:
        case EntityFileshareEvent.UNSET_EXPIRY_ACTION:
        case EntityFileshareEvent.SET_ALLOW_FILEOVERRIDE_ACTION:
        case EntityFileshareEvent.UNSET_ALLOW_FILEOVERRIDE_ACTION:
        case EntityFileshareEvent.SET_SENDNOTIFICATIONS_ACTION:
        case EntityFileshareEvent.UNSET_SENDNOTIFICATIONS_ACTION:
            return getAction(localId, ev);
        default:
            return StringUtils.EMPTY;
        }
    }


    /**
     * @param ev
     * @param props
     * @return
     */
    private static String formatLabelChangeExtraInfo ( MapEvent ev, Map<String, Object> props ) {
        return FileshareMessages.format(
            "audit.security-label-change", //$NON-NLS-1$
            ev.get(SingleEntityFileshareEvent.TARGET_SECURITY_LABEL),
            props.get("newLabel")); //$NON-NLS-1$
    }


    /**
     * @param ev
     * @return
     */
    private static String formatMoveHereExtraInfo ( MapEvent ev ) {
        return FileshareMessages.get("audit.move-here-detail"); //$NON-NLS-1$
    }


    /**
     * @param ev
     * @return
     */
    private static String formatMoveAwayExtraInfo ( MapEvent ev ) {
        return FileshareMessages.format(
            "audit.move-away-detail", //$NON-NLS-1$
            ev.get("targetName")); //$NON-NLS-1$
    }


    /**
     * @param ev
     * @param props
     * @return
     */
    private static String formatRenameExtraInfo ( MapEvent ev, Map<String, Object> props ) {
        return FileshareMessages.format(
            "audit.rename-detail", //$NON-NLS-1$
            ev.get(SingleEntityFileshareEvent.TARGET_ENTITY_NAME),
            props.get("newName")); //$NON-NLS-1$
    }


    /**
     * @param props
     * @return
     */
    private static String getGrantExtraInfo ( Map<String, Object> props ) {
        switch ( (String) props.get(BaseFileshareEvent.TYPE) ) {
        case BaseFileshareEvent.TYPE_SUBJECT:
            return formatSubjectShareDetail(props);
        case BaseFileshareEvent.TYPE_MAIL:
            return formatMailShareDetail(props);
        case BaseFileshareEvent.TYPE_LINK:
            return formatLinkShareDetail(props);
        default:
            return StringUtils.EMPTY;
        }
    }


    /**
     * @param props
     * @return
     */
    private static String formatLinkShareDetail ( Map<String, Object> props ) {
        return FileshareMessages.format(
            "audit.link-share-detail", //$NON-NLS-1$
            props.get("identifier"), //$NON-NLS-1$
            formatSharePerms(props));
    }


    @SuppressWarnings ( "unchecked" )
    private static String formatMailShareDetail ( Map<String, Object> props ) {
        List<String> list = (List<String>) props.get("recipients"); //$NON-NLS-1$
        String targetNames;
        if ( list == null ) {
            targetNames = (String) props.get("mailAddress"); //$NON-NLS-1$
        }
        else {
            targetNames = StringUtils.join(list, LIST_SEP);
        }

        return FileshareMessages.format("audit.mail-share-detail", targetNames, formatSharePerms(props)); //$NON-NLS-1$
    }


    /**
     * @param props
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private static String formatSubjectShareDetail ( Map<String, Object> props ) {
        List<String> list = (List<String>) props.get("targetNames"); //$NON-NLS-1$
        String targetNames;
        if ( list == null ) {
            targetNames = (String) props.get("subjectName"); //$NON-NLS-1$
        }
        else {
            targetNames = StringUtils.join(list, LIST_SEP);
        }
        return FileshareMessages.format("audit.subject-share-detail", targetNames, formatSharePerms(props)); //$NON-NLS-1$
    }


    /**
     * @param props
     * @return
     */
    private static String formatSharePerms ( Map<String, Object> props ) {

        Integer permsOrd = (Integer) props.get("newPermissions"); //$NON-NLS-1$

        if ( permsOrd == null ) {
            permsOrd = (Integer) props.get("permissions"); //$NON-NLS-1$
        }

        String permsFormatted = FileshareMessages.get("audit.unknown-perms"); //$NON-NLS-1$
        if ( permsOrd != null ) {
            permsFormatted = SharePermissionsBean.formatPermissions(GrantPermission.fromInt(permsOrd));
        }
        return permsFormatted;
    }


    /**
     * @param ev
     * @return the formatted user information
     */
    @SuppressWarnings ( "unchecked" )
    public static String formatUser ( MapEvent ev ) {
        if ( ev.get(BaseFileshareEvent.PRINCIPAL) != null ) {
            Map<String, Object> princMap = (Map<String, Object>) ev.get(BaseFileshareEvent.PRINCIPAL);
            return String.format(
                "%s@%s", //$NON-NLS-1$
                princMap.get("userName"), //$NON-NLS-1$
                princMap.get("realmName")); //$NON-NLS-1$
        }
        else if ( ev.get(EntityFileshareEvent.TOKEN_GRANT_ID) != null ) {
            return (String) ev.get(EntityFileshareEvent.TOKEN_GRANT_ID);
        }

        return null;
    }


    /**
     * @param ev
     * @return the subject icon
     */
    public static String getSubjectIcon ( MapEvent ev ) {
        if ( ev.get(BaseFileshareEvent.PRINCIPAL) != null ) {
            return SUBJECT_GRANT_ICON;
        }

        String type = (String) ev.get(EntityFileshareEvent.TOKEN_GRANT_TYPE);

        if ( type == null ) {
            return BLANK_ICON;
        }

        switch ( type ) {
        case BaseFileshareEvent.TYPE_SUBJECT:
            return SUBJECT_GRANT_ICON;
        case BaseFileshareEvent.TYPE_MAIL:
            return MAIL_GRANT_ICON;
        case BaseFileshareEvent.TYPE_LINK:
            return LINK_GRANT_ICON;
        default:
            return BLANK_ICON;
        }
    }


    /**
     * @param localId
     * @param ev
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private EntityKey getEntityIdMatch ( EntityKey localId, MapEvent ev ) {

        if ( isSingleEntityEvent(ev) ) {
            if ( ev.get(SingleEntityFileshareEvent.TARGET_ENTITY_ID) != null ) {
                return this.fsp.getEntityService().parseEntityKey((String) ev.get(SingleEntityFileshareEvent.TARGET_ENTITY_ID));
            }
        }
        else if ( isMultiEntityEvent(ev) ) {
            if ( ev.get(MultiEntityFileshareEvent.TARGET_ENTITY_IDS) != null
                    && ( (List<String>) ev.get(MultiEntityFileshareEvent.TARGET_ENTITY_IDS) ).contains(localId.toString()) ) {
                return localId;
            }
        }
        else if ( isMoveEntityEvent(ev) ) {
            if ( ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_IDS) != null
                    && ( (List<String>) ev.get(MoveEntityFileshareEvent.SOURCE_ENTITY_IDS) ).contains(localId.toString()) ) {
                return localId;
            }

            return this.fsp.getEntityService().parseEntityKey((String) ev.get(MoveEntityFileshareEvent.TARGET_ID));
        }

        return null;
    }


    /**
     * @param ev
     * @return
     */
    private static boolean isSingleEntityEvent ( MapEvent ev ) {
        return SingleEntityFileshareEvent.SINGLE_ENTITY_TYPE.equals(ev.getType());
    }


    /**
     * 
     * @param ev
     * @return
     */
    private static boolean isMultiEntityEvent ( MapEvent ev ) {
        return MultiEntityFileshareEvent.MULTI_ENTITY_TYPE.equals(ev.getType());
    }


    /**
     * 
     * @param ev
     * @return
     */
    private static boolean isMoveEntityEvent ( MapEvent ev ) {
        return MoveEntityFileshareEvent.MOVE_TYPE.equals(ev.getType());
    }
}
