/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema.internal;


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.agno3.runtime.xml.schema.SchemaRegistration;
import eu.agno3.runtime.xml.schema.SchemaRegistry;
import eu.agno3.runtime.xml.schema.SchemaResolverException;


/**
 * @author mbechler
 * 
 */
@Component ( service = SchemaRegistry.class )
public class SchemaRegistryImpl implements SchemaRegistry {

    private static final Logger log = Logger.getLogger(SchemaRegistryImpl.class);

    private Map<String, SchemaRegistration> registrations = new HashedMap<>();


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindSchemaRegistration ( SchemaRegistration reg ) {
        String tns = reg.getTargetNamespace();

        if ( this.registrations.containsKey(tns) ) {
            log.warn("Multiple registrations for schema " + tns); //$NON-NLS-1$
            return;
        }

        this.registrations.put(tns, reg);
    }


    protected synchronized void unbindSchemaRegistration ( SchemaRegistration reg ) {
        String tns = reg.getTargetNamespace();

        if ( this.registrations.containsKey(tns) && reg == this.registrations.get(tns) ) {
            this.registrations.remove(tns);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaRegistry#getSchemaSource(java.lang.String)
     */
    @Override
    public synchronized InputStream getSchemaSource ( String namespace ) throws SchemaResolverException {
        if ( !this.registrations.containsKey(namespace) ) {
            throw new SchemaResolverException("No schema registered for namespace " + namespace); //$NON-NLS-1$
        }

        try {
            return this.registrations.get(namespace).getSchemaSource();
        }
        catch ( IOException e ) {
            throw new SchemaResolverException("Failed to open schema source location:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SchemaResolverException
     *
     * @see eu.agno3.runtime.xml.schema.SchemaRegistry#getSchemaDocument(java.lang.String)
     */
    @Override
    public Document getSchemaDocument ( String namespace ) throws SchemaResolverException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder().parse(getSchemaSource(namespace), namespace);
        }
        catch (
            SAXException |
            IOException |
            ParserConfigurationException |
            SchemaResolverException e ) {
            throw new SchemaResolverException("Failed to parse schema", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaRegistry#hasSchemaFor(java.lang.String)
     */
    @Override
    public boolean hasSchemaFor ( String ns ) {
        return this.registrations.containsKey(ns);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaRegistry#getRegisteredNamespaces()
     */
    @Override
    public Set<String> getRegisteredNamespaces () {
        return this.registrations.keySet();
    }

}
