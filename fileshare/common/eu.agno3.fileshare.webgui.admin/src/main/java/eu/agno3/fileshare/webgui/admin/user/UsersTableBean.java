/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2014 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


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
import eu.agno3.fileshare.model.SubjectType;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "app_fs_adm_usersTableBean" )
@ViewScoped
public class UsersTableBean implements Serializable {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(UsersTableBean.class);
    private static final long serialVersionUID = -4430859461308151720L;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    private SelectableDataModel<User> model;

    @Inject
    private UserSelectionBean selectionBean;

    @Inject
    private UserTableBinding binding;

    private boolean failed = false;


    /**
     * @return the fsp
     */
    FileshareAdminServiceProvider getFsp () {
        return this.fsp;
    }


    /**
     * @return the exceptionHandler
     */
    FileshareAdminExceptionHandler getExceptionHandler () {
        return this.exceptionHandler;
    }


    /**
     * @return the log
     */
    Logger getLog () {
        return log;
    }


    /**
     * 
     * @return the table model
     */
    public SelectableDataModel<User> getModel () {
        if ( this.model == null && !this.failed ) {
            try {
                this.model = this.createModel();
            }
            catch ( Exception e ) {
                this.exceptionHandler.handleException(e);
                this.failed = true;
                return null;
            }
        }
        return this.model;
    }


    /**
     * 
     * @param ev
     */
    public void init ( ComponentSystemEvent ev ) {
        getModel();
    }


    /**
     * 
     * @param u
     * @return whether the user is a local (non-synchronized user)
     */
    public boolean isLocalUser ( User u ) {
        if ( u == null ) {
            return false;
        }
        return u.getType() == null || u.getType() == SubjectType.LOCAL;
    }


    /**
     * Reload model on next access
     */
    public void refresh () {
        try {
            this.model = createModel();
        }
        catch ( Exception e ) {
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
        this.selectionBean.setMultiSelection((List<@Nullable User>) null);
        this.refresh();
    }


    /**
     * @return
     * @throws FileshareException
     */
    private LazyDataModel<User> createModel () throws FileshareException {
        return new UserDataModel();
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
    public class UserDataModel extends LazyDataModel<User> {

        private static final long serialVersionUID = 1394526598692215381L;


        /**
         * @throws FileshareException
         * 
         */
        public UserDataModel () throws FileshareException {
            long userCount = getFsp().getUserService().getUserCount();
            this.setRowCount((int) userCount);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.util.List, java.util.Map)
         */
        @Override
        public List<User> load ( int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters ) {
            return load(first, pageSize);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder,
         *      java.util.Map)
         */
        @Override
        public List<User> load ( int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters ) {
            return load(first, pageSize);
        }


        /**
         * @param first
         * @param pageSize
         * @return
         */
        private List<User> load ( int first, int pageSize ) {
            try {
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug(String.format("Loading users starting from %d with page size %d", first, pageSize)); //$NON-NLS-1$
                }
                long userCount = getFsp().getUserService().getUserCount();
                this.setRowCount((int) userCount);
                return getFsp().getUserService().listUsers(first, pageSize);
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
        public User getRowData ( String rowKey ) {
            try {
                return getFsp().getUserService().getUser(UUID.fromString(rowKey));
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
        public Object getRowKey ( User object ) {
            if ( object == null ) {
                return null;
            }
            return object.getId();
        }
    }

}
