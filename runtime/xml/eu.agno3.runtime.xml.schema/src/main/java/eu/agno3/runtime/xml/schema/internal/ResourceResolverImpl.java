/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema.internal;


import java.io.InputStream;

import javax.xml.XMLConstants;

import org.apache.commons.io.input.ClosedInputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import eu.agno3.runtime.xml.schema.LSInputImpl;
import eu.agno3.runtime.xml.schema.SchemaResolverException;
import eu.agno3.runtime.xml.schema.SchemaService;


/**
 * @author mbechler
 * 
 */
public class ResourceResolverImpl implements LSResourceResolver {

    private static final Logger log = Logger.getLogger(ResourceResolverImpl.class);

    private SchemaService schemaService;


    /**
     * @param schemaService
     */
    public ResourceResolverImpl ( SchemaService schemaService ) {
        this.schemaService = schemaService;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public LSInput resolveResource ( String type, String namespaceURI, String publicId, String systemId, String baseURI ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Request to resolve resource type=%s ns=%s pubId=%s sysId=%s baseURI=%s",//$NON-NLS-1$
                type,
                namespaceURI,
                publicId,
                systemId,
                baseURI));
        }

        if ( namespaceURI == null ) {
            return null;
        }

        if ( !type.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI) ) {
            log.error("Trying to resolve resource which is not an XML schema"); //$NON-NLS-1$
            return new LSInputImpl(ClosedInputStream.CLOSED_INPUT_STREAM);
        }

        InputStream inStream = ClosedInputStream.CLOSED_INPUT_STREAM;

        try {
            inStream = this.schemaService.getSchemaSourceForNamespace(namespaceURI);
            log.trace("Found schema"); //$NON-NLS-1$
        }
        catch ( SchemaResolverException e ) {
            if ( baseURI != null ) {
                // this might be a local embedded schema, let the implementation try to resolve this
                if ( log.isDebugEnabled() ) {
                    log.debug("Failed to look up schema with base url " + baseURI); //$NON-NLS-1$
                }
                return null;
            }
            log.error(String.format("Failed to look up schema for namespace '%s' with system id '%s''", namespaceURI, systemId), e); //$NON-NLS-1$
        }

        LSInputImpl lsin = new LSInputImpl(inStream);
        lsin.setSystemId(namespaceURI);
        return lsin;
    }

}
