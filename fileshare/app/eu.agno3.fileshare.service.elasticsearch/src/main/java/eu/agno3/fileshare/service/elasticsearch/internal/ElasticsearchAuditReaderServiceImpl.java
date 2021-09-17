/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.elasticsearch.internal;


import java.util.List;
import java.util.Set;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.AuditReaderService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl;
import eu.agno3.runtime.eventlog.elasticsearch.ElasticsearchLogReader;
import eu.agno3.runtime.eventlog.elasticsearch.QueryParamBuilder;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
@Component ( service = AuditReaderService.class, configurationPid = "audit.reader.elastic" )
public class ElasticsearchAuditReaderServiceImpl extends BaseAuditReaderServiceImpl implements AuditReaderService {

    /**
     * 
     */
    private ElasticsearchLogReader reader;


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
    protected synchronized void setLogReader ( ElasticsearchLogReader elr ) {
        this.reader = elr;
    }


    protected synchronized void unsetLogReader ( ElasticsearchLogReader elr ) {
        if ( this.reader == elr ) {
            this.reader = null;
        }
    }


    @Override
    public boolean isAvailable () {
        return this.reader != null;
    }


    @Override
    public int getRetentionTimeDays () {
        if ( this.reader == null ) {
            return 0;
        }
        return this.reader.getRetentionDays();
    }


    /**
     * @param entityId
     * @param start
     * @param end
     * @param filterActions
     * @param offset
     * @param pageSize
     * @return
     */
    @Override
    protected List<MapEvent> fetchEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions, int offset,
            int pageSize ) {
        QueryParamBuilder pb = QueryParamBuilder.get();

        pb.pageFrom(offset);
        pb.pageSize(Math.min(MAX_PAGESIZE, pageSize));

        QueryBuilder completeFilter = makeEntityFilter(entityId, start, end, filterActions, pb);
        pb.query(completeFilter);
        return this.reader.findAllEvents(pb);
    }


    /**
     * @param entityId
     * @param start
     * @param end
     * @param filterActions
     * @return
     */
    @Override
    protected long countEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions ) {
        QueryParamBuilder pb = QueryParamBuilder.get();

        QueryBuilder completeFilter = makeEntityFilter(entityId, start, end, filterActions, pb);
        pb.query(completeFilter);
        return this.reader.countEvents(pb);
    }


    /**
     * @param entityId
     * @param start
     * @param end
     * @param filterActions
     * @param pb
     * @return
     */
    private static QueryBuilder makeEntityFilter ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions,
            QueryParamBuilder pb ) {
        if ( start != null ) {
            pb.from(start);
        }
        if ( end != null ) {
            pb.to(end);
        }

        String id = entityId.toString();

        pb.type(SINGLE_ENTITY_TYPE, MULTI_ENTITY_TYPE, MOVE_ENTITY_TYPE);

        QueryBuilder singleEntityFilter = QueryBuilders.boolQuery().must(QueryBuilders.typeQuery(SINGLE_ENTITY_TYPE)).must(
            QueryBuilders.boolQuery().minimumShouldMatch(1).should(QueryBuilders.termQuery(TARGET_ENTITY_ID, id)).should(
                QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TARGET_PARENT_ID, id))
                        .must(QueryBuilders.termsQuery(ACTION, INCLUDE_PARENT_ACTIONS))));

        QueryBuilder multiEntityFilter = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TYPE, MULTI_ENTITY_TYPE)).must(
            QueryBuilders.boolQuery().minimumShouldMatch(1).should(QueryBuilders.termQuery(TARGET_ENTITY_IDS, id)).should(
                QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TARGET_PARENT_ID, id))
                        .must(QueryBuilders.termsQuery(ACTION, INCLUDE_PARENT_ACTIONS))));

        QueryBuilder moveEntityFilter = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TYPE, MOVE_ENTITY_TYPE)).must(
            QueryBuilders.boolQuery().minimumShouldMatch(1).should(QueryBuilders.termQuery(SOURCE_ENTITY_IDS, id))
                    .should(QueryBuilders.termQuery(SOURCE_ENTITY_PARENT_IDS, id)).should(
                        QueryBuilders.boolQuery().must(QueryBuilders.termQuery(TARGET_ID, id))
                                .must(QueryBuilders.termsQuery(ACTION, INCLUDE_PARENT_ACTIONS))));

        QueryBuilder completeFilter = QueryBuilders.boolQuery().minimumShouldMatch(1).should(singleEntityFilter).should(multiEntityFilter)
                .should(moveEntityFilter);

        if ( filterActions != null ) {
            QueryBuilder typeFilter = QueryBuilders.termsQuery(ACTION, filterActions.toArray());
            completeFilter = QueryBuilders.boolQuery().must(typeFilter).must(completeFilter);
        }
        return completeFilter;
    }
}
