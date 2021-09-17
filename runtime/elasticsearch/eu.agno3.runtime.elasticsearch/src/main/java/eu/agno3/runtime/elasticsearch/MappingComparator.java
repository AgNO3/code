/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import java.io.IOException;

import org.elasticsearch.cluster.metadata.MappingMetaData;


/**
 * @author mbechler
 *
 */
public interface MappingComparator {

    /**
     * @param src
     * @param tgt
     * @param targetDefaultMapping
     * @return mapping status
     * @throws IOException
     */
    MappingStatus needsUpdate ( MappingMetaData src, MappingMetaData tgt, MappingMetaData targetDefaultMapping ) throws IOException;

}
