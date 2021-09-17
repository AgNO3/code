/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import eu.agno3.runtime.jmx.MBean;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public interface NodeStatisticMXBean extends MBean {

    String getClusterStatus ();


    String getClusterName ();


    int getNodeCount ();


    int getFailedNodeCount ();


    int getNodeCountMaster ();


    int getNodeCountData ();


    int getNodeCountIngest ();


    long getSegmentBitsetMemoryBytes ();


    long getSegmentTermVerctorsMemoryBytes ();


    long getSegmentStoredFieldsMemoryBytes ();


    long getSegmentNormsMemoryBytes ();


    long getSegmentMemoryBytes ();


    long getSegmentDocValuesMemoryBytes ();


    long getSegmentTermMemoryBytes ();


    long getSegmentVersionMapMemoryBytes ();


    long getSegmentIndexWriterMemoryBytes ();


    long getSegmentCountMemoryBytes ();


    long getSegmentCount ();


    long getCompletionSizeBytes ();


    long getQueryCacheSizeBytes ();


    long getQueryCacheTotalCount ();


    long getQueryCacheMissCount ();


    long getQueryCacheHitCount ();


    long getQueryCacheEvictions ();


    long getQueryCacheCount ();


    long getQueryCacheSize ();


    long getFieldDataEvicitions ();


    long getFieldDataSizeBytes ();


    long getStoreSizeBytes ();


    long getDocumentDeletedCount ();


    long getDocumentCount ();


    int getShardPrimaryCount ();


    int getShardCount ();


    int getIndexCount ();


    long getRefreshTimeMs ();


    long getRefreshCount ();


    long getMergeCurrentSizeBytes ();


    long getMergeCurrentDocCount ();


    long getMergeCurrentCount ();


    long getMergeStoppedTime ();


    long getMergeThrottledTime ();


    long getMergeTotalTimeMs ();


    long getMergeDocCount ();


    long getMergeTotalSize ();


    long getMergeCount ();


    long getDeleteTimeMs ();


    long getDeleteCurrentCount ();


    long getDeleteCount ();


    long getIndexingTimeMs ();


    long getIndexingFailed ();


    long getIndexingCurrentCount ();


    long getIndexingCount ();


    long getGetMissingTimeMs ();


    long getGetMissingCount ();


    long getGetExistsTimeMs ();


    long getGetExistsCount ();


    long getGetTimeMs ();


    long getGetCount ();


    long getSearchScrollTimeMs ();


    long getSearchScrollCurrentCount ();


    long getSearchScrollCount ();


    long getSearchFetchTimeMs ();


    long getSearchFetchCurrentCount ();


    long getSearchFetchCount ();


    long getSearchQueryTimeMs ();


    long getSearchQueryCurrentCount ();


    long getSearchQueryCount ();

}
