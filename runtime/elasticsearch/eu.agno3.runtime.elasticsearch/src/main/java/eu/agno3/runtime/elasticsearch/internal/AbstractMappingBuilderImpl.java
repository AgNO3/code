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

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import eu.agno3.runtime.elasticsearch.BaseMappingBuilder;
import eu.agno3.runtime.elasticsearch.FieldMappingBuilder;


/**
 * @author mbechler
 * @param <T>
 *
 */
public abstract class AbstractMappingBuilderImpl <T extends BaseMappingBuilder<T>> implements BaseMappingBuilder<T> {

    private String type;
    private String analyzer;
    private Float boost;
    private boolean noIndex;
    private boolean noIncludeInAll;
    private boolean disable;
    private boolean disableNorms;
    private boolean disableDocValues;
    private int maxLength;

    private List<FieldMappingBuilderImpl> subFields = new ArrayList<>();


    @Override
    public T type ( String typeName ) {
        this.type = typeName;
        return self();
    }


    @Override
    public T analyzer ( String analyzerName ) {
        this.analyzer = analyzerName;
        return self();
    }


    @Override
    public T maxLength ( int length ) {
        this.maxLength = length;
        return self();
    }


    @Override
    public T boost ( float b ) {
        this.boost = b;
        return self();
    }


    @Override
    public T dontIndex () {
        this.noIndex = true;
        return self();
    }


    @Override
    public T dontIncludeInAll () {
        this.noIncludeInAll = true;
        return self();
    }


    @Override
    public T disable () {
        this.disable = true;
        return self();
    }


    @Override
    public T disableNorms () {
        this.disableNorms = true;
        return self();
    }


    @Override
    public T disableDocValues () {
        this.disableDocValues = true;
        return self();
    }


    @Override
    public FieldMappingBuilder subfield ( String name ) {
        FieldMappingBuilderImpl fm = new FieldMappingBuilderImpl(name);
        this.subFields.add(fm);
        return fm;
    }


    /**
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    protected T self () {
        return (T) this;
    }


    /**
     * @param mapping
     * @throws IOException
     */
    protected void buildMappingPropertiesInternal ( XContentBuilder mapping ) throws IOException {
        if ( !StringUtils.isBlank(this.type) ) {
            mapping.field(
                "type", //$NON-NLS-1$
                this.type); // $NON-NLS-1$
        }

        if ( !StringUtils.isBlank(this.analyzer) ) {
            mapping.field("analyzer", this.analyzer); //$NON-NLS-1$
        }

        if ( this.boost != null ) {
            mapping.field("boost", this.boost); //$NON-NLS-1$
        }

        if ( this.noIndex ) {
            mapping.field("index", false); //$NON-NLS-1$
        }

        if ( this.noIncludeInAll ) {
            mapping.field("include_in_all", false); //$NON-NLS-1$
        }

        if ( this.disable ) {
            mapping.field("enabled", false); //$NON-NLS-1$
        }

        if ( this.disableNorms ) {
            mapping.field("norms", false); //$NON-NLS-1$
        }

        if ( this.disableDocValues ) {
            mapping.field("doc_values", false); //$NON-NLS-1$
        }

        if ( this.maxLength > 0 ) {
            mapping.field("ignore_above", this.maxLength); //$NON-NLS-1$
        }

        if ( !this.subFields.isEmpty() ) {
            mapping.startObject("fields"); //$NON-NLS-1$
            for ( FieldMappingBuilderImpl fieldMappingBuilder : this.subFields ) {

                fieldMappingBuilder.buildInternal(mapping);
            }
            mapping.endObject();
        }
    }
}
