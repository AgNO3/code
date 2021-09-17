/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "sharePermissionsBean" )
public class SharePermissionsBean {

    private static final List<Integer> DIR_PERMISSION_OPTIONS = Arrays.asList(4, 3, 15, 31);
    private static final List<Integer> FILE_PERMISSION_OPTIONS = Arrays.asList(3, 31);
    private static final List<Integer> READ_ONLY_PERMISSION_OPTIONS = Arrays.asList(3);


    /**
     * 
     * @param perms
     * @return whether the permissions contain a write permission
     */
    public static boolean containsWrite ( int perms ) {
        Set<GrantPermission> fromInt = GrantPermission.fromInt(perms);
        return fromInt.contains(GrantPermission.EDIT) || fromInt.contains(GrantPermission.EDIT_SELF) || fromInt.contains(GrantPermission.UPLOAD);
    }


    /**
     * 
     * @param target
     * @return permission options for the given target
     */
    public static List<SelectItem> getPermissionOptions ( VFSEntity target ) {

        if ( target != null && target.isStaticReadOnly() ) {
            return getReadOnlyPermissionOptions();
        }

        if ( target instanceof VFSFileEntity ) {
            return getFilePermissionOptions();
        }
        else if ( target instanceof VFSContainerEntity ) {
            return getDirectoryPermissionOptions();
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * @return
     */
    private static List<SelectItem> getReadOnlyPermissionOptions () {
        List<SelectItem> items = new ArrayList<>();

        for ( int perm : READ_ONLY_PERMISSION_OPTIONS ) {
            items.add(new SelectItem(perm, getPermLabel(perm), getPermDirectoryDescription(perm)));
        }
        return items;
    }


    /**
     * 
     * @return usable directory permissions
     */
    public static List<SelectItem> getDirectoryPermissionOptions () {
        List<SelectItem> items = new ArrayList<>();

        for ( int perm : DIR_PERMISSION_OPTIONS ) {
            items.add(new SelectItem(perm, getPermLabel(perm), getPermDirectoryDescription(perm)));
        }
        return items;
    }


    /**
     * 
     * @return usable file permissions
     */
    public static List<SelectItem> getFilePermissionOptions () {
        List<SelectItem> items = new ArrayList<>();

        for ( int perm : FILE_PERMISSION_OPTIONS ) {
            items.add(new SelectItem(perm, getPermLabel(perm), getPermFileDescription(perm)));
        }

        return items;
    }


    /**
     * @param perm
     * @return
     */
    private static String getPermDirectoryDescription ( int perm ) {
        return FileshareMessages.get(String.format("perm.%d.desc.dir", perm)); //$NON-NLS-1$
    }


    /**
     * @param perm
     * @return
     */
    private static String getPermFileDescription ( int perm ) {
        return FileshareMessages.get(String.format("perm.%d.desc.file", perm)); //$NON-NLS-1$
    }


    /**
     * @param perm
     * @return the permission label
     */
    public static String getPermLabel ( int perm ) {
        return FileshareMessages.get(String.format("perm.%d.label", perm)); //$NON-NLS-1$
    }


    /**
     * 
     * @param perms
     * @return implied permission labels
     */
    public static List<String> getImpliedPermissions ( int perms ) {
        List<String> res = new ArrayList<>();

        int matched = 0;

        for ( int i = DIR_PERMISSION_OPTIONS.size() - 1; i >= 0; i-- ) {
            int permOption = DIR_PERMISSION_OPTIONS.get(i);
            if ( ( permOption & matched ) != permOption && ( permOption & perms ) == permOption ) {
                matched |= permOption;
                res.add(getPermLabel(permOption));
            }
        }

        return res;
    }


    /**
     * 
     * @param perms
     * @return the closest implied permission level
     */
    public static int getBestMatch ( int perms ) {
        for ( int i = DIR_PERMISSION_OPTIONS.size() - 1; i >= 0; i-- ) {
            int permOption = DIR_PERMISSION_OPTIONS.get(i);
            if ( ( permOption & perms ) == permOption ) {
                return permOption;
            }
        }

        return 0;
    }


    /**
     * 
     * @param permissions
     * @return a formatted permissions string
     */
    public static String formatPermissions ( Set<GrantPermission> permissions ) {
        int code = 0;
        if ( permissions != null && !permissions.isEmpty() ) {
            code = GrantPermission.toInt(permissions);
        }

        return FileshareMessages.get(String.format("perm.%d.label", code)); //$NON-NLS-1$
    }

}
