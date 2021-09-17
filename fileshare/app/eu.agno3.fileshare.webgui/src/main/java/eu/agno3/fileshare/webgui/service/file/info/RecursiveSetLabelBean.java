/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2016 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "recursiveSetLabelBean" )
public class RecursiveSetLabelBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4029565168313381625L;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    FileshareServiceProvider fsp;

    private String label;

    private boolean inconsitenciesLoaded;
    private TreeNode[] confirmed;

    private CheckboxTreeNode model;

    private int numHigher;


    /**
     * @return the label
     */
    public String getLabel () {
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    public void setLabel ( String label ) {
        this.label = label;
    }


    /**
     * @return the confirmed
     */
    public TreeNode[] getConfirmed () {
        load();
        return this.confirmed;
    }


    /**
     * @param confirmed
     *            the confirmed to set
     */
    public void setConfirmed ( TreeNode[] confirmed ) {
        this.confirmed = confirmed;
    }


    /**
     * @return the model
     */
    public CheckboxTreeNode getModel () {
        load();
        return this.model;
    }


    /**
     * @return the numHigher
     */
    public int getNumHigher () {
        load();
        return this.numHigher;
    }


    /**
     * 
     * @return null
     */
    public String selectAll () {
        List<TreeNode> all = new LinkedList<>();
        select(this.getModel(), all);
        this.confirmed = all.toArray(new TreeNode[all.size()]);
        return null;
    }


    /**
     * @param children
     * @param all
     */
    private void select ( TreeNode n, List<TreeNode> all ) {
        if ( n.isSelectable() ) {
            n.setSelected(true);
            all.add(n);
        }
        for ( TreeNode c : n.getChildren() ) {
            select(c, all);
        }
    }


    /**
     * @return null
     */
    public String selectNone () {
        TreeNode[] cf = this.confirmed;
        if ( cf != null ) {
            for ( TreeNode tn : cf ) {
                tn.setSelected(false);
            }
            this.confirmed = null;
        }
        return null;
    }


    /**
     * 
     */
    void load () {
        if ( !this.inconsitenciesLoaded ) {
            this.inconsitenciesLoaded = true;
            @Nullable
            VFSEntity root = this.fileSelection.getSingleSelection();

            if ( root == null ) {
                return;
            }

            try {
                List<VFSEntity> inconsistencies = this.fsp.getEntityService().getChildrenSecurityLabels(this.fileSelection.getSingleSelectionId());
                SecurityLabel oldLabel = root.getSecurityLabel();
                this.numHigher = 0;
                for ( VFSEntity e : inconsistencies ) {
                    int res = this.fsp.getConfigurationProvider().getSecurityPolicyConfiguration().compareLabels(oldLabel, e.getSecurityLabel());
                    if ( res < 0 ) {
                        this.numHigher++;
                    }
                }
                this.model = buildTreeModel(inconsistencies);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }
    }


    /**
     * @param wrappedData
     * @return
     * @throws FileshareException
     */
    private CheckboxTreeNode buildTreeModel ( List<VFSEntity> wrappedData ) throws FileshareException {
        SelectionTreeNode root = new SelectionTreeNode();
        root.setSelectable(false);
        root.setExpanded(true);
        Queue<VFSEntity> queue = new LinkedList<>(wrappedData);
        addNodes(root, queue, null);
        return root;
    }


    /**
     * @param parent
     * @param iterator
     * @param object
     * @throws FileshareException
     */
    private List<String> addNodes ( SelectionTreeNode parent, Queue<VFSEntity> queue, String[] parentPath ) throws FileshareException {

        String[] lastPath = null;
        SelectionTreeNode lastNode = null;
        List<String> nextPath = null;

        while ( !queue.isEmpty() ) {

            VFSEntity next = queue.peek();
            List<String> fullPath = nextPath != null ? nextPath : this.fsp.getEntityService().getFullPath(next.getEntityKey());
            nextPath = null;

            if ( parentPath != null && !startsWith(fullPath, parentPath) ) {
                return fullPath;
            }

            if ( lastPath != null && startsWith(fullPath, lastPath) ) {
                nextPath = addNodes(lastNode, queue, lastPath);
                continue;
            }

            next = queue.poll();

            SelectionTreeNode node = new SelectionTreeNode(next, parent);
            node.setRowKey(next.getEntityKey().toString());
            if ( parentPath != null ) {
                node.setPath(StringUtils.join(fullPath.subList(parentPath.length, fullPath.size()), '/'));
            }
            else {
                node.setPath(next.getLocalName());
            }

            @Nullable
            VFSEntity root = this.fileSelection.getSingleSelection();
            if ( root != null && parentPath == null && next instanceof VFSFileEntity
                    && Objects.equals(root.getSecurityLabel(), next.getSecurityLabel()) ) {
                node.setSelected(true);
            }

            node.setExpanded(true);
            lastPath = fullPath.toArray(new String[fullPath.size()]);
            lastNode = node;
        }
        return null;
    }


    /**
     * @param fullPath
     * @param parentPath
     * @return
     */
    private static boolean startsWith ( List<String> fullPath, String[] parentPath ) {
        if ( parentPath == null ) {
            return false;
        }
        for ( int i = 0; i < parentPath.length; i++ ) {
            if ( !fullPath.get(i).equals(parentPath[ i ]) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * 
     * @return dialog close, null on error
     */
    public String confirm () {
        try {
            Set<EntityKey> confirmLower = new HashSet<>();
            confirmLower.add(this.fileSelection.getSingleSelectionId());
            TreeNode[] cf = this.confirmed;

            if ( cf != null ) {
                for ( TreeNode c : cf ) {
                    if ( c.getData() instanceof VFSEntity ) {
                        confirmLower.add( ( (VFSEntity) c.getData() ).getEntityKey());
                    }
                }
            }

            System.out.println(confirmLower);

            this.fsp.getEntityService().setSecurityLabelRecursive(this.fileSelection.getSingleSelectionId(), getLabel(), false, confirmLower);
            return DialogContext.closeDialog(true);
        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
        }
        return null;
    }

    /**
     * @author mbechler
     *
     */
    public class EntitySelectionDataModel extends DataModel<VFSEntity> implements SelectableDataModel<VFSEntity>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -5531740202421495217L;
        private final Map<EntityKey, VFSEntity> index = new LinkedHashMap<>();
        private final List<VFSEntity> entities;
        private int rowIndex;


        /**
         * @param entities
         */
        public EntitySelectionDataModel ( List<VFSEntity> entities ) {
            this.entities = entities;
            for ( VFSEntity vf : entities ) {
                this.index.put(vf.getEntityKey(), vf);
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.faces.model.ListDataModel#getRowCount()
         */
        @Override
        public int getRowCount () {
            return this.index.size();
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.SelectableDataModel#getRowData(java.lang.String)
         */
        @Override
        public VFSEntity getRowData ( String key ) {
            EntityKey ek = RecursiveSetLabelBean.this.fsp.getEntityService().parseEntityKey(key);
            VFSEntity e = this.index.get(ek);

            return e;
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.SelectableDataModel#getRowKey(java.lang.Object)
         */
        @Override
        public Object getRowKey ( VFSEntity e ) {
            String string = e.getEntityKey().toString();
            return string;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.faces.model.DataModel#getRowData()
         */
        @Override
        public VFSEntity getRowData () {
            return this.entities.get(getRowIndex());
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.faces.model.DataModel#getRowIndex()
         */
        @Override
        public int getRowIndex () {
            return this.rowIndex;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.faces.model.DataModel#getWrappedData()
         */
        @Override
        public List<VFSEntity> getWrappedData () {
            return this.entities;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.faces.model.DataModel#isRowAvailable()
         */
        @Override
        public boolean isRowAvailable () {
            return this.rowIndex < this.entities.size();
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.faces.model.DataModel#setRowIndex(int)
         */
        @Override
        public void setRowIndex ( int idx ) {
            this.rowIndex = idx;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.faces.model.DataModel#setWrappedData(java.lang.Object)
         */
        @Override
        public void setWrappedData ( Object arg0 ) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * @author mbechler
     *
     */
    public class SelectionTreeNode extends CheckboxTreeNode {

        private String path;


        /**
         * 
         */
        public SelectionTreeNode () {
            super();
        }


        /**
         * @param data
         * @param parent
         */
        public SelectionTreeNode ( VFSEntity data, TreeNode parent ) {
            super(data, parent);
        }


        /**
         * @param data
         */
        public SelectionTreeNode ( VFSEntity data ) {
            super(data);
        }


        /**
         * @param type
         * @param data
         * @param parent
         */
        public SelectionTreeNode ( String type, VFSEntity data, TreeNode parent ) {
            super(type, data, parent);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.CheckboxTreeNode#getData()
         */
        @Override
        public VFSEntity getData () {
            return (VFSEntity) super.getData();
        }


        /**
         * @return the path
         */
        public String getPath () {
            return this.path;
        }


        /**
         * @param path
         *            the path to set
         */
        public void setPath ( String path ) {
            this.path = path;
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.CheckboxTreeNode#propagateSelectionDown(boolean)
         */
        @Override
        protected void propagateSelectionDown ( boolean value ) {
            // dont
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.CheckboxTreeNode#propagateSelectionUp()
         */
        @Override
        protected void propagateSelectionUp () {
            // dont
        }

        /**
         * 
         */
        private static final long serialVersionUID = 1239381042545398307L;

    }

}
