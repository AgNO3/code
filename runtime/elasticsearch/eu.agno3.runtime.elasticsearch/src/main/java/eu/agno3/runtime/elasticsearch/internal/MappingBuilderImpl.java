/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import eu.agno3.runtime.elasticsearch.DynamicMappingBuilder;
import eu.agno3.runtime.elasticsearch.FieldMappingBuilder;
import eu.agno3.runtime.elasticsearch.Mapping;
import eu.agno3.runtime.elasticsearch.MappingBuilder;
import eu.agno3.runtime.elasticsearch.MappingsBuilder;
import eu.agno3.runtime.elasticsearch.SourceMapping;


/**
 * @author mbechler
 *
 */
public class MappingBuilderImpl implements MappingBuilder {

    private MappingsBuilder parent;
    private String type;

    private boolean enableAllMapping = false;

    private List<DynamicMappingBuilderImpl> dynamicMappings = new ArrayList<>();
    private List<FieldMappingBuilderImpl> fieldMappings = new ArrayList<>();


    /**
     * @param type
     * @param parent
     */
    public MappingBuilderImpl ( String type, MappingsBuilder parent ) {
        this.type = type;
        this.parent = parent;
    }


    /**
     * @param parent
     */
    public MappingBuilderImpl ( MappingsBuilderImpl parent ) {
        this.parent = parent;
    }


    /**
     * @return the type
     */
    @Override
    public String getType () {
        return this.type;
    }


    /**
     * @return parent
     */
    @Override
    public MappingsBuilder complete () {
        return this.parent;
    }


    @Override
    public MappingBuilder enableAllMapping () {
        this.enableAllMapping = true;
        return this;
    }


    @Override
    public MappingBuilder disableAllMapping () {
        this.enableAllMapping = false;
        return this;
    }


    @Override
    public DynamicMappingBuilder dynamic ( String id ) {
        DynamicMappingBuilderImpl dm = new DynamicMappingBuilderImpl(id);
        this.dynamicMappings.add(dm);
        return dm;
    }


    @Override
    public FieldMappingBuilder field ( String field ) {
        FieldMappingBuilderImpl fm = new FieldMappingBuilderImpl(field);
        this.fieldMappings.add(fm);
        return fm;
    }


    @Override
    public Mapping build () throws IOException {
        try ( XContentBuilder mapping = XContentFactory.jsonBuilder() ) {
            buildInternal(mapping);

            if ( this.type == null ) {
                return new SourceMapping(mapping);
            }
            return new SourceMapping(this.type, mapping);
        }
    }


    /**
     * @param mapping
     * @throws IOException
     */
    public void buildInternal ( XContentBuilder mapping ) throws IOException {
        mapping.startObject().startObject(this.type != null ? this.type : Mapping.DEFAULT) //
                .startObject("_all") //$NON-NLS-1$
                .field("enabled", this.enableAllMapping) //$NON-NLS-1$
                .endObject();
        // static mappings

        if ( !this.fieldMappings.isEmpty() ) {
            mapping.startObject("properties"); //$NON-NLS-1$
            buildStaticMappings(mapping);
            mapping.endObject();
        }

        // dynamic mappings
        if ( !this.dynamicMappings.isEmpty() ) {
            mapping.startArray("dynamic_templates"); //$NON-NLS-1$
            buildDynamicMappings(mapping);
            mapping.endArray();
        }
        mapping.endObject().endObject();
    }


    /**
     * @param mapping
     * @throws IOException
     */
    void buildDynamicMappings ( XContentBuilder mapping ) throws IOException {
        for ( DynamicMappingBuilderImpl mb : this.dynamicMappings ) {
            mb.buildInternal(mapping);
        }

    }


    /**
     * @param mapping
     * @throws IOException
     */
    private void buildStaticMappings ( XContentBuilder mapping ) throws IOException {
        for ( FieldMappingBuilderImpl mb : this.fieldMappings ) {
            mb.buildInternal(mapping);
        }
    }
}
