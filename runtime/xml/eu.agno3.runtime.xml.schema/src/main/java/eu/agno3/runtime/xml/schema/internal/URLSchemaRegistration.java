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

import eu.agno3.runtime.xml.schema.SchemaRegistration;


/**
 * @author mbechler
 * 
 */
public class URLSchemaRegistration implements SchemaRegistration {

    private String targetNamespace;
    private URL schemaSource;


    /**
     * @param targetNamespace
     * @param url
     */
    public URLSchemaRegistration ( String targetNamespace, URL url ) {
        this.targetNamespace = targetNamespace;
        this.schemaSource = url;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaRegistration#getTargetNamespace()
     */
    @Override
    public String getTargetNamespace () {
        return this.targetNamespace;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     * 
     * @see eu.agno3.runtime.xml.schema.SchemaRegistration#getSchemaSource()
     */
    @Override
    public InputStream getSchemaSource () throws IOException {
        return this.schemaSource.openStream();
    }

}
