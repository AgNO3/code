/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch;


import org.elasticsearch.index.query.QueryBuilder;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class QueryParamBuilder {

    private QueryParams params = new QueryParams();


    /**
     * 
     */
    private QueryParamBuilder () {}


    /**
     * 
     * @return the constructed params
     */
    public QueryParams build () {
        return this.params;
    }


    /**
     * @return a param builder
     */
    public static final QueryParamBuilder get () {
        return new QueryParamBuilder();
    }


    /**
     * @param types
     * @return this
     */
    public QueryParamBuilder type ( String... types ) {
        this.params.setTypes(types);
        return this;
    }


    /**
     * @param query
     * @return this
     */
    public QueryParamBuilder query ( QueryBuilder query ) {
        this.params.setCustomQuery(query);
        return this;
    }


    /**
     * @param from
     * @return this
     */
    public QueryParamBuilder from ( DateTime from ) {
        this.params.setStartDate(from);
        return this;
    }


    /**
     * @param to
     * @return this
     */
    public QueryParamBuilder to ( DateTime to ) {
        this.params.setEndDate(to);
        return this;
    }


    /**
     * 
     * @param offset
     * @return this
     */
    public QueryParamBuilder pageFrom ( int offset ) {
        this.params.setPageFrom(offset);
        return this;
    }


    /**
     * 
     * @param pageSize
     * @return this
     */
    public QueryParamBuilder pageSize ( int pageSize ) {
        this.params.setPageSize(pageSize);
        return this;
    }


    /**
     * @return this
     */
    public QueryParamBuilder filterDuplicates () {
        this.params.setIncludeDuplicates(false);
        return this;
    }
}
