/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.webgui.service.tree.EmptyDirectoryTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.GroupTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "fileSelectionBean" )
public class FileSelectionBean implements Serializable {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(FileSelectionBean.class);
    private static final long serialVersionUID = 5205073861225821236L;

    private TreeNode[] selection;

    @Inject
    private FileRootSelectionBean fileTree;

    private Long cachedSelectionSize;


    /**
     * 
     * @return whether no entity is selected
     */
    public synchronized boolean isEmptySelection () {
        return this.selection == null || this.selection.length == 0;
    }


    /**
     * 
     * @return whether a single entity is selected
     */
    public synchronized boolean isSingleSelection () {
        return this.selection != null && this.selection.length == 1;
    }


    /**
     * 
     * @return whether an empty dir entry is selected
     */
    public synchronized boolean isEmptyDirSelection () {
        return this.selection != null && this.selection.length == 1 && this.selection[ 0 ] instanceof EmptyDirectoryTreeNode;
    }


    /**
     * @return whether the group root is selected
     */
    public synchronized boolean isGroupRootSelected () {
        return this.selection != null && this.selection.length == 1 && this.selection[ 0 ] instanceof GroupTreeNode;
    }


    /**
     * 
     * @return whether all selected items are subjects
     */
    public synchronized boolean isAllSubjects () {
        if ( this.selection == null && this.selection.length == 0 ) {
            return true;
        }

        for ( TreeNode n : this.selection ) {
            if ( !FileDisplayBean.isSubjectNode(n) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * 
     * @return whether all selected items are subjects
     */
    public synchronized boolean isAllPeers () {
        if ( this.selection == null && this.selection.length == 0 ) {
            return true;
        }

        for ( TreeNode n : this.selection ) {
            if ( !FileDisplayBean.isPeerNode(n) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * 
     * @return whether all selected items are subjects
     */
    public synchronized boolean containsSubjects () {
        if ( this.selection == null && this.selection.length == 0 ) {
            return false;
        }

        for ( TreeNode n : this.selection ) {
            if ( FileDisplayBean.isSubjectNode(n) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * 
     * @return whether all selected items are entities
     */
    public synchronized boolean isAllEntities () {
        if ( this.selection == null || this.selection.length == 0 ) {
            return true;
        }

        for ( TreeNode n : this.selection ) {
            if ( !FileDisplayBean.isFileNode(n) && !FileDisplayBean.isDirectoryNode(n) ) {
                return false;
            }

            EntityTreeNode en = (EntityTreeNode) n;
            if ( StringUtils.isBlank(en.getAttachedObject().getLocalName()) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @return whether multiple entities are selected
     */
    public synchronized boolean isMultiSelection () {
        return this.selection != null && this.selection.length > 1;
    }


    /**
     * @return the selected entities
     */
    public synchronized Set<VFSEntity> getSelectedEntities () {
        Set<VFSEntity> entities = new HashSet<>();
        if ( this.selection == null ) {
            return entities;
        }

        for ( TreeNode n : this.selection ) {
            if ( n instanceof EntityTreeNode ) {
                EntityTreeNode tn = (EntityTreeNode) n;
                entities.add(tn.getAttachedObject());
            }
        }

        return entities;
    }


    /**
     * 
     * @return the selected subjects
     */
    public synchronized Set<SubjectInfo> getSelectedSubjects () {
        Set<SubjectInfo> subjects = new HashSet<>();
        if ( this.selection == null ) {
            return subjects;
        }

        for ( TreeNode n : this.selection ) {
            if ( FileDisplayBean.isSubjectNode(n) ) {
                subjects.add(FileDisplayBean.getSubject(n));
            }
        }

        return subjects;
    }


    /**
     * 
     * @return the selected subjects
     */
    public synchronized Set<PeerInfo> getSelectedPeers () {
        Set<PeerInfo> peers = new HashSet<>();
        if ( this.selection == null ) {
            return peers;
        }

        for ( TreeNode n : this.selection ) {
            if ( FileDisplayBean.isPeerNode(n) ) {
                peers.add(FileDisplayBean.getPeer(n));
            }
        }

        return peers;
    }


    /**
     * 
     * @return the content entity if a single one is selected
     */
    public synchronized VFSEntity getSingleSelectionEntity () {
        if ( !this.isSingleSelection() ) {
            return null;
        }
        TreeNode n = this.selection[ 0 ];

        if ( ! ( n instanceof EntityTreeNode ) ) {
            return null;
        }

        return ( (EntityTreeNode) n ).getAttachedObject();
    }


    /**
     * @return the grant for the selected entity
     */
    public synchronized Grant getSingleSelectionGrant () {
        if ( !this.isSingleSelection() && !this.isMultiSelection() ) {
            return null;
        }
        TreeNode n = this.selection[ 0 ];

        if ( ! ( n instanceof EntityTreeNode ) ) {
            return null;
        }

        return ( (EntityTreeNode) n ).getGrant();
    }


    /**
     * @return the grant for the selected entity
     */
    public synchronized UUID getSingleSelectionGrantId () {

        Grant g = getSingleSelectionGrant();

        if ( g != null ) {
            return g.getId();
        }

        return null;
    }


    /**
     * 
     * @return the selected tree nodes
     */
    public synchronized TreeNode[] getSelection () {
        return this.selection;
    }


    /**
     * 
     * @return the selected tree nodes
     */
    public synchronized TreeNode getFirstSelection () {
        if ( this.selection == null || this.selection.length < 1 ) {
            return null;
        }

        if ( this.selection[ 0 ] instanceof EmptyDirectoryTreeNode ) {
            return this.selection[ 0 ].getParent();
        }

        return this.selection[ 0 ];
    }


    /**
     * @param selection
     *            the selection to set
     */
    public synchronized void setSelection ( TreeNode[] selection ) {
        this.selection = selection;
        this.cachedSelectionSize = null;
    }


    /**
     * @return the directory in which the directory will be created
     */
    public synchronized VFSContainerEntity getSingleSelectionContainer () {

        if ( this.getSelection() == null ) {
            return this.fileTree.getRootContainer();
        }
        else if ( this.isSingleSelection() ) {
            VFSEntity e = this.getSingleSelectionEntity();
            if ( e instanceof VFSContainerEntity ) {
                return (VFSContainerEntity) e;
            }
        }

        return null;
    }


    /**
     * @return the total file size
     */
    public long getSelectionTotalSize () {
        if ( this.getSelection() == null ) {
            return 0L;
        }

        if ( this.cachedSelectionSize != null ) {
            return this.cachedSelectionSize;
        }

        long totalSize = 0L;

        Set<TreeNode> sel = new HashSet<>(Arrays.asList(getSelection()));
        for ( TreeNode e : sel ) {
            if ( sel.contains(e.getParent()) ) {
                continue;
            }

            if ( ! ( e instanceof EntityTreeNode ) ) {
                continue;
            }

            VFSEntity en = ( (EntityTreeNode) e ).getAttachedObject();
            if ( en instanceof VFSFileEntity ) {
                totalSize += ( (VFSFileEntity) en ).getFileSize();
            }
            else if ( en instanceof VFSContainerEntity ) {
                VFSContainerEntity c = (VFSContainerEntity) en;
                if ( c.getChildrenSize() != null ) {
                    totalSize += c.getChildrenSize();
                }
            }
        }

        this.cachedSelectionSize = totalSize;
        return totalSize;
    }


    /**
     * 
     */
    public synchronized void clear () {
        log.debug("Clearing selection"); //$NON-NLS-1$
        this.selection = null;
        this.cachedSelectionSize = null;
    }


    /**
     * 
     * @return null
     */
    public synchronized String clearSelection () {
        clear();
        return null;
    }

}
