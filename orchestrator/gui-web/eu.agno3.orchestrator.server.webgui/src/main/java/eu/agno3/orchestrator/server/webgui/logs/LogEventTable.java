package eu.agno3.orchestrator.server.webgui.logs;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.prefs.LocaleSettingsBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logging.service.LoggingService;
import eu.agno3.runtime.eventlog.EventFilter;
import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "logEventTable" )
public class LogEventTable implements Serializable {

    private static final Logger log = Logger.getLogger(LogEventTable.class);
    private static final ObjectMapper OM = new ObjectMapper();
    static final JsonFactory JF;

    private static final DateTimeFormatter FILE_DATE_FORMAT = new DateTimeFormatterBuilder().appendYear(4, 4).appendMonthOfYear(2).appendDayOfMonth(2)
            .appendHourOfDay(2).appendMinuteOfHour(2).toFormatter();


    static {
        OM.registerModule(new JodaModule());
        JF = new JsonFactory(OM);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3302512641784005505L;

    @Inject
    ServerServiceProvider ssp;

    @Inject
    StructureViewContextBean structureContext;

    @Inject
    LocaleSettingsBean localeSettings;

    private LazyLoadEventDataModel model;

    private EventFilter filter = new EventFilter();

    private int pageSize = 50;
    private MapEvent[] selection;
    private boolean follow;
    private boolean relativeTime;


    @PostConstruct
    public void init () {
        this.filter.setFilterSeverity(EventSeverity.INFO);
    }


    /**
     * @param pageSize
     *            the pageSize to set
     */
    protected void setPageSize ( int pageSize ) {
        this.pageSize = pageSize;
    }


    public void setSelection ( MapEvent[] events ) {
        this.selection = events;
    }


    /**
     * @return the selection
     */
    public MapEvent[] getSelection () {
        return this.selection;
    }


    /**
     * @return the query filter
     */
    public EventFilter getFilter () {
        return this.filter;
    }


    public boolean getFollow () {
        return this.follow;
    }


    public void setFollow ( boolean follow ) {
        this.follow = follow;
    }


    public boolean getRelativeTime () {
        return this.relativeTime;
    }


    public void setRelativeTime ( boolean relativeTime ) {
        this.relativeTime = relativeTime;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @return the model
     */
    public LazyLoadEventDataModel getModel () {
        if ( this.model == null ) {
            this.model = makeModel();
        }
        return this.model;
    }


    /**
     * @return the pageSize
     */
    public int getPageSize () {
        return this.pageSize;
    }


    public Date getStartDate () {
        return this.filter.getStartTime() != null ? this.filter.getStartTime().toDate() : null;
    }


    public String getStartDateString () {
        return this.filter.getStartTime() != null ? this.localeSettings.formatDateTime(this.filter.getStartTime(), "SM") : null; //$NON-NLS-1$
    }


    public void setStartDate ( Date d ) {
        this.filter.setStartTime(d != null ? new DateTime(d) : null);
    }


    public Date getEndDate () {
        return this.filter.getEndTime() != null ? this.filter.getEndTime().toDate() : null;
    }


    public void setEndDate ( Date d ) {
        this.filter.setEndTime(d != null ? new DateTime(d) : null);
    }


    public String getEndDateString () {
        return this.filter.getEndTime() != null ? this.localeSettings.formatDateTime(this.filter.getEndTime(), "SM") : null; //$NON-NLS-1$
    }


    public String getFilterTag () {
        return this.filter.getFilterProperties().get(LogFields.TAG);
    }


    public void setFilterTag ( String tag ) {
        this.filter.getFilterProperties().put(LogFields.TAG, tag);
    }


    /**
     * 
     */
    public void refresh () {
        if ( this.follow ) {
            this.filter.setEndTime(DateTime.now());
        }
        this.model = makeModel();
    }


    public StreamedContent getDownload () {
        long startTime;
        if ( getFilter().getEndTime() != null ) {
            startTime = getFilter().getEndTime().getMillis();
        }
        else {
            startTime = System.currentTimeMillis();
        }

        try {
            StringBuilder sb = new StringBuilder(this.structureContext.getSelectedDisplayName());
            sb.append('-');

            if ( getFilter().getStartTime() != null ) {
                sb.append(getFilter().getStartTime().toString(FILE_DATE_FORMAT));
                sb.append('-');
            }

            sb.append(new DateTime(startTime).toString(FILE_DATE_FORMAT));

            sb.append(".log"); //$NON-NLS-1$

            List<String> items = this.ssp.getService(LoggingService.class)
                    .list(this.structureContext.getSelectedObject(), getFilter(), startTime, 0, getPageSize());

            return new LogDownloadContent(sb.toString(), items);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

    }


    public void refresh ( AjaxBehaviorEvent ev ) {
        refresh();
    }


    /**
     * @return
     */
    private LazyLoadEventDataModel makeModel () {
        return new LazyLoadEventDataModel();
    }

    class LazyLoadEventDataModel extends LazyDataModel<MapEvent> {

        /**
         * 
         */
        private static final long serialVersionUID = -5399284099602325183L;
        private DateTime firstTimestamp;
        private DateTime lastTimestamp;


        /**
         * 
         */
        public LazyLoadEventDataModel () {
            refreshCount();
        }


        /**
         * 
         */
        public void refreshCount () {
            try {
                int eventCount = (int) LogEventTable.this.ssp.getService(LoggingService.class)
                        .count(LogEventTable.this.structureContext.getSelectedObject(), LogEventTable.this.getFilter(), getStartTime());
                this.setRowCount(eventCount);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#getRowKey(java.lang.Object)
         */
        @Override
        public Object getRowKey ( MapEvent object ) {
            if ( object != null ) {
                return object.getId();
            }
            return super.getRowKey(object);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#getRowData(java.lang.String)
         */
        @Override
        public MapEvent getRowData ( String rowKey ) {
            try ( JsonParser p = JF.createParser(
                LogEventTable.this.ssp.getService(LoggingService.class).getById(LogEventTable.this.structureContext.getSelectedObject(), rowKey)) ) {
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug("Loading event with rowKey " + rowKey); //$NON-NLS-1$
                }
                return p.readValueAs(MapEvent.class);
            }
            catch ( Exception e ) {
                getLog().warn("Failed to get event", e); //$NON-NLS-1$
                return null;
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.util.List, java.util.Map)
         */
        @Override
        public List<MapEvent> load ( int first, int ps, List<SortMeta> multiSortMeta, Map<String, Object> filters ) {
            return load(first, ps, filters);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, org.primefaces.model.SortOrder,
         *      java.util.Map)
         */
        @Override
        public List<MapEvent> load ( int first, int ps, String sortField, SortOrder sortOrder, Map<String, Object> filters ) {
            return load(first, ps, filters);
        }


        /**
         * @param first
         * @param ps
         * @return
         */
        private List<MapEvent> load ( int first, int ps, Map<String, Object> filters ) {
            try {
                long startTime;

                if ( getFilter().getEndTime() != null ) {
                    startTime = getFilter().getEndTime().getMillis();
                }
                else {
                    startTime = ( first != 0 && ps > 1 ) ? getStartTime()
                            : ( this.firstTimestamp != null ? this.firstTimestamp.getMillis() : System.currentTimeMillis() );
                }

                if ( getLog().isDebugEnabled() ) {
                    getLog().debug(String.format("Loading from timestamp %s offset %d page size %d", startTime, ps > 1 ? 0 : first, ps)); //$NON-NLS-1$
                    getLog().debug("Filters " + filters); //$NON-NLS-1$
                }

                List<String> data = LogEventTable.this.ssp.getService(LoggingService.class).list(
                    LogEventTable.this.structureContext.getSelectedObject(),
                    LogEventTable.this.getFilter(),
                    startTime,
                    ps > 1 ? 0 : first,
                    ps);
                if ( data == null ) {
                    return Collections.EMPTY_LIST;
                }

                List<MapEvent> evs = new ArrayList<>();
                for ( String ev : data ) {
                    try ( JsonParser p = JF.createParser(ev) ) {
                        evs.add(p.readValueAs(MapEvent.class));
                    }
                    catch ( IOException e ) {
                        getLog().warn("Failed to parse event", e); //$NON-NLS-1$
                    }
                }

                if ( getLog().isDebugEnabled() ) {
                    getLog().debug("Loaded " + evs.size()); //$NON-NLS-1$
                }
                updateBoundaries(first, evs);
                return evs;
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return Collections.EMPTY_LIST;
            }
        }


        private long getStartTime () {
            if ( getLog().isDebugEnabled() ) {
                getLog().debug("Last known timestamp is " + //$NON-NLS-1$
                        ( this.lastTimestamp != null ? this.lastTimestamp.getMillis() : "NULL" )); //$NON-NLS-1$
                getLog().debug("First known timestamp is " + //$NON-NLS-1$
                        ( this.firstTimestamp != null ? this.firstTimestamp.getMillis() : "NULL" )); //$NON-NLS-1$
            }
            return this.lastTimestamp != null ? this.lastTimestamp.getMillis() : System.currentTimeMillis();
        }


        /**
         * @param first
         * @param evs
         */
        private void updateBoundaries ( int first, List<MapEvent> evs ) {
            if ( !evs.isEmpty() ) {
                if ( first == 0 ) {
                    MapEvent top = evs.get(0);
                    if ( this.firstTimestamp == null ) {
                        if ( getLog().isDebugEnabled() ) {
                            getLog().debug("Setting first timestamp " + top.getTimestamp()); //$NON-NLS-1$
                        }
                        this.firstTimestamp = top.getTimestamp();
                    }

                }

                MapEvent bottom = evs.get(evs.size() - 1);
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug(String.format("Bottom timestamp is %s current last %s", bottom.getTimestamp(), this.lastTimestamp)); //$NON-NLS-1$
                }
                if ( this.lastTimestamp == null || this.lastTimestamp.isAfter(bottom.getTimestamp()) ) {
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Setting last timestamp " + bottom.getTimestamp()); //$NON-NLS-1$
                    }
                    this.lastTimestamp = bottom.getTimestamp();
                }
            }
        }
    }

}
