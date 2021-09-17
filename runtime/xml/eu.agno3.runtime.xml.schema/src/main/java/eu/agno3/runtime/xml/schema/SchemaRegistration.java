/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema;


import java.io.IOException;
import java.io.InputStream;


/**
 * @author mbechler
 * 
 */
public interface SchemaRegistration {

    /**
     * @return the target namespace of the schema
     */
    String getTargetNamespace ();


    /**
     * 
     * @return the schema XSD source input stream
     * @throws IOException
     */
    InputStream getSchemaSource () throws IOException;
}
