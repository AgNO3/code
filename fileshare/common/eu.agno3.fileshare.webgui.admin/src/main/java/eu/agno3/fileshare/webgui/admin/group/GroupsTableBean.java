/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2014 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "app_fs_adm_groupsTableBean" )
@ViewScoped
public class GroupsTableBean implements Serializable {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(GroupsTableBean.class);
    private static final long serialVersionUID = -4430859461308151720L;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    @Inject
    private GroupTableBinding binding;

    @Inject
    private GroupSelectionBean selectionBean;

    private SelectableDataModel<Group> model;

    private boolean failed = false;


    /**
     * @return the fsp
     */
    FileshareAdminServiceProvider getFsp () {
        return this.fsp;
    }


    /**
     * 
     * @param ev
     */
    public void init ( ComponentSystemEvent ev ) {
        getModel();
    }


    /**
     * @return the exceptionHandler
     */
    FileshareAdminExceptionHandler getExceptionHandler () {
        return this.exceptionHandler;
    }


    /**
     * @return
     * 
     */
    Logger getLog () {
        return log;
    }


    /**
     * 
     * @return the table model
     */
    public SelectableDataModel<Group> getModel () {
        if ( this.model == null && !this.failed ) {
            try {
                this.model = this.createModel();
            }
            catch ( FileshareException e ) {
                this.failed = true;
                this.exceptionHandler.handleException(e);
                return null;
            }
        }
        return this.model;
    }


    /**
     * Reload model on next access
     */
    public void refresh () {
        try {
            this.model = createModel();
        }
        catch ( FileshareException e ) {
            this.exceptionHandler.handleException(e);
            return;
        }
        DataTable component = this.binding.getComponent();
        if ( component != null ) {
            log.debug("Refreshing data table"); //$NON-NLS-1$
            component.clearLazyCache();
            component.setSelection(this.selectionBean.getMultiSelection());
            component.setValue(this.model);
        }
    }


    /**
     * Reload model on next access
     */
    public void refreshAndClearSelection () {
        this.selectionBean.setSingleSelection(null);
        this.selectionBean.setMultiSelection((List<@Nullable Group>) null);
        this.refresh();
    }


    /**
     * @return
     * @throws FileshareException
     */
    private LazyDataModel<Group> createModel () throws FileshareException {
        return new GroupDataModel();
    }


    /**
     * @param ev
     */
    public void onSelect ( SelectEvent ev ) {
        // nothing
    }


    /**
     * @param ev
     */
    public void onUnselect ( UnselectEvent ev ) {
        // nothing
    }

    /**
     * @author mbechler
     *
     */
    public class GroupDataModel extends LazyDataModel<Group> {

        private static final long serialVersionUID = 1394526598692215381L;


        /**
         * @throws FileshareException
         * 
         */
        public GroupDataModel () throws FileshareException {
            long userCount = getFsp().getUserService().getUserCount();
            this.setRowCount((int) userCount);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.util.List, java.util.Map)
         */
        @Override
        public List<Group> load ( int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters ) {
            return load(first, pageSize);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder,
         *      java.util.Map)
         */
        @Override
        public List<Group> load ( int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters ) {
            return load(first, pageSize);
        }


        /**
         * @param first
         * @param pageSize
         * @return
         */
        private List<Group> load ( int first, int pageSize ) {
            try {
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug(String.format("Loading groups starting from %d with page size %d", first, pageSize)); //$NON-NLS-1$
                }
                long userCount = getFsp().getGroupService().getGroupCount();
                this.setRowCount((int) userCount);
                if ( userCount == 0 ) {
                    return Collections.EMPTY_LIST;
                }
                return getFsp().getGroupService().listGroups(first, pageSize);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                getExceptionHandler().handleException(e);
                return Collections.EMPTY_LIST;
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#getRowData(java.lang.String)
         */
        @Override
        public Group getRowData ( String rowKey ) {
            try {
                return getFsp().getGroupService().getGroup(UUID.fromString(rowKey));
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                getExceptionHandler().handleException(e);
                return null;
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#getRowKey(java.lang.Object)
         */
        @Override
        public Object getRowKey ( Group object ) {
            if ( object == null ) {
                return null;
            }
            return object.getId();
        }
    }
}
