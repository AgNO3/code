/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.jobs;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import eu.agno3.orchestrator.jobs.JobStatusInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.service.JobInfoService;


/**
 * @author mbechler
 * 
 */
public class JobInfoDataModel extends LazyDataModel<JobStatusInfo> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4717252951391409154L;

    private static final Logger log = Logger.getLogger(JobInfoDataModel.class);

    private JobInfoService jobInfoService;
    private List<JobStatusInfo> infos;
    private Map<UUID, JobStatusInfo> infoIndex;

    private int max;


    /**
     * @param jobInfoService
     * @param max
     */
    public JobInfoDataModel ( JobInfoService jobInfoService, int max ) {
        this.jobInfoService = jobInfoService;
        this.max = max;
    }


    private void loadAll ( int first, int pageSize ) throws JobQueueException {
        if ( this.infos == null ) {
            log.debug("Loading jobs"); //$NON-NLS-1$
            this.infos = new ArrayList<>(this.jobInfoService.listJobs(this.max));

            this.infoIndex = new HashMap<>(this.infos.size());

            for ( JobStatusInfo info : this.infos ) {
                this.infoIndex.put(info.getJobId(), info);
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found %d jobs", this.infos.size())); //$NON-NLS-1$
            }
            this.setRowCount(this.infos.size());
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.LazyDataModel#load(int, int, java.util.List, java.util.Map)
     */
    @Override
    public List<JobStatusInfo> load ( int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters ) {
        try {
            this.loadAll(first, pageSize);
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to load job info", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        return new ArrayList<>(this.infos.subList(first, Math.min(first + pageSize, this.infos.size())));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder,
     *      java.util.Map)
     */
    @Override
    public List<JobStatusInfo> load ( int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters ) {
        try {
            this.loadAll(first, pageSize);
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to load job info", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        return new ArrayList<>(this.infos.subList(first, Math.min(first + pageSize, this.infos.size())));
    }


    @Override
    public JobStatusInfo getRowData ( String key ) {
        if ( this.infoIndex == null ) {
            return null;
        }
        UUID jobId = UUID.fromString(key);
        return this.infoIndex.get(jobId);
    }


    @Override
    public Object getRowKey ( JobStatusInfo ver ) {
        return ver.getJobId().toString();
    }

}
