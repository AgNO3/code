/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 6, 2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.support.IndicesOptions;


/**
 * @author mbechler
 *
 */
public interface MappingManager {

    /**
     * 
     * @return a mappings builder instance
     */
    MappingsBuilder createBuilder ();


    /**
     * @param name
     * @param checkMapping
     * @param mappings
     * @return an index handle
     * @throws ElasticsearchMappingException
     */
    IndexHandle ensureIndexExists ( String name, boolean checkMapping, Collection<Mapping> mappings ) throws ElasticsearchMappingException;


    /**
     * 
     * @param name
     * @param checkMapping
     * @param mappings
     * @param mapping
     * @param indexSettings
     * @return an index handle
     * @throws ElasticsearchMappingException
     */
    IndexHandle ensureIndexExists ( String name, boolean checkMapping, Collection<Mapping> mappings, IndexSettings indexSettings )
            throws ElasticsearchMappingException;


    /**
     * @param names
     * @param targetMappings
     * @return migration status
     * @throws ElasticsearchMappingException
     */
    MappingMigrationStatus migrateMappings ( Collection<String> names, Collection<Mapping> targetMappings ) throws ElasticsearchMappingException;


    /**
     * @param names
     * @param targetMappings
     * @param indexSettings
     * @return migration status
     * @throws ElasticsearchMappingException
     */
    MappingMigrationStatus migrateMappings ( Collection<String> names, Collection<Mapping> targetMappings, IndexSettings indexSettings )
            throws ElasticsearchMappingException;


    /**
     * 
     * @param cl
     * @param name
     * @param targetMappings
     * @return migration status
     * @throws ElasticsearchMappingException
     */
    MappingMigrationStatus migrateMappings ( Client cl, Collection<String> name, Collection<Mapping> targetMappings )
            throws ElasticsearchMappingException;


    /**
     * @param cl
     * @param name
     * @param targetMappings
     * @param indexSettings
     * @return migration status
     * @throws ElasticsearchMappingException
     */
    MappingMigrationStatus migrateMappings ( Client cl, Collection<String> name, Collection<Mapping> targetMappings, IndexSettings indexSettings )
            throws ElasticsearchMappingException;


    /**
     * Remove indices and assoicated aliases
     * 
     * @param cl
     * @param expired
     * @param options
     * @throws ClientException
     */
    void removeIndices ( Client cl, Set<String> expired, IndicesOptions options ) throws ClientException;


    /**
     * @param cl
     * @param indices
     * @return index handles
     * @throws ClientException
     */
    List<IndexHandle> expandIndices ( Client cl, String... indices ) throws ClientException;


    /**
     * @param cl
     * @param indices
     * @return index handles
     * @throws ClientException
     */
    List<IndexHandle> expandIndices ( Client cl, Set<String> indices ) throws ClientException;


    /**
     * @param client
     * @param indices
     * @return index names for reading
     * @throws ClientException
     */
    String[] expandIndicesRead ( Client client, String[] indices ) throws ClientException;


    /**
     * @param client
     * @param indices
     * @return index names for writing
     * @throws ClientException
     */
    String[] expandIndicesWrite ( Client client, String[] indices ) throws ClientException;


    /**
     * @param client
     * @param indices
     * @return index names for management
     * @throws ClientException
     */
    String[] expandIndicesBacking ( Client client, String[] indices ) throws ClientException;


    /**
     * @param client
     * @param pattern
     * @return pattern to use for reading
     * @throws ClientException
     */
    String toReadPattern ( Client client, String pattern ) throws ClientException;


    /**
     * @param cl
     * @param idx
     * @return index name to use for reading
     * @throws ClientException
     */
    String expandIndexRead ( Client cl, String idx ) throws ClientException;


    /**
     * @param cl
     * @param idx
     * @return index name to use for writing
     * @throws ClientException
     */
    String expandIndexWrite ( Client cl, String idx ) throws ClientException;


    /**
     * 
     */
    void clearCache ();

}