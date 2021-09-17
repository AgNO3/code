/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import eu.agno3.runtime.elasticsearch.DynamicMappingBuilder;


/**
 * @author mbechler
 *
 */
public class DynamicMappingBuilderImpl extends AbstractMappingBuilderImpl<DynamicMappingBuilder> implements DynamicMappingBuilder {

    private String id;
    private String matchField;
    private String matchType;


    /**
     * @param id
     */
    public DynamicMappingBuilderImpl ( String id ) {
        this.id = id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.DynamicMappingBuilder#matchField(java.lang.String)
     */
    @Override
    public DynamicMappingBuilder matchField ( String field ) {
        this.matchField = field;
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.DynamicMappingBuilder#matchType(java.lang.String)
     */
    @Override
    public DynamicMappingBuilder matchType ( String type ) {
        this.matchType = type;
        return this;
    }


    /**
     * @param mapping
     * @throws IOException
     */
    public void buildInternal ( XContentBuilder mapping ) throws IOException {
        mapping.startObject().startObject(this.id); // $NON-NLS-1$
        mapping.field(
            "match", //$NON-NLS-1$
            this.matchField != null ? this.matchField : "*"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(this.matchType) ) {
            mapping.field(
                "match_mapping_type", //$NON-NLS-1$
                this.matchType);
        }

        mapping.startObject("mapping"); //$NON-NLS-1$
        buildMappingPropertiesInternal(mapping);
        mapping.endObject().endObject().endObject();
    }

}
