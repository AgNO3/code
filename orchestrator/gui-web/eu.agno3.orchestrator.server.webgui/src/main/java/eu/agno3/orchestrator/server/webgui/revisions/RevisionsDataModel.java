/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.revisions;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.versioning.VersionInfo;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.config.model.realm.service.RevisionsService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.exceptions.ModelExceptionHandler;


/**
 * @author mbechler
 * 
 */
@ViewScoped
@Named ( "revisionsDataModel" )
public class RevisionsDataModel extends LazyDataModel<VersionInfo> implements Serializable {

    private static final long serialVersionUID = -5933705471291607615L;
    private Map<Long, VersionInfo> revisionsIndex;
    private List<VersionInfo> revisions;

    private UUID configId;

    @Inject
    private ServerServiceProvider ssp;


    /**
     * @return the configId
     */
    public UUID getConfigId () {
        return this.configId;
    }


    /**
     * @param configId
     *            the configId to set
     */
    public void setConfigId ( UUID configId ) {
        this.configId = configId;
    }


    /**
     * 
     * @return the most recent configuration
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getConfiguration () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.ssp.getService(ConfigurationService.class).fetchById(this.getConfigId());
    }


    private void loadAll () throws AbstractModelException, GuiWebServiceException {
        if ( this.revisions == null ) {
            this.revisions = this.ssp.getService(RevisionsService.class).getRevisions(this.getConfiguration());
            this.revisionsIndex = new HashMap<>(this.revisions.size());

            for ( VersionInfo rev : this.revisions ) {
                this.revisionsIndex.put(rev.getRevisionNumber(), rev);
            }

            this.setRowCount(this.revisions.size());
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.LazyDataModel#load(int, int, java.util.List, java.util.Map)
     */
    @Override
    public List<VersionInfo> load ( int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters ) {
        try {
            this.loadAll();
        }
        catch ( AbstractModelException e ) {
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_REVISIONS_LOAD_FAILED), e);
            return Collections.EMPTY_LIST;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_LIST;
        }

        return new ArrayList<>(this.revisions.subList(first, Math.min(first + pageSize, this.revisions.size())));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder,
     *      java.util.Map)
     */
    @Override
    public List<VersionInfo> load ( int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters ) {
        try {
            this.loadAll();
        }
        catch ( AbstractModelException e ) {
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_REVISIONS_LOAD_FAILED), e);
            return Collections.EMPTY_LIST;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_LIST;
        }

        return new ArrayList<>(this.revisions.subList(first, Math.min(first + pageSize, this.revisions.size())));
    }


    @Override
    public VersionInfo getRowData ( String key ) {
        if ( this.revisionsIndex == null ) {
            return null;
        }
        long ver = Long.parseLong(key);
        return this.revisionsIndex.get(ver);
    }


    @Override
    public Object getRowKey ( VersionInfo ver ) {
        return ver.getRevisionNumber();
    }
}