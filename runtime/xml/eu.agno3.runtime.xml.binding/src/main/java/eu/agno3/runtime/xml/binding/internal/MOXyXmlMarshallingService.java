/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.util.log.LogOutputStream;
import eu.agno3.runtime.xml.DuplicatingXmlStreamWriter;
import eu.agno3.runtime.xml.XmlFormattingWriter;
import eu.agno3.runtime.xml.binding.DefaultUnmarshallingSession;
import eu.agno3.runtime.xml.binding.JAXBContextProvider;
import eu.agno3.runtime.xml.binding.MapAs;
import eu.agno3.runtime.xml.binding.UnmarshallingSession;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;
import eu.agno3.runtime.xml.schema.SchemaResolverException;
import eu.agno3.runtime.xml.schema.SchemaService;
import eu.agno3.runtime.xml.schema.SchemaValidationConfig;
import eu.agno3.runtime.xml.schema.SchemaValidationLevel;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    XmlMarshallingService.class,
}, property = {
    "provider=moxy"
} )
public class MOXyXmlMarshallingService implements XmlMarshallingService {

    /**
     * 
     */
    private static final String DOCUMENT_ENCODING = "UTF-8"; //$NON-NLS-1$
    private static final String FAILED_TO_UNMARSHALL_DOCUMENT = "Failed to unmarshall document:"; //$NON-NLS-1$
    private static final String FAILED_TO_MARSHALL_DOCUMENT = "Failed to marshall document:"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(MOXyXmlMarshallingService.class);
    private JAXBContextProvider contextProvider;

    private SchemaService schemaService;
    private SchemaValidationConfig validationConfig;


    @Reference
    protected synchronized void setJAXBContextProvider ( JAXBContextProvider provider ) {
        this.contextProvider = provider;
    }


    protected synchronized void unsetJAXBContextProvider ( JAXBContextProvider provider ) {
        if ( this.contextProvider == provider ) {
            this.contextProvider = null;
        }
    }


    @Reference
    protected synchronized void setSchemaService ( SchemaService service ) {
        this.schemaService = service;
    }


    protected synchronized void unsetSchemaService ( SchemaService service ) {
        if ( this.schemaService == service ) {
            this.schemaService = null;
        }
    }


    @Reference
    protected synchronized void setValidationConfig ( SchemaValidationConfig svc ) {
        this.validationConfig = svc;
    }


    protected synchronized void unsetValidationConfig ( SchemaValidationConfig svc ) {
        if ( this.validationConfig == svc ) {
            this.validationConfig = null;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    @Override
    public void marshall ( Object o, XMLStreamWriter w ) throws XMLBindingException {

        Class<?> clazz = o.getClass();
        XMLStreamWriter writer = w;

        if ( log.isDebugEnabled() ) {
            log.debug("Marshalling object of type " + clazz.getName()); //$NON-NLS-1$
            writer = makeDebugWriter(writer);
        }

        if ( o.getClass().isAnnotationPresent(MapAs.class) ) {
            clazz = clazz.getAnnotation(MapAs.class).value();
        }

        JAXBContext context = getJAXBContext();

        Schema s;
        try {
            s = this.schemaService.getSchemaForClass(clazz);
        }
        catch ( SchemaResolverException e ) {
            throw new XMLBindingException("No schema available for class " + clazz.getName(), e); //$NON-NLS-1$
        }

        Marshaller m = createMarshaller(context, s);

        try {
            m.marshal(o, writer);
        }
        catch ( JAXBException e ) {
            getLog().warn(FAILED_TO_MARSHALL_DOCUMENT, e);
            throw new XMLBindingException(FAILED_TO_MARSHALL_DOCUMENT, e);
        }
    }


    @SuppressWarnings ( "resource" )
    private static XMLStreamWriter makeDebugWriter ( XMLStreamWriter writer ) {
        try {
            String logCharset = DOCUMENT_ENCODING;
            return new DuplicatingXmlStreamWriter(writer, new XMLStreamWriter[] {
                new XmlFormattingWriter(
                    XMLOutputFactory.newInstance()
                            .createXMLStreamWriter(new LogOutputStream(log, Level.DEBUG, Charset.forName(logCharset)), logCharset))
            });
        }
        catch (
            XMLStreamException |
            FactoryConfigurationError e ) {
            log.trace("Failed to initialize debug stream:", e); //$NON-NLS-1$
            return writer;
        }
    }


    /**
     * @param context
     * @param s
     * @return
     * @throws XMLBindingException
     */
    private static Marshaller createMarshaller ( JAXBContext context, Schema s ) throws XMLBindingException {
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_ENCODING, DOCUMENT_ENCODING);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            m.setSchema(s);
            m.setEventHandler(new LoggingEventHandler());
            return m;
        }
        catch ( JAXBException e ) {
            throw new XMLBindingException("Failed to create object marshaller:", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws XMLBindingException
     */
    private JAXBContext getJAXBContext () throws XMLBindingException {
        JAXBContext context;
        try {
            context = this.contextProvider.getContext();
        }
        catch ( JAXBException e ) {
            throw new XMLBindingException("Failed to obtain JAXB context:", e); //$NON-NLS-1$
        }
        return context;
    }


    @Override
    public <T> T unmarshall ( Class<T> clazz, XMLStreamReader reader ) throws XMLBindingException {
        return this.unmarshall(clazz, reader, new DefaultUnmarshallingSession());
    }


    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> T unmarshall ( Class<T> clazz, XMLStreamReader reader, UnmarshallingSession session ) throws XMLBindingException {

        if ( log.isDebugEnabled() ) {
            log.debug("Unmarshalling XML to type " + clazz.getName()); //$NON-NLS-1$
        }

        JAXBContext context = getJAXBContext();

        Schema s = getSchemaForClass(clazz, session);
        Unmarshaller um = createUnmarshaller(session, context, s, reader);

        try {
            Object o = um.unmarshal(reader);

            if ( o instanceof JAXBElement ) {
                o = ( (JAXBElement<T>) o ).getValue();
            }

            if ( o == null ) {
                throw new XMLBindingException("Unmarshalled object is null"); //$NON-NLS-1$
            }

            return (T) o;
        }
        catch ( JAXBException e ) {
            getLog().warn(FAILED_TO_UNMARSHALL_DOCUMENT, e);
            throw new XMLBindingException(FAILED_TO_UNMARSHALL_DOCUMENT, e);
        }
    }


    /**
     * @param clazz
     * @param session
     * @return
     * @throws XMLBindingException
     */
    private <T> Schema getSchemaForClass ( Class<T> clazz, UnmarshallingSession session ) throws XMLBindingException {
        Schema s = null;
        try {
            if ( this.validationConfig.getLevel() != SchemaValidationLevel.OFF && session.isValidating() ) {
                s = this.schemaService.getSchemaForClass(clazz);
            }
        }
        catch ( SchemaResolverException e ) {
            throw new XMLBindingException("No schema registered for class " + clazz.getName(), e); //$NON-NLS-1$
        }
        return s;
    }


    /**
     * @param session
     * @param um
     * @param context
     * @param s
     * @param reader
     * @return
     * @throws XMLBindingException
     */
    private Unmarshaller createUnmarshaller ( UnmarshallingSession session, JAXBContext context, Schema s, XMLStreamReader reader )
            throws XMLBindingException {
        try {
            Unmarshaller um = context.createUnmarshaller();
            um.setEventHandler(new LoggingEventHandler(reader));

            if ( this.validationConfig.getLevel() != SchemaValidationLevel.OFF && session.isValidating() ) {
                um.setSchema(s);
            }

            for ( XmlAdapter<?, ?> adapter : session.getAdapters() ) {
                um.setAdapter(adapter);
            }

            return um;
        }
        catch ( JAXBException e ) {
            throw new XMLBindingException("Failed to create unmarshaller:", e); //$NON-NLS-1$
        }
    }


    @Override
    public Map<String, ByteArrayOutputStream> generateSchemas () throws XMLBindingException {
        try {
            JAXBContext context = this.contextProvider.getContext();

            log.debug("Generating XML Schema for JAXB mapped classes"); //$NON-NLS-1$
            ByteArraySchemaOutputResolver resolver = new ByteArraySchemaOutputResolver();
            context.generateSchema(resolver);

            if ( log.isTraceEnabled() ) {
                for ( Entry<String, ByteArrayOutputStream> e : resolver.getResults().entrySet() ) {
                    log.trace("Schema for namespace " + e.getKey()); //$NON-NLS-1$
                    e.getValue().writeTo(LogOutputStream.makePrintStream(log, Level.TRACE));
                }
            }

            return resolver.getResults();
        }
        catch (
            JAXBException |
            IOException e ) {
            throw new XMLBindingException("Failed to generate schema:", e); //$NON-NLS-1$
        }

    }

    private static class LoggingEventHandler implements ValidationEventHandler {

        /**
         * 
         */
        private static final String LOCATION_FORMAT = "At line %d col %d (offset %d)"; //$NON-NLS-1$
        private XMLStreamReader reader;


        /**
         * 
         */
        public LoggingEventHandler () {}


        /**
         * 
         * @param r
         */
        public LoggingEventHandler ( XMLStreamReader r ) {
            this.reader = r;
        }


        /**
         * {@inheritDoc}
         * 
         * @see javax.xml.bind.ValidationEventHandler#handleEvent(javax.xml.bind.ValidationEvent)
         */
        @Override
        public boolean handleEvent ( ValidationEvent event ) {
            getLog().warn(event.toString());

            if ( this.reader == null ) {
                getLog().warn(
                    String.format(
                        LOCATION_FORMAT,
                        event.getLocator().getLineNumber(),
                        event.getLocator().getColumnNumber(),
                        event.getLocator().getOffset()));

                getLog().warn("At node: " + event.getLocator().getNode()); //$NON-NLS-1$
            }
            else {
                Location l = this.reader.getLocation();
                getLog().warn(String.format(LOCATION_FORMAT, l.getLineNumber(), l.getColumnNumber(), l.getCharacterOffset()));
            }

            if ( event.getLinkedException() != null ) {
                getLog().warn("Exception:", event.getLinkedException()); //$NON-NLS-1$
            }

            return true;
        }

    }
}
