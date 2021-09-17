/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;

import eu.agno3.runtime.elasticsearch.FieldMappingBuilder;


/**
 * @author mbechler
 *
 */
public class FieldMappingBuilderImpl extends AbstractMappingBuilderImpl<FieldMappingBuilder> implements FieldMappingBuilder {

    private String field;


    /**
     * @param field
     */
    public FieldMappingBuilderImpl ( String field ) {
        this.field = field;
    }


    /**
     * @param mapping
     * @throws IOException
     */
    public void buildInternal ( XContentBuilder mapping ) throws IOException {
        mapping.startObject(this.field);
        buildMappingPropertiesInternal(mapping);
        mapping.endObject();
    }

}
