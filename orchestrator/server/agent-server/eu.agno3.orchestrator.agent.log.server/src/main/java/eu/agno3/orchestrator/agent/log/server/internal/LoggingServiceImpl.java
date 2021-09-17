/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.02.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.log.server.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logging.SyslogEvent;
import eu.agno3.orchestrator.system.logging.service.LoggingService;
import eu.agno3.orchestrator.system.logging.service.LoggingServiceDescriptor;
import eu.agno3.runtime.eventlog.EventFilter;
import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader;
import eu.agno3.runtime.eventlog.elasticsearch.QueryParamBuilder;
import eu.agno3.runtime.eventlog.impl.MapEvent;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    LoggingService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.system.logging.service.LoggingService",
    targetNamespace = LoggingServiceDescriptor.NAMESPACE,
    serviceName = LoggingServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/agent/log" )
public class LoggingServiceImpl implements LoggingService {

    private static final Logger log = Logger.getLogger(LoggingServiceImpl.class);

    private static final Set<String> VALID_FILTER_PROPERTIES = new HashSet<>(Arrays.asList(LogFields.TAG)); // $NON-NLS-1$

    private ElasticsearchLogReader reader;
    private DefaultServerServiceContext sctx;
    private ObjectAccessControl authz;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setElasticsearchReader ( ElasticsearchLogReader elr ) {
        this.reader = elr;
    }


    protected synchronized void unsetElasticsearchReader ( ElasticsearchLogReader elr ) {
        if ( this.reader == elr ) {
            this.reader = null;
        }
    }


    @Reference
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logging.service.LoggingService#list(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.runtime.eventlog.EventFilter, long, int, int)
     */
    @Override
    @RequirePermissions ( "sysinfo:logs:view" )
    public List<String> list ( StructuralObject anchor, EventFilter filter, long startTime, int offset, int limit )
            throws ModelServiceException, ModelObjectNotFoundException {
        if ( this.reader == null ) {
            log.debug("No reader present"); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        Set<String> objIds = resolveChildren(anchor);
        List<String> results = new ArrayList<>();
        QueryParamBuilder qb = makeQueryBuilder(objIds, makeFilter(filter), startTime, offset, limit);
        SearchResponse resp = this.reader.searchEvents(qb);
        for ( SearchHit hit : resp.getHits() ) {
            results.add(hit.getSourceAsString());
        }
        return results;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.system.logging.service.LoggingService#getById(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "sysinfo:logs:view" )
    public String getById ( StructuralObject anchor, String id ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( this.reader == null ) {
            throw new ModelObjectNotFoundException(
                "Event not found, index not available", //$NON-NLS-1$
                new ModelObjectNotFoundFault(SyslogEvent.class.getName(), id));
        }

        Set<String> objIds = resolveChildren(anchor);
        if ( log.isDebugEnabled() ) {
            log.debug("Loading event with id " + id); //$NON-NLS-1$
        }
        QueryParamBuilder queryBuilder = makeQueryBuilder(objIds, null, QueryBuilders.termQuery("id", id)); //$NON-NLS-1$
        SearchResponse resp = this.reader.searchEvents(queryBuilder);
        if ( resp.getHits().getTotalHits() == 0 ) {
            throw new ModelObjectNotFoundException("Event not found", new ModelObjectNotFoundFault(SyslogEvent.class.getName(), id)); //$NON-NLS-1$
        }
        return resp.getHits().getAt(0).getSourceAsString();
    }


    /**
     * @param anchor
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private Set<String> resolveChildren ( StructuralObject anchor ) throws ModelServiceException, ModelObjectNotFoundException {
        if ( anchor == null ) {
            throw new ModelObjectNotFoundException(StructuralObject.class, null);
        }
        if ( anchor instanceof ServiceStructuralObject ) {
            this.authz.checkAccess(anchor, "sysinfo:logs:view"); //$NON-NLS-1$
            return Collections.singleton(anchor.getId().toString());
        }

        @NonNull
        EntityManager em = this.sctx.createConfigEM();
        @SuppressWarnings ( "null" )
        List<@NonNull AbstractStructuralObjectImpl> children = TreeUtil
                .getChildren(em, AbstractStructuralObjectImpl.class, PersistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor.getId()));
        Set<String> objIds = new HashSet<>();

        objIds.add(anchor.getId().toString());

        for ( AbstractStructuralObjectImpl obj : children ) {
            if ( this.authz.hasAccess(obj, "sysinfo:logs:view") ) { //$NON-NLS-1$
                objIds.add(obj.getId().toString());
            }
        }
        return objIds;
    }


    @Override
    @RequirePermissions ( "sysinfo:logs:view" )
    public long count ( StructuralObject anchor, EventFilter f, long startTime ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( this.reader == null ) {
            return 0;
        }
        Set<String> objIds = resolveChildren(anchor);
        return Math.min(8000, this.reader.countEvents(makeQueryBuilder(objIds, startTime, makeFilter(f))));
    }


    /**
     * @param f
     * @return
     */
    private static QueryBuilder makeFilter ( EventFilter f ) {
        if ( f == null ) {
            log.trace("Filter is null"); //$NON-NLS-1$
            return null;
        }

        BoolQueryBuilder q = QueryBuilders.boolQuery();
        if ( f.getFilterSeverity() != null && f.getFilterSeverity() != EventSeverity.UNKNOWN ) {
            int ordStart = f.getFilterSeverity().ordinal();
            Set<String> severities = new HashSet<>();

            for ( int i = ordStart; i <= EventSeverity.ERROR.ordinal(); i++ ) {
                severities.add(EventSeverity.values()[ i ].name());
            }

            q.must(QueryBuilders.termsQuery("severity", severities.toArray())); //$NON-NLS-1$
        }

        if ( f.getStartTime() != null || f.getEndTime() != null ) {
            RangeQueryBuilder rq = QueryBuilders.rangeQuery(MapEvent.TIMESTAMP);

            if ( f.getStartTime() != null ) {
                rq.gte(f.getStartTime().getMillis()); // $NON-NLS-1$
            }

            if ( f.getEndTime() != null ) {
                rq.lte(f.getEndTime().getMillis()); // $NON-NLS-1$
            }

            q.must(rq);
        }

        if ( !StringUtils.isBlank(f.getFilterMessage()) ) {
            q.must(QueryBuilders.matchQuery("message", f.getFilterMessage()).operator(Operator.AND)); //$NON-NLS-1$
        }

        for ( Entry<String, String> e : f.getFilterProperties().entrySet() ) {
            if ( VALID_FILTER_PROPERTIES.contains(e.getKey()) && !StringUtils.isBlank(e.getValue()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Adding property filter %s = %s", e.getKey(), e.getValue())); //$NON-NLS-1$
                }
                q.must(QueryBuilders.termQuery("properties." + e.getKey(), e.getValue())); //$NON-NLS-1$
            }
        }

        return q;
    }


    /**
     * @param objIds
     * @param startTime
     * @param limit
     * @param limit2
     * @return
     */
    private static QueryParamBuilder makeQueryBuilder ( Set<String> objIds, QueryBuilder extraQuery, Long startTime, int offset, int limit ) {
        return makeQueryBuilder(objIds, startTime, extraQuery).pageFrom(offset).pageSize(limit);
    }


    private static QueryParamBuilder makeQueryBuilder ( Set<String> objIds, Long startTime, QueryBuilder extraQuery ) {
        BoolQueryBuilder q = QueryBuilders.boolQuery();
        q.must(QueryBuilders.termsQuery("objectId", objIds.toArray())); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug("Querying from timestamp " + startTime); //$NON-NLS-1$
            log.debug("Querying objects " + objIds); //$NON-NLS-1$
        }
        if ( startTime != null ) {
            q.must(QueryBuilders.rangeQuery(MapEvent.TIMESTAMP).lte(startTime)); // $NON-NLS-1$
        }
        if ( extraQuery != null ) {
            q.must(extraQuery);
        }

        if ( log.isTraceEnabled() ) {
            log.trace(q);
        }
        return QueryParamBuilder.get().type("syslog-event").filterDuplicates().query(q); //$NON-NLS-1$
    }

}
