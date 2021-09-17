/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import org.elasticsearch.common.xcontent.XContentType;


/**
 * @author mbechler
 *
 */
public interface IndexSettings {

    /**
     * 
     * @return index settings source
     */
    String toSource ();


    /**
     * @return source content type
     */
    XContentType getContentType ();
}
