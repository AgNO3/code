/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema.console;


import java.io.InputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.xml.XmlFormattingWriter;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.schema.SchemaService;


/**
 * @author mbechler
 * 
 */
@Component ( service = CommandProvider.class )
public class XsdCommandProvider implements CommandProvider {

    private static final String SCHEMA_SERVICE_UNAVAILABLE = "Schema service unavailable"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(XsdCommandProvider.class);

    private SchemaService schemaService;
    private XmlParserFactory xmlParserFactory;


    @Reference ( policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setSchemaService ( SchemaService ss ) {
        this.schemaService = ss;
    }


    protected synchronized void unsetSchemaService ( SchemaService ss ) {
        if ( this.schemaService == ss ) {
            this.schemaService = null;
        }
    }


    @Reference
    protected synchronized void setXmlParserFactory ( XmlParserFactory factory ) {
        this.xmlParserFactory = factory;
    }


    protected synchronized void unsetXmlParserFactory ( XmlParserFactory factory ) {
        if ( this.xmlParserFactory == factory ) {
            this.xmlParserFactory = null;
        }
    }


    /**
     * @return the schemaService
     */
    synchronized SchemaService getSchemaService () {
        return this.schemaService;
    }


    /**
     * @return the xmlParserFactory
     */
    synchronized XmlParserFactory getXmlParserFactory () {
        return this.xmlParserFactory;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }

    /**
     * Get registered namespaces
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "xsd", name = "list", description = "Show registered XML schemas" )
    public class ListCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            if ( getSchemaService() == null ) {
                this.session.getConsole().println(SCHEMA_SERVICE_UNAVAILABLE);
                return null;
            }

            Ansi out = Ansi.ansi();

            for ( String namespace : getSchemaService().getRegisteredNamespaces() ) {
                out.bold().a(namespace).boldOff().newline();
            }

            this.session.getConsole().print(out.toString());

            return null;
        }
    }

    /**
     * Show schema source
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "xsd", name = "show", description = "Show schema source" )
    public class ShowCommand implements Action {

        /**
         * 
         */
        private static final String FAILED_TO_GET_SCHEMA_SOURCE = "Failed to get schema source: "; //$NON-NLS-1$

        @Option ( name = "--parsed", aliases = "-p", required = false )
        boolean parsed = false;

        @Argument ( index = 0, name = "namespace", required = true )
        String namespace;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            if ( getSchemaService() == null ) {
                this.session.getConsole().println(SCHEMA_SERVICE_UNAVAILABLE);
                return null;
            }

            try {
                if ( this.parsed ) {
                    XMLStreamWriter outWriter = new XmlFormattingWriter(XMLOutputFactory.newFactory()
                            .createXMLStreamWriter(this.session.getConsole()));
                    TransformerFactory.newInstance().newTransformer()
                            .transform(new DOMSource(getSchemaService().getSchemaDocumentForNamespace(this.namespace)), new StAXResult(outWriter));
                }
                else {
                    try ( InputStream in = getSchemaService().getSchemaSourceForNamespace(this.namespace) ) {
                        int b = 0;
                        while ( ( b = in.read() ) >= 0 ) {
                            this.session.getConsole().write(b);
                        }

                        this.session.getConsole().println();
                    }
                }
            }
            catch ( Exception e ) {
                getLog().warn(FAILED_TO_GET_SCHEMA_SOURCE, e);
                this.session.getConsole().println(FAILED_TO_GET_SCHEMA_SOURCE + e.getMessage());
            }

            return null;
        }
    }

}
