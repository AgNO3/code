/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;


/**
 * @author mbechler
 *
 */
public class SourceIndexSettings implements IndexSettings {

    private final String settings;
    private final XContentType type;


    /**
     * @param indexSettings
     * @throws IOException
     */
    public SourceIndexSettings ( XContentBuilder indexSettings ) throws IOException {
        this.settings = indexSettings.string();
        this.type = indexSettings.contentType();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexSettings#getContentType()
     */
    @Override
    public XContentType getContentType () {
        return this.type;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexSettings#toSource()
     */
    @Override
    public String toSource () {
        return this.settings;
    }

}
