/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema.internal;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.schema.SchemaResolverException;


/**
 * @author mbechler
 * 
 */
@Component ( service = URLSchemaRegistrationFactory.class )
public class URLSchemaRegistrationFactory {

    private static final String TARGET_NAMESPACE_ATTR = "targetNamespace"; //$NON-NLS-1$
    private static final String SCHEMA_ELEMENT = "schema"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(URLSchemaRegistrationFactory.class);

    private XmlParserFactory xmlParserFactory;


    @Reference
    protected synchronized void setXMLParserFactory ( XmlParserFactory factory ) {
        this.xmlParserFactory = factory;
    }


    protected synchronized void unsetXMLParserFactory ( XmlParserFactory factory ) {
        if ( this.xmlParserFactory == factory ) {
            this.xmlParserFactory = null;
        }
    }


    /**
     * @param url
     * @return an URLSchemaRegistration for the given URL
     * @throws SchemaResolverException
     */
    public URLSchemaRegistration createURLSchemaRegistration ( URL url ) throws SchemaResolverException {

        try ( InputStream is = url.openStream() ) {

            XMLStreamReader r = this.xmlParserFactory.createStreamReader(is);

            String targetNamespace = getTargetNamespace(url, r);

            if ( log.isDebugEnabled() ) {
                log.debug("Found targetNamespace " + targetNamespace); //$NON-NLS-1$
            }

            return new URLSchemaRegistration(targetNamespace, url);
        }
        catch (
            IOException |
            XMLParserConfigurationException e ) {
            log.error("Failed to read schema file:", e); //$NON-NLS-1$
            throw new SchemaResolverException(e);
        }

    }


    /**
     * @param url
     * @param r
     * @return
     * @throws XMLStreamException
     * @throws SchemaResolverException
     */
    private static String getTargetNamespace ( URL url, XMLStreamReader r ) throws SchemaResolverException {

        try {
            while ( r.hasNext() ) {
                if ( r.next() != XMLStreamConstants.START_ELEMENT || !isSchemaElement(r) ) {
                    continue;
                }

                String targetNamespace = r.getAttributeValue(StringUtils.EMPTY, TARGET_NAMESPACE_ATTR);
                if ( targetNamespace != null ) {
                    return targetNamespace;
                }
            }
        }
        catch ( XMLStreamException e ) {
            throw new SchemaResolverException("Failure parsing schema document:", e); //$NON-NLS-1$
        }

        throw new SchemaResolverException("Failed to determine targetNamespace of " + url); //$NON-NLS-1$
    }


    /**
     * @param r
     * @return
     * @throws XMLStreamException
     */
    private static boolean isSchemaElement ( XMLStreamReader r ) throws XMLStreamException {
        return r.getNamespaceURI().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI) && SCHEMA_ELEMENT.equals(r.getName().getLocalPart());
    }
}
