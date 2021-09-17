/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.properties;


import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class StreamableDefaultDAVProperty <T> extends DefaultDavProperty<T> {

    /**
     * @param name
     * @param value
     * @param isInvisibleInAllprop
     */
    public StreamableDefaultDAVProperty ( DavPropertyName name, T value, boolean isInvisibleInAllprop ) {
        super(name, value, isInvisibleInAllprop);
    }


    /**
     * @param name
     * @param value
     */
    public StreamableDefaultDAVProperty ( DavPropertyName name, T value ) {
        super(name, value);
    }


    /**
     * @param name
     * @param value
     * @param namespace
     * @param isInvisibleInAllprop
     */
    public StreamableDefaultDAVProperty ( String name, T value, Namespace namespace, boolean isInvisibleInAllprop ) {
        super(name, value, namespace, isInvisibleInAllprop);
    }


    /**
     * @param name
     * @param value
     * @param namespace
     */
    public StreamableDefaultDAVProperty ( String name, T value, Namespace namespace ) {
        super(name, value, namespace);
    }

}
