/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch;


import java.util.List;

import org.elasticsearch.action.search.SearchResponse;

import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
public interface ElasticsearchLogReader {

    /**
     * @param pb
     * @return the deduplicated result
     */
    List<MapEvent> findDeduplicated ( QueryParamBuilder pb );


    /**
     * 
     * @param pb
     * @return the deduplicated raw results
     */
    List<String> findDeduplicatedRaw ( QueryParamBuilder pb );


    /**
     * @param client
     * @param params
     * @return the search response
     */
    SearchResponse searchEvents ( QueryParamBuilder params );


    /**
     * @param params
     * @return the number of results
     */
    long countEvents ( QueryParamBuilder params );


    /**
     * @param pb
     * @return the found events
     */
    List<MapEvent> findAllEvents ( QueryParamBuilder pb );


    /**
     * @param ev
     */
    void acknowledge ( Event ev );


    /**
     * @param ev
     */
    void delete ( Event ev );


    /**
     * @return the number of days events will be available for non-range searches
     */
    int getOpenRetentionDays ();


    /**
     * @return the number of days events will be available (if searched for by date)
     */
    int getRetentionDays ();

}