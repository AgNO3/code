/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.compare;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.util.GrantComparator;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ui.FileTreeComponentBinding;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "fileTableSortingHelper" )
public class FileTableSortingHelper {

    @Inject
    private FileTreeComponentBinding fileTable;


    private int getSortOrder () {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String clientId = this.fileTable.getComponent().getClientId(ctx);
        String sortDirParam = ctx.getExternalContext().getRequestParameterMap().get(clientId + "_sortDir"); //$NON-NLS-1$

        if ( "DESCENDING".equals(sortDirParam) ) { //$NON-NLS-1$
            return -1;
        }
        return 1;
    }


    /**
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByOwner ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }
        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;
        Subject asubj = na.getAttachedObject().getOwner();
        Subject bsubj = nb.getAttachedObject().getOwner();

        return FileSortHelpers.sortBySubject(asubj, bsubj);

    }


    /**
     * 
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByCreated ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }

        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;

        DateTime aCreated = na.getAttachedObject().getCreated();
        DateTime bCreated = nb.getAttachedObject().getCreated();

        return DateTimeComparator.getInstance().compare(aCreated, bCreated);
    }


    /**
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByCreator ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }

        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;
        User auser = na.getAttachedObject().getCreator();
        Grant agrant = na.getAttachedObject().getCreatorGrant();
        User buser = nb.getAttachedObject().getCreator();
        Grant bgrant = nb.getAttachedObject().getCreatorGrant();
        return sortByUserOrGrant(auser, agrant, buser, bgrant);
    }


    /**
     * 
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByLastModified ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }

        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;

        DateTime aLastMod = na.getAttachedObject().getLastModified();
        DateTime bLastMod = nb.getAttachedObject().getLastModified();

        return DateTimeComparator.getInstance().compare(aLastMod, bLastMod);
    }


    /**
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByLastModifier ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }

        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;
        User auser = na.getAttachedObject().getLastModifier();
        Grant agrant = na.getAttachedObject().getLastModifiedGrant();
        User buser = nb.getAttachedObject().getLastModifier();
        Grant bgrant = nb.getAttachedObject().getLastModifiedGrant();
        return sortByUserOrGrant(auser, agrant, buser, bgrant);
    }


    /**
     * @param auser
     * @param agrant
     * @param buser
     * @param bgrant
     * @return
     */
    private static Integer sortByUserOrGrant ( User auser, Grant agrant, User buser, Grant bgrant ) {

        if ( auser != null && buser != null ) {
            return FileSortHelpers.sortBySubject(auser, buser);
        }
        else if ( auser != null ) {
            return -1;
        }
        else if ( buser != null ) {
            return 1;
        }

        return GrantComparator.compareStatic(agrant, bgrant);
    }


    /**
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByFileName ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }
        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;
        VFSEntity aobj = na.getAttachedObject();
        VFSEntity bobj = nb.getAttachedObject();

        if ( aobj instanceof VFSContainerEntity && bobj instanceof VFSContainerEntity ) {}
        else if ( aobj instanceof VFSContainerEntity ) {
            return -1 * this.getSortOrder();
        }
        else if ( bobj instanceof VFSContainerEntity ) {
            return 1 * this.getSortOrder();
        }

        return FileSortHelpers.sortByFileNameInternal(aobj, bobj);

    }


    /**
     * @return
     */
    protected Integer sortByVirtualNodes () {
        return 0;
    }


    /**
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByFileType ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }

        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;
        VFSEntity aobj = na.getAttachedObject();
        VFSEntity bobj = nb.getAttachedObject();

        if ( ! ( aobj instanceof VFSFileEntity ) && ! ( bobj instanceof VFSFileEntity ) ) {
            return 0;
        }
        else if ( ! ( aobj instanceof VFSFileEntity ) ) {
            return -1 * getSortOrder();
        }
        else if ( ! ( bobj instanceof VFSFileEntity ) ) {
            return 1 * getSortOrder();
        }

        return ( (VFSFileEntity) aobj ).getContentType().compareTo( ( (VFSFileEntity) bobj ).getContentType());

    }


    /**
     * @param a
     * @param b
     * @return compare result
     */
    public Integer sortByFileSize ( Object a, Object b ) {
        if ( ! ( a instanceof EntityTreeNode ) || ! ( b instanceof EntityTreeNode ) ) {
            return sortByVirtualNodes();
        }

        EntityTreeNode na = (EntityTreeNode) a;
        EntityTreeNode nb = (EntityTreeNode) b;
        VFSEntity aobj = na.getAttachedObject();
        VFSEntity bobj = nb.getAttachedObject();

        if ( ! ( aobj instanceof VFSFileEntity ) && ! ( bobj instanceof VFSFileEntity ) ) {
            return 0;
        }
        else if ( ! ( aobj instanceof VFSFileEntity ) ) {
            return -1 * getSortOrder();
        }
        else if ( ! ( bobj instanceof VFSFileEntity ) ) {
            return 1 * getSortOrder();
        }

        return Long.compare( ( (VFSFileEntity) aobj ).getFileSize(), ( (VFSFileEntity) bobj ).getFileSize());

    }

}
