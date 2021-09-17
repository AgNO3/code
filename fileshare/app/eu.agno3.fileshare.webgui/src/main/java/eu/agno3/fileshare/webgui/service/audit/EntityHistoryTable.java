/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.audit;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import eu.agno3.fileshare.model.audit.EntityFileshareEvent;
import eu.agno3.fileshare.model.audit.MoveEntityFileshareEvent;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "entityHistoryTable" )
public class EntityHistoryTable implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3302512641784005505L;

    @Inject
    FileshareServiceProvider fsp;

    @Inject
    URLFileSelectionBean fileSelection;

    private LazyLoadAuditDataModel model;

    private String filter = "content"; //$NON-NLS-1$

    Set<String> filterActions = FILTER_ACTIONS.get(this.filter);

    private int pageSize = 12;

    private int retentionTime = -1;

    private static final Map<String, Set<String>> FILTER_ACTIONS = new HashMap<>();

    static {
        FILTER_ACTIONS.put(
            "content", //$NON-NLS-1$
            new HashSet<>(
                Arrays.asList(
                    EntityFileshareEvent.CREATE_ACTION,
                    EntityFileshareEvent.CREATE_FOLDER_ACTION,
                    EntityFileshareEvent.CREATE_OR_REPLACE_ACTION,
                    EntityFileshareEvent.REPLACE_ACTION,
                    EntityFileshareEvent.DOWNLOAD_ACTION,
                    EntityFileshareEvent.DOWNLOAD_MULTI_ACTION,
                    EntityFileshareEvent.DOWNLOAD_FOLDER_ACTION,
                    MoveEntityFileshareEvent.MOVE_ACTION)));

        FILTER_ACTIONS.put(
            "share", //$NON-NLS-1$
            new HashSet<>(
                Arrays.asList(
                    EntityFileshareEvent.SHARE_LINK_ACTION,
                    EntityFileshareEvent.SHARE_SUBJECT_ACTION,
                    EntityFileshareEvent.SHARE_MAIL_ACTION,
                    EntityFileshareEvent.GRANT_SET_EXPIRY_ACTION,
                    EntityFileshareEvent.GRANT_SET_PERMISSIONS_ACTION,
                    EntityFileshareEvent.RECREATE_SHARE_LINK_ACTION,
                    EntityFileshareEvent.GRANT_EXPIRE_ACTION,
                    EntityFileshareEvent.REVOKE_GRANT_ACTION)));
    }


    /**
     * @return the model
     */
    public LazyLoadAuditDataModel getModel () {
        if ( this.model == null ) {
            this.model = makeModel();
        }
        return this.model;
    }


    /**
     * @return the retentionTime
     */
    public int getRetentionTime () {
        if ( this.retentionTime < 0 ) {
            this.retentionTime = this.fsp.getAuditReaderService().getRetentionTimeDays();
        }
        return this.retentionTime;
    }


    /**
     * @return the filter
     */
    public String getFilter () {
        return this.filter;
    }


    /**
     * @return the pageSize
     */
    public int getPageSize () {
        return this.pageSize;
    }


    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter ( String filter ) {
        this.filter = filter;
        this.filterActions = FILTER_ACTIONS.get(filter);
    }


    /**
     * 
     */
    public void refresh () {
        this.model = makeModel();
    }


    /**
     * @return
     */
    private LazyLoadAuditDataModel makeModel () {
        return new LazyLoadAuditDataModel();
    }

    private class LazyLoadAuditDataModel extends LazyDataModel<MapEvent> {

        /**
         * 
         */
        private static final long serialVersionUID = -5399284099602325183L;


        /**
         * 
         */
        public LazyLoadAuditDataModel () {
            try {
                int entityEventCount = (int) EntityHistoryTable.this.fsp.getAuditReaderService().getEntityEventCount(
                    EntityHistoryTable.this.fileSelection.getSingleSelectionId(),
                    null,
                    null,
                    EntityHistoryTable.this.filterActions);
                this.setRowCount(entityEventCount);
            }
            catch ( Exception e ) {
                ExceptionHandler.handleException(e);
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.util.List, java.util.Map)
         */
        @Override
        public List<MapEvent> load ( int first, int ps, List<SortMeta> multiSortMeta, Map<String, Object> filters ) {
            return load(first, ps);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder,
         *      java.util.Map)
         */
        @Override
        public List<MapEvent> load ( int first, int ps, String sortField, SortOrder sortOrder, Map<String, Object> filters ) {
            return load(first, ps);
        }


        /**
         * @param first
         * @param ps
         * @return
         */
        private List<MapEvent> load ( int first, int ps ) {
            try {
                return EntityHistoryTable.this.fsp.getAuditReaderService().getAllEntityEvents(
                    EntityHistoryTable.this.fileSelection.getSingleSelectionId(),
                    null,
                    null,
                    EntityHistoryTable.this.filterActions,
                    first,
                    ps);
            }
            catch ( Exception e ) {
                ExceptionHandler.handleException(e);
                return Collections.EMPTY_LIST;
            }
        }
    }
}
