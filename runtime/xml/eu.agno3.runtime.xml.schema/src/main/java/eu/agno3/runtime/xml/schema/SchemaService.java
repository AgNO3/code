/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema;


import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * @author mbechler
 * 
 */
public interface SchemaService {

    /**
     * @return a configured schema factory
     */
    SchemaFactory getSchemaFactory ();


    /**
     * @param clazz
     * @return the schema for the given class
     * @throws SchemaResolverException
     */
    Schema getSchemaForClass ( Class<?> clazz ) throws SchemaResolverException;


    /**
     * @param clazz
     * @return the schema document source for the given class
     *         throws SchemaResolverException
     * @throws SchemaResolverException
     */
    Document getSchemaDocumentForClass ( Class<?> clazz ) throws SchemaResolverException;


    /**
     * @param namespace
     * @return the schema for the namespace
     * @throws SchemaResolverException
     */
    Schema getSchemaForNamespace ( String namespace ) throws SchemaResolverException;


    /**
     * @param namespace
     * @return the schema document source for the given namespace
     * @throws SchemaResolverException
     */
    InputStream getSchemaSourceForNamespace ( String namespace ) throws SchemaResolverException;


    /**
     * @param namespace
     * @return the schema document for the given namespace
     * @throws SchemaResolverException
     */
    Document getSchemaDocumentForNamespace ( String namespace ) throws SchemaResolverException;


    /**
     * @param ns
     * @return whether a schema is registered for this namespace
     */
    boolean hasSchemaFor ( String ns );


    /**
     * @return the set of registered namespaces
     */
    Set<String> getRegisteredNamespaces ();


    /**
     * @param sources
     * @return a schema from the source documents
     * @throws SAXException
     */
    Schema fromSources ( Map<String, DOMSource> sources ) throws SAXException;

}
