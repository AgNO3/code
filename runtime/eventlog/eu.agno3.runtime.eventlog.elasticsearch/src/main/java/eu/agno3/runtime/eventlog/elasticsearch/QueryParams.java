/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch;


import java.util.Arrays;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class QueryParams {

    private DateTime startDate;
    private DateTime endDate;

    private boolean includeExpired = false;
    private boolean includeAcknowledged = false;

    private QueryBuilder customQuery;
    private String[] types;
    private List<SortBuilder<?>> sorting;
    private List<AbstractAggregationBuilder<?>> customAggregations;
    private boolean includeSuperseded = true;
    private boolean includeDuplicates = true;
    private Integer pageSize;
    private Integer pageFrom;


    /**
     * @return the startDate
     */
    public DateTime getStartDate () {
        return this.startDate;
    }


    /**
     * @param startDate
     *            the startDate to set
     */
    public void setStartDate ( DateTime startDate ) {
        this.startDate = startDate;
    }


    /**
     * @return the endDate
     */
    public DateTime getEndDate () {
        return this.endDate;
    }


    /**
     * @param endDate
     *            the endDate to set
     */
    public void setEndDate ( DateTime endDate ) {
        this.endDate = endDate;
    }


    /**
     * @return the includeExpired
     */
    public boolean isIncludeExpired () {
        return this.includeExpired;
    }


    /**
     * @param includeExpired
     *            the includeExpired to set
     */
    public void setIncludeExpired ( boolean includeExpired ) {
        this.includeExpired = includeExpired;
    }


    /**
     * @return the includeAcknowledged
     */
    public boolean isIncludeAcknowledged () {
        return this.includeAcknowledged;
    }


    /**
     * @param includeAcknowledged
     *            the includeAcknowledged to set
     */
    public void setIncludeAcknowledged ( boolean includeAcknowledged ) {
        this.includeAcknowledged = includeAcknowledged;
    }


    /**
     * @return the customQuery
     */
    public QueryBuilder getCustomQuery () {
        return this.customQuery;
    }


    /**
     * @param customQuery
     *            the customQuery to set
     */
    public void setCustomQuery ( QueryBuilder customQuery ) {
        this.customQuery = customQuery;
    }


    /**
     * @return the types
     */
    public String[] getTypes () {
        if ( this.types != null ) {
            return Arrays.copyOf(this.types, this.types.length);
        }
        return null;
    }


    /**
     * @param types
     */
    public void setTypes ( String[] types ) {
        if ( types != null ) {
            this.types = Arrays.copyOf(types, types.length);
        }
        else {
            this.types = null;
        }
    }


    /**
     * @return sorting
     */
    public List<SortBuilder<?>> getSorting () {
        return this.sorting;
    }


    /**
     * @param sorting
     *            the sorting to set
     */
    public void setSorting ( List<SortBuilder<?>> sorting ) {
        this.sorting = sorting;
    }


    /**
     * @return custom aggregations
     */
    public List<AbstractAggregationBuilder<?>> getCustomAggregations () {
        return this.customAggregations;
    }


    /**
     * @param customAggregations
     *            the customAggregations to set
     */
    public void setCustomAggregations ( List<AbstractAggregationBuilder<?>> customAggregations ) {
        this.customAggregations = customAggregations;
    }


    /**
     * @return includeSuperseded
     */
    public boolean isIncludeSuperseded () {
        return this.includeSuperseded;
    }


    /**
     * @param includeSuperseded
     *            the includeSuperseded to set
     */
    public void setIncludeSuperseded ( boolean includeSuperseded ) {
        this.includeSuperseded = includeSuperseded;
    }


    /**
     * 
     * @return includeDuplicates
     */
    public boolean isIncludeDuplicates () {
        return this.includeDuplicates;
    }


    /**
     * @param includeDuplicates
     *            the includeDuplicates to set
     */
    public void setIncludeDuplicates ( boolean includeDuplicates ) {
        this.includeDuplicates = includeDuplicates;
    }


    /**
     * @return page size
     */
    public Integer getPageSize () {
        return this.pageSize;
    }


    /**
     * @param pageSize
     *            the pageSize to set
     */
    public void setPageSize ( int pageSize ) {
        this.pageSize = pageSize;
    }


    /**
     * 
     * @return page start offset
     */
    public Integer getPageFrom () {
        return this.pageFrom;
    }


    /**
     * @param pageFrom
     *            the pageFrom to set
     */
    public void setPageFrom ( Integer pageFrom ) {
        this.pageFrom = pageFrom;
    }
}
