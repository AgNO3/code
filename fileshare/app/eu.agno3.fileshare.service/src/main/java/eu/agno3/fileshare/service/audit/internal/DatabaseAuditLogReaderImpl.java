/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 19, 2017 by mbechler
 */
package eu.agno3.fileshare.service.audit.internal;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.AuditReaderService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
@Component ( service = AuditReaderService.class, configurationPid = "audit.reader.db" )
public class DatabaseAuditLogReaderImpl extends BaseAuditReaderServiceImpl {

    private static final Logger log = Logger.getLogger(DatabaseAuditLogReaderImpl.class);

    private static final ObjectMapper OM = new ObjectMapper();
    private static final JsonFactory JF;

    static {
        OM.registerModule(new JodaModule());
        JF = new JsonFactory(OM);
    }

    private DatabaseEventLogConfig config;
    private SchemaManagedDataSource dataSource;
    private DataSourceUtil dataSourceUtil;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#activate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        super.activate(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#modified(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        super.modified(ctx);
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
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#setAccessControlService(eu.agno3.fileshare.security.AccessControlService)
     */
    @Override
    @Reference
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        super.setAccessControlService(acs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#unsetAccessControlService(eu.agno3.fileshare.security.AccessControlService)
     */
    @Override
    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        super.unsetAccessControlService(acs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#setPolicyEvaluator(eu.agno3.fileshare.service.api.internal.PolicyEvaluator)
     */
    @Override
    @Reference
    protected synchronized void setPolicyEvaluator ( PolicyEvaluator pe ) {
        super.setPolicyEvaluator(pe);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#unsetPolicyEvaluator(eu.agno3.fileshare.service.api.internal.PolicyEvaluator)
     */
    @Override
    protected synchronized void unsetPolicyEvaluator ( PolicyEvaluator pe ) {
        super.unsetPolicyEvaluator(pe);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#setServiceContext(eu.agno3.fileshare.service.api.internal.DefaultServiceContext)
     */
    @Override
    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext ctx ) {
        super.setServiceContext(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#unsetServiceContext(eu.agno3.fileshare.service.api.internal.DefaultServiceContext)
     */
    @Override
    protected synchronized void unsetServiceContext ( DefaultServiceContext ctx ) {
        super.unsetServiceContext(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#setVFSService(eu.agno3.fileshare.service.api.internal.VFSServiceInternal)
     */
    @Override
    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        super.setVFSService(vs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#unsetVFSService(eu.agno3.fileshare.service.api.internal.VFSServiceInternal)
     */
    @Override
    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        super.unsetVFSService(vs);
    }


    @Reference
    protected synchronized void setDatabaseEventLogConfig ( DatabaseEventLogConfig ec ) {
        this.config = ec;
    }


    protected synchronized void unsetDatabaseEventLogConfig ( DatabaseEventLogConfig ec ) {
        if ( this.config == ec ) {
            this.config = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.AuditReaderService#isAvailable()
     */
    @Override
    public boolean isAvailable () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.AuditReaderService#getRetentionTimeDays()
     */
    @Override
    public int getRetentionTimeDays () {
        return this.config.getRetainDays();
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#fetchEntityEvents(eu.agno3.fileshare.model.EntityKey,
     *      org.joda.time.DateTime, org.joda.time.DateTime, java.util.Set, int, int)
     */
    @Override
    protected List<MapEvent> fetchEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions, int offset,
            int pageSize ) throws FileshareException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            log.debug("Fetch events"); //$NON-NLS-1$
            String id = entityId.toString();
            String evtable = this.dataSourceUtil.quoteIdentifier(conn, "audit_event"); //$NON-NLS-1$
            String enttable = this.dataSourceUtil.quoteIdentifier(conn, "audit_entity"); //$NON-NLS-1$
            List<MapEvent> results = new LinkedList<>();
            try ( PreparedStatement ps = conn.prepareStatement(createSelectQuery(start, end, filterActions, evtable, enttable)) ) {

                bindQueryParameters(ps, id, start, end, filterActions);

                try ( ResultSet rs = ps.executeQuery() ) {
                    while ( rs.next() ) {
                        try ( InputStream is = rs.getBinaryStream(1);
                              JsonParser parser = JF.createParser(is) ) {
                            results.add(parser.readValueAs(MapEvent.class));
                        }
                        catch ( IOException e ) {
                            log.warn("Failed to parse event", e); //$NON-NLS-1$
                        }
                    }
                }
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Found events " + results.size()); //$NON-NLS-1$
            }
            return results;
        }
        catch ( SQLException e ) {
            throw new FileshareException("Failed to read audit log", e); //$NON-NLS-1$
        }
    }


    /**
     * @param start
     * @param end
     * @param filterActions
     * @param evtable
     * @param enttable
     * @return
     */
    @SuppressWarnings ( "nls" )
    private static String createSelectQuery ( DateTime start, DateTime end, Set<String> filterActions, String evtable, String enttable ) {
        return "SELECT " + evtable + ".payload FROM " + enttable + " " + "LEFT JOIN " + evtable + " ON " + enttable + ".evid = " + evtable + ".evid "
                + "WHERE " + createQuery(start, end, filterActions, evtable, enttable);
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#countEntityEvents(eu.agno3.fileshare.model.EntityKey,
     *      org.joda.time.DateTime, org.joda.time.DateTime, java.util.Set)
     */
    @Override
    protected long countEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions ) throws FileshareException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            log.debug("Count events"); //$NON-NLS-1$
            String id = entityId.toString();
            String evtable = this.dataSourceUtil.quoteIdentifier(conn, "audit_event"); //$NON-NLS-1$
            String enttable = this.dataSourceUtil.quoteIdentifier(conn, "audit_entity"); //$NON-NLS-1$
            try ( PreparedStatement ps = conn.prepareStatement(createCountQuery(start, end, filterActions, evtable, enttable)) ) {
                bindQueryParameters(ps, id, start, end, filterActions);
                try ( ResultSet rs = ps.executeQuery() ) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }
        catch ( SQLException e ) {
            throw new FileshareException("Failed to read audit log", e); //$NON-NLS-1$
        }
    }


    /**
     * @param start
     * @param end
     * @param filterActions
     * @param evtable
     * @param enttable
     * @return
     */
    @SuppressWarnings ( "nls" )
    private static String createCountQuery ( DateTime start, DateTime end, Set<String> filterActions, String evtable, String enttable ) {
        return "SELECT COUNT(" + evtable + ".evid) FROM " + enttable + " " + "LEFT JOIN " + evtable + " ON " + enttable + ".evid = " + evtable
                + ".evid " + "WHERE " + createQuery(start, end, filterActions, evtable, enttable);
    }


    private static void bindQueryParameters ( PreparedStatement ps, String id, DateTime start, DateTime end, Set<String> filterActions )
            throws SQLException {
        int i = 1;
        ps.setString(i, id);
        i++;
        if ( start != null ) {
            ps.setTimestamp(i, new Timestamp(start.toDateTime(DateTimeZone.UTC).getMillis()), DatabaseEventLoggerBackend.utc());
            i++;
        }
        if ( end != null ) {
            ps.setTimestamp(i, new Timestamp(end.toDateTime(DateTimeZone.UTC).getMillis()), DatabaseEventLoggerBackend.utc());
            i++;
        }
        if ( filterActions != null && !filterActions.isEmpty() ) {
            for ( String filterAction : filterActions ) {
                ps.setString(i, filterAction);
                i++;
            }
        }
    }


    @SuppressWarnings ( "nls" )
    private static String createQuery ( DateTime start, DateTime end, Set<String> filterActions, String evtable, String enttable ) {
        StringBuilder sb = new StringBuilder();
        sb.append(enttable);
        sb.append(".key = ?");

        if ( start != null ) {
            sb.append(" AND " + evtable + ".ts >= ?");
        }
        if ( end != null ) {
            sb.append(" AND " + evtable + ".ts <= ?");
        }
        if ( filterActions != null && !filterActions.isEmpty() ) {
            sb.append(" AND " + evtable + ".action IN (");
            sb.append(String.join(",", Collections.nCopies(filterActions.size(), "?")));
            sb.append(")");
        }
        return sb.toString();
    }

}
