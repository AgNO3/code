/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import eu.agno3.runtime.elasticsearch.Mapping;
import eu.agno3.runtime.elasticsearch.MappingBuilder;
import eu.agno3.runtime.elasticsearch.MappingsBuilder;


/**
 * @author mbechler
 *
 */
public class MappingsBuilderImpl implements MappingsBuilder {

    private List<MappingBuilderImpl> mappings = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.MappingsBuilder#defaults()
     */
    @Override
    public MappingBuilder defaults () {
        MappingBuilderImpl mb = new MappingBuilderImpl(this);
        this.mappings.add(mb);
        return mb;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.MappingsBuilder#forType(java.lang.String)
     */
    @Override
    public MappingBuilder forType ( String type ) {
        MappingBuilderImpl mb = new MappingBuilderImpl(type, this);
        this.mappings.add(mb);
        return mb;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.elasticsearch.MappingsBuilder#build()
     */
    @Override
    public Collection<Mapping> build () throws IOException {
        List<Mapping> maps = new LinkedList<>();
        boolean foundDefault = false;
        for ( MappingBuilder mb : this.mappings ) {
            if ( mb.getType() == null ) {
                foundDefault = true;
            }
            maps.add(mb.build());
        }
        if ( !foundDefault ) {
            maps.add(0, new MappingBuilderImpl(this).build());
        }
        return maps;
    }
}
