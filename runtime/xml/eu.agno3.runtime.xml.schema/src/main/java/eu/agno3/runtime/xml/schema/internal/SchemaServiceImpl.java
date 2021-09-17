/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema.internal;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import eu.agno3.runtime.util.log.LogOutputStream;
import eu.agno3.runtime.xml.LoggingErrorHandler;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.schema.PackageToNamespaceResolver;
import eu.agno3.runtime.xml.schema.SchemaRegistration;
import eu.agno3.runtime.xml.schema.SchemaRegistry;
import eu.agno3.runtime.xml.schema.SchemaResolverException;
import eu.agno3.runtime.xml.schema.SchemaService;


/**
 * @author mbechler
 * 
 */
@Component ( service = SchemaService.class )
public class SchemaServiceImpl implements SchemaService {

    private static final Logger log = Logger.getLogger(SchemaServiceImpl.class);

    private Map<String, Schema> schemaCache = new HashMap<>();

    private PackageToNamespaceResolver namespaceResolver;
    private XmlParserFactory xmlParserFactory;
    private SchemaRegistry schemaRegistry;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setNamespaceResolver ( PackageToNamespaceResolver resolver ) {
        this.namespaceResolver = resolver;
    }


    protected synchronized void unsetNamespaceResolver ( PackageToNamespaceResolver resolver ) {
        if ( this.namespaceResolver == resolver ) {
            this.namespaceResolver = null;
        }
    }


    @Reference
    protected synchronized void setXMLParserFactory ( XmlParserFactory factory ) {
        this.xmlParserFactory = factory;
    }


    protected synchronized void unsetXMLParserFactory ( XmlParserFactory factory ) {
        if ( this.xmlParserFactory == factory ) {
            this.xmlParserFactory = null;
        }
    }


    @Reference
    protected synchronized void setSchemaRegistry ( SchemaRegistry reg ) {
        this.schemaRegistry = reg;
    }


    protected synchronized void unsetSchemaRegistry ( SchemaRegistry reg ) {
        if ( this.schemaRegistry == reg ) {
            this.schemaRegistry = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindSchemaRegistration ( SchemaRegistration reg ) {

    }


    protected synchronized void unbindSchemaRegistration ( SchemaRegistration reg ) {
        this.schemaCache.remove(reg.getTargetNamespace());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaService#getSchemaFactory()
     */
    @Override
    public SchemaFactory getSchemaFactory () {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            sf.setFeature("http://apache.org/xml/features/namespace-growth", true); //$NON-NLS-1$
            sf.setFeature("http://apache.org/xml/features/internal/tolerate-duplicates", true); //$NON-NLS-1$
            //sf.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true); //$NON-NLS-1$
        }
        catch (
            SAXNotRecognizedException |
            SAXNotSupportedException e ) {
            log.warn("Failed to enable schema growth", e); //$NON-NLS-1$
        }
        sf.setResourceResolver(new ResourceResolverImpl(this));
        sf.setErrorHandler(new LoggingErrorHandler(Logger.getLogger(SchemaService.class)));
        return sf;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SchemaResolverException
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaService#getSchemaForClass(java.lang.Class)
     */
    @Override
    public Schema getSchemaForClass ( Class<?> clazz ) throws SchemaResolverException {
        String ns = null;
        if ( Proxy.isProxyClass(clazz) ) {
            ns = getNamespaceForProxy(clazz);
        }

        if ( ns == null && clazz.getPackage() != null ) {
            ns = this.getNamespaceResolver().getNamespaceForPackage(clazz.getPackage());
        }

        if ( ns == null ) {
            throw new SchemaResolverException("Failed to determine schema namespace for type " + clazz); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Returning schema namespace %s for object of type %s", ns, clazz.getName())); //$NON-NLS-1$
        }
        return this.getSchemaForNamespace(ns);
    }


    private String getNamespaceForProxy ( Class<?> clazz ) throws SchemaResolverException {
        String ns = null;
        for ( Class<?> intf : clazz.getInterfaces() ) {
            Package p = intf.getPackage();
            if ( p != null ) {
                ns = this.getNamespaceResolver().getNamespaceForPackage(p);
            }
        }
        return ns;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SchemaResolverException
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaService#getSchemaForNamespace(java.lang.String)
     */
    @Override
    public Schema getSchemaForNamespace ( String namespace ) throws SchemaResolverException {

        if ( !this.schemaRegistry.hasSchemaFor(namespace) ) {
            this.schemaCache.remove(namespace);
            throw new SchemaResolverException("No schema registered for namespace " + namespace); //$NON-NLS-1$
        }

        if ( this.schemaCache.containsKey(namespace) ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Returning cached schema for namespace " + namespace); //$NON-NLS-1$
            }
            return this.makeProxy(this.schemaCache.get(namespace));
        }

        return loadSchemaForNamespace(namespace);
    }


    /**
     * @param schema
     * @return
     */
    private Schema makeProxy ( Schema schema ) {
        return makeProxy(schema, new ResourceResolverImpl(this));
    }


    private static Schema makeProxy ( Schema schema, ResourceResolverImpl resolver ) {
        return new ResourceResolverSettingProxy(schema, resolver);
    }


    /**
     * @param namespace
     * @return
     * @throws SchemaResolverException
     */
    private Schema loadSchemaForNamespace ( String namespace ) throws SchemaResolverException {
        if ( log.isTraceEnabled() ) {
            log.trace("Loading schema for namespace " + namespace); //$NON-NLS-1$
        }

        try {
            DOMSource schema = new DOMSource(getSchemaDocumentForNamespace(namespace));
            Schema s = this.getSchemaFactory().newSchema(schema);
            this.schemaCache.put(namespace, s);
            return this.makeProxy(s);
        }
        catch ( SAXException e ) {
            log.warn("Schema failed to parse:"); //$NON-NLS-1$
            try ( InputStream is = this.getSchemaSourceForNamespace(namespace);
                  LogOutputStream lw = new LogOutputStream(log, Level.WARN, Charset.defaultCharset()) ) {
                int b = 0;
                while ( ( b = is.read() ) >= 0 ) {
                    lw.write(b);
                }

            }
            catch ( IOException e2 ) {
                log.warn("Failed to read schema:", e2); //$NON-NLS-1$
            }

            throw new SchemaResolverException("Failed to load schema for namespace " + namespace, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.xml.schema.SchemaService#getSchemaDocumentForClass(java.lang.Class)
     */
    @Override
    public Document getSchemaDocumentForClass ( Class<?> clazz ) throws SchemaResolverException {
        return this.getSchemaDocumentForNamespace(this.getNamespaceResolver().getNamespaceForPackage(clazz.getPackage()));
    }


    /**
     * @return
     * @throws SchemaResolverException
     */
    protected synchronized PackageToNamespaceResolver getNamespaceResolver () throws SchemaResolverException {
        if ( this.namespaceResolver == null ) {
            throw new SchemaResolverException("Cannot resolve class schemas while no PackageToNamespaceResolver is available"); //$NON-NLS-1$
        }

        return this.namespaceResolver;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaService#getSchemaSourceForNamespace(java.lang.String)
     */
    @Override
    public InputStream getSchemaSourceForNamespace ( String namespace ) throws SchemaResolverException {
        return this.schemaRegistry.getSchemaSource(namespace);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.xml.schema.SchemaService#getSchemaDocumentForNamespace(java.lang.String)
     */
    @Override
    public Document getSchemaDocumentForNamespace ( String namespace ) throws SchemaResolverException {
        return this.schemaRegistry.getSchemaDocument(namespace);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaService#hasSchemaFor(java.lang.String)
     */
    @Override
    public boolean hasSchemaFor ( String ns ) {
        return this.schemaRegistry.hasSchemaFor(ns);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaService#getRegisteredNamespaces()
     */
    @Override
    public Set<String> getRegisteredNamespaces () {
        return this.schemaRegistry.getRegisteredNamespaces();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaService#fromSources(java.util.Map)
     */
    @Override
    public Schema fromSources ( Map<String, DOMSource> sources ) throws SAXException {
        return makeProxy(this.getSchemaFactory().newSchema(sources.values().toArray(new DOMSource[] {})));
    }

}
