/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public interface DynamicMappingBuilder extends BaseMappingBuilder<DynamicMappingBuilder> {

    /**
     * 
     * @param field
     * @return self
     */
    public DynamicMappingBuilder matchField ( String field );


    /**
     * 
     * @param type
     * @return self
     */
    public DynamicMappingBuilder matchType ( String type );
}
