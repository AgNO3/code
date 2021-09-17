/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 19, 2017 by mbechler
 */
package eu.agno3.fileshare.service.audit.internal;


import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.audit.BaseFileshareEvent;
import eu.agno3.fileshare.model.audit.EntityFileshareEvent;
import eu.agno3.fileshare.model.audit.MoveEntityFileshareEvent;
import eu.agno3.fileshare.model.audit.MultiEntityFileshareEvent;
import eu.agno3.fileshare.model.audit.SingleEntityFileshareEvent;
import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;
import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventLoggerBackend;


/**
 * @author mbechler
 *
 */
@Component ( service = EventLoggerBackend.class )
public class DatabaseEventLoggerBackend implements EventLoggerBackend {

    private static final Logger log = Logger.getLogger(DatabaseEventLoggerBackend.class);

    private static final Set<String> INCLUDE_PARENT_ACTIONS = new HashSet<>(Arrays.asList(BaseAuditReaderServiceImpl.INCLUDE_PARENT_ACTIONS));
    private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$


    static Calendar utc () {
        return (Calendar) UTC.clone();
    }

    private DatabaseEventLogConfig config;

    private SchemaManagedDataSource dataSource;

    private DataSourceUtil dataSourceUtil;


    @Reference
    protected synchronized void setDatabaseEventLogConfig ( DatabaseEventLogConfig ec ) {
        this.config = ec;
    }


    protected synchronized void unsetDatabaseEventLogConfig ( DatabaseEventLogConfig ec ) {
        if ( this.config == ec ) {
            this.config = null;
        }
    }


    @Reference ( target = "(dataSourceName=fileshare)" )
    protected synchronized void setDataSource ( SchemaManagedDataSource ds ) {
        this.dataSource = ds;
    }


    protected synchronized void unsetDataSource ( SchemaManagedDataSource ds ) {
        if ( this.dataSource == ds ) {
            this.dataSource = null;
        }
    }


    @Reference ( target = "(dataSourceName=fileshare)" )
    protected synchronized void setDataSourceUtil ( DataSourceUtil dsu ) {
        this.dataSourceUtil = dsu;
    }


    protected synchronized void unsetDataSourceUtil ( DataSourceUtil dsu ) {
        if ( this.dataSourceUtil == dsu ) {
            this.dataSourceUtil = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getPriority()
     */
    @Override
    public int getPriority () {
        return 110;
    }


    @Override
    public Future<?> log ( Event ev, byte[] bytes ) {
        if ( this.config.isIgnorePostdated() && this.config.getRetainDays() > 0
                && ev.getTimestamp().isBefore(DateTime.now().minusDays(this.config.getRetainDays())) ) {
            log.warn("Tried to log an event that is older than retention time"); //$NON-NLS-1$
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Logging %s: %s", ev.getType(), new String(bytes, StandardCharsets.UTF_8))); //$NON-NLS-1$
        }

        Set<EntityKey> indexForKey = getIndexKeys(ev);
        UUID evid = getId(ev);
        String action = ev instanceof BaseFileshareEvent ? ( (BaseFileshareEvent) ev ).getAction() : null;
        try ( Connection conn = this.dataSource.getConnection() ) {
            conn.setAutoCommit(false);
            try ( PreparedStatement ps = conn.prepareStatement("INSERT INTO " + //$NON-NLS-1$
                    this.dataSourceUtil.quoteIdentifier(conn, "audit_event") + //$NON-NLS-1$
                    " (evid,ts,type,action,payload) VALUES (?,?,?,?,?)") ) { //$NON-NLS-1$
                this.dataSourceUtil.setParameter(ps, 1, evid);
                DateTime dt = ev.getTimestamp().toDateTime(DateTimeZone.UTC);
                ps.setTimestamp(2, new Timestamp(dt.getMillis()), utc());
                ps.setString(3, ev.getType());
                ps.setString(4, action);
                ps.setBytes(5, bytes);
                ps.executeUpdate();
            }
            try ( PreparedStatement ps = conn.prepareStatement("INSERT INTO " + //$NON-NLS-1$
                    this.dataSourceUtil.quoteIdentifier(conn, "audit_entity") + //$NON-NLS-1$
                    " (key,evid) VALUES (?,?)") ) { //$NON-NLS-1$
                for ( EntityKey indexKey : indexForKey ) {
                    if ( indexKey != null ) {
                        ps.setString(1, indexKey.toString());
                        this.dataSourceUtil.setParameter(ps, 2, evid);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            conn.commit();
        }
        catch (

        SQLException e ) {
            log.error("Failed to write audit log", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param ev
     * @return
     */
    protected UUID getId ( Event ev ) {
        UUID evid = null;
        try {
            if ( ev.getId() != null ) {
                evid = UUID.fromString(ev.getId());
            }
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Invalid UUID", e); //$NON-NLS-1$
        }
        if ( evid == null ) {
            evid = UUID.randomUUID();
        }
        return evid;
    }


    /**
     * @param ev
     * @return
     */
    protected Set<EntityKey> getIndexKeys ( Event ev ) {
        Set<EntityKey> indexForKey = new HashSet<>();
        if ( ev instanceof EntityFileshareEvent ) {
            EntityFileshareEvent fev = (EntityFileshareEvent) ev;
            boolean includeParent = INCLUDE_PARENT_ACTIONS.contains(fev.getAction());
            if ( ev instanceof MoveEntityFileshareEvent ) {
                MoveEntityFileshareEvent mev = (MoveEntityFileshareEvent) ev;
                for ( int i = 0; i < mev.getSourceEntityIds().size(); i++ ) {
                    indexForKey.add(mev.getSourceEntityIds().get(i));
                    if ( includeParent ) {
                        indexForKey.add(mev.getSourceEntityParentIds().get(i));
                    }
                    indexForKey.add(mev.getTargetId());
                }
            }
            else if ( ev instanceof MultiEntityFileshareEvent ) {
                MultiEntityFileshareEvent mev = (MultiEntityFileshareEvent) ev;
                indexForKey.addAll(mev.getTargetEntityIds());
            }
            else if ( ev instanceof SingleEntityFileshareEvent ) {
                SingleEntityFileshareEvent sev = (SingleEntityFileshareEvent) ev;
                indexForKey.add(sev.getTargetEntityId());
                if ( includeParent ) {
                    indexForKey.add(sev.getTargetParentId());
                }
            }
        }
        return indexForKey;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#bulkLog(java.util.List, java.util.Map)
     */
    @Override
    public Future<?> bulkLog ( List<Event> evs, Map<Event, byte[]> data ) {
        for ( Event ev : evs ) {
            log(ev, data.get(ev));
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#runMaintenance()
     */
    @Override
    public void runMaintenance () {
        log.debug("runMaintenance"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#reset()
     */
    @Override
    public void reset () {
        log.debug("Reset"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getExcludeStreams()
     */
    @Override
    public Set<String> getExcludeStreams () {
        return this.config.getExcludeStreams();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getIncludeStreams()
     */
    @Override
    public Set<String> getIncludeStreams () {
        return this.config.getIncludeStreams();
    }

}
