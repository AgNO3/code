/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public enum MappingStatus {

    /**
     * No changes
     */
    CURRENT,

    /**
     * Update required
     */
    NEEDS_UPDATE,

    /**
     * Can be only updated by reindexing
     */
    NEEDS_REINDEX
}
