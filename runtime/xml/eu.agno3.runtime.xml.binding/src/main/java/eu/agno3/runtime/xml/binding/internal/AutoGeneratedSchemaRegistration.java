/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import eu.agno3.runtime.xml.schema.SchemaRegistration;


/**
 * @author mbechler
 * 
 */
public class AutoGeneratedSchemaRegistration implements SchemaRegistration {

    private String targetNamespace;
    private byte[] content;


    /**
     * @param ns
     * @param byteArray
     */
    public AutoGeneratedSchemaRegistration ( String ns, byte[] byteArray ) {
        this.targetNamespace = ns;
        this.content = byteArray.clone();
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
     * @see eu.agno3.runtime.xml.schema.SchemaRegistration#getSchemaSource()
     */
    @Override
    public synchronized InputStream getSchemaSource () throws IOException {
        return new ByteArrayInputStream(this.content);
    }

}