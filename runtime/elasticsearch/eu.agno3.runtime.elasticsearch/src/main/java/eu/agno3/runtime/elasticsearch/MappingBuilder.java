/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 6, 2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import java.io.IOException;


/**
 * @author mbechler
 *
 */
public interface MappingBuilder {

    /**
     * @return parent
     */
    MappingsBuilder complete ();


    /**
     * @return this
     */
    MappingBuilder enableAllMapping ();


    /**
     * @return this
     */
    MappingBuilder disableAllMapping ();


    /**
     * @param id
     * @return dynamic mapping builder
     */
    DynamicMappingBuilder dynamic ( String id );


    /**
     * @param field
     * @return field mapping builder
     */
    FieldMappingBuilder field ( String field );


    /**
     * @return the mapping
     * @throws IOException
     */
    Mapping build () throws IOException;


    /**
     * @return type for this mapping
     */
    String getType ();

}
