/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema;


import java.io.InputStream;
import java.util.Set;

import org.w3c.dom.Document;


/**
 * @author mbechler
 * 
 */
public interface SchemaRegistry {

    /**
     * @param namespace
     * @return the schema source for the given namespace
     * @throws SchemaResolverException
     */
    InputStream getSchemaSource ( String namespace ) throws SchemaResolverException;


    /**
     * @param namespace
     * @return the schema document
     * @throws SchemaResolverException
     */
    Document getSchemaDocument ( String namespace ) throws SchemaResolverException;


    /**
     * @param ns
     * @return whether a sachema for this namespace is registered
     */
    boolean hasSchemaFor ( String ns );


    /**
     * @return the set of registered schemas
     */
    Set<String> getRegisteredNamespaces ();

}