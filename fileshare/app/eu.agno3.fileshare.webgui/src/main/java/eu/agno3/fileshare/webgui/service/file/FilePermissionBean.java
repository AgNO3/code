/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EmptyDirectoryTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "filePermissionBean" )
public class FilePermissionBean {

    @Inject
    private CurrentUserBean cur;


    /**
     * @param e
     * @return whether the current user is the owner of the entity or member of a the owning group
     */
    public boolean isOwner ( VFSEntity e ) {

        if ( e == null ) {
            return false;
        }

        if ( e.getOwner().equals(this.cur.getCurrentUser()) ) {
            return true;
        }

        if ( ! ( e.getOwner() instanceof Group ) ) {
            return false;
        }

        return this.cur.isMemberOf((Group) e.getOwner());
    }


    /**
     * @param e
     * @return
     */
    private static EntityTreeNode unwrapEntityTreeNode ( BrowseTreeNode e ) {
        if ( e instanceof EntityTreeNode ) {
            return (EntityTreeNode) e;
        }

        if ( e instanceof EmptyDirectoryTreeNode ) {
            return (EntityTreeNode) e.getParent();
        }
        return null;
    }


    /**
     * 
     * @param e
     * @return extra style class based on the elements share status
     */
    public String getShareStyleClass ( VFSEntity e ) {

        if ( !isOwner(e) ) {
            return "notowner"; //$NON-NLS-1$
        }
        else if ( e.hasLocalValidGrants() ) {
            return "shared-local"; //$NON-NLS-1$
        }
        else if ( e.hasGrants() ) {
            return "shared"; //$NON-NLS-1$
        }

        return StringUtils.EMPTY;
    }


    /**
     * 
     * @param e
     * @return whether the user has the BROWSE permission
     */
    public boolean canBrowse ( BrowseTreeNode e ) {
        return e != null && this.hasPermission(e, GrantPermission.BROWSE);
    }


    /**
     * 
     * @param e
     * @return whether the user has the READ permission
     */
    public boolean canRead ( BrowseTreeNode e ) {
        return e != null && this.hasPermission(e, GrantPermission.READ);
    }


    /**
     * 
     * @param e
     * @return whether thte user has the READ permission or is the creator
     */
    public boolean canReadOrIsCreator ( BrowseTreeNode e ) {
        return e != null && this.hasPermission(e, GrantPermission.READ) || this.isCreator(e);
    }


    /**
     * 
     * @param e
     * @return whether the user has the UPLOAD permission
     */
    public boolean canUpload ( BrowseTreeNode e ) {
        return e != null && !isStaticReadOnly(e) && this.hasPermission(e, GrantPermission.UPLOAD);
    }


    /**
     * @param e
     * @return whether the node is marked as read only
     */
    public static boolean isStaticReadOnly ( BrowseTreeNode e ) {
        EntityTreeNode et = unwrapEntityTreeNode(e);
        if ( et == null ) {
            return true;
        }

        return et.getAttachedObject().isStaticReadOnly();
    }


    /**
     * 
     * @param e
     * @return whether the user has the EDIT permission
     */
    public boolean canEdit ( BrowseTreeNode e ) {
        return e != null && !isStaticReadOnly(e) && this.hasPermission(e, GrantPermission.EDIT);
    }


    /**
     * 
     * @param e
     * @return whether the user has the EDIT_SELF permission
     */
    public boolean canEditSelf ( BrowseTreeNode e ) {
        return e != null && !isStaticReadOnly(e) && this.hasPermission(e, GrantPermission.EDIT_SELF);
    }


    /**
     * 
     * @param e
     * @return whether the user has the edit permission or has the edit self permission and is the creator
     */
    public boolean canEditOrEditSelf ( BrowseTreeNode e ) {
        if ( e == null ) {
            return false;
        }
        if ( isStaticReadOnly(e) ) {
            return false;
        }
        if ( this.hasPermission(e, GrantPermission.EDIT) ) {
            return true;
        }
        else if ( this.isCreator(e) && this.hasPermission(e, GrantPermission.EDIT_SELF) ) {
            return true;
        }
        return false;
    }


    /**
     * @param e
     * @param g
     * @return whether the user has the edit permission or has the edit self permission and is the creator
     */
    public boolean canEditOrEditSelfEntity ( VFSEntity e, Grant g ) {

        if ( e == null ) {
            return false;
        }

        if ( e.isStaticReadOnly() ) {
            return false;
        }

        if ( g == null ) {
            return isOwner(e);
        }

        if ( g.getPermissions().contains(GrantPermission.EDIT) ) {
            return true;
        }

        if ( g.getPermissions().contains(GrantPermission.EDIT_SELF) && e.getCreatorGrant() != null && e.getCreatorGrant().equals(g) ) {
            if ( g instanceof TokenGrant && ! ( g instanceof MailGrant ) ) {
                return false;
            }
            return true;
        }

        return false;
    }


    /**
     * 
     * @param e
     * @param perm
     * @return whether the user has a permission
     */
    public boolean hasPermission ( BrowseTreeNode e, GrantPermission perm ) {
        EntityTreeNode et = unwrapEntityTreeNode(e);
        if ( et == null ) {
            return false;
        }

        if ( isOwner(et.getAttachedObject()) ) {
            return true;
        }

        return e.hasPermission(perm);
    }


    /**
     * @param e
     * @return whether the user is the creator of the node
     */
    public boolean isCreator ( BrowseTreeNode e ) {
        EntityTreeNode et = unwrapEntityTreeNode(e);
        if ( et == null || et.getAttachedObject() == null ) {
            return false;
        }

        if ( et.getAttachedObject().getCreator() != null && et.getAttachedObject().getCreator().equals(this.cur.getCurrentUser()) ) {
            return true;
        }
        else if ( et.getGrant() != null && ( et.getGrant() instanceof TokenGrant && ! ( et.getGrant() instanceof MailGrant ) ) ) {
            // this is a anonymous grant that might be shared by multiple users
            return false;
        }
        else if ( et.getAttachedObject().getCreatorGrant() != null ) {

            Grant userGrant = et.getGrant();
            Grant creatorGrant = et.getAttachedObject().getCreatorGrant();

            if ( creatorGrant.equals(userGrant) ) {
                return true;
            }
        }
        return false;

    }


    /**
     * @param e
     * @return whether the current user can overwrite the file / or files in the directory
     */
    public boolean canOverwrite ( BrowseTreeNode e ) {
        EntityTreeNode et = unwrapEntityTreeNode(e);
        if ( et == null ) {
            return false;
        }

        VFSEntity entity = et.getAttachedObject();
        if ( entity instanceof VFSContainerEntity ) {
            // this will enable the option in the UI but might ultimately revert to creating a new file if the user has
            // only upload permissions and is not creator of the conflict file
            return ( (VFSContainerEntity) entity ).getAllowFileOverwrite();
        }

        if ( ! ( entity instanceof VFSFileEntity ) ) {
            return false;
        }

        if ( ! ( (VFSFileEntity) entity ).canReplace() ) {
            return false;
        }

        return this.canEditOrEditSelf(e);
    }

}
