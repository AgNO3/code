/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import java.io.IOException;
import java.util.Collection;


/**
 * @author mbechler
 *
 */
public interface MappingsBuilder {

    /**
     * 
     * @return mapping for all types
     */
    MappingBuilder defaults ();


    /**
     * 
     * @param type
     * @return mapping for specified types
     */
    MappingBuilder forType ( String type );


    /**
     * @return mappings
     * @throws IOException
     */
    Collection<Mapping> build () throws IOException;
}
