/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;


/**
 * @author mbechler
 *
 */
public class SourceMapping implements Mapping {

    private String source;
    private String targetType;


    /**
     * 
     * @param cb
     * @throws IOException
     */
    public SourceMapping ( XContentBuilder cb ) throws IOException {
        this(Mapping.DEFAULT, cb);
    }


    /**
     * @param targetType
     * @param cb
     * @throws IOException
     * 
     */
    public SourceMapping ( String targetType, XContentBuilder cb ) throws IOException {
        this.targetType = targetType;
        this.source = cb.string();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Mapping#getTargetType()
     */
    @Override
    public String getTargetType () {
        return this.targetType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.Mapping#toSource()
     */
    @Override
    public String toSource () {
        return this.source;
    }
}
