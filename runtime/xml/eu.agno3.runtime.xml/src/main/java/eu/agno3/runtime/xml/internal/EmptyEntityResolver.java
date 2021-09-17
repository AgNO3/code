/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.internal;


import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.input.ClosedInputStream;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class EmptyEntityResolver implements XMLResolver {

    private static final Logger log = Logger.getLogger(EmptyEntityResolver.class);


    /**
     * 
     */
    public EmptyEntityResolver () {}


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.stream.XMLResolver#resolveEntity(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Object resolveEntity ( String publicID, String systemID, String baseURI, String namespace ) throws XMLStreamException {
        log.warn(String.format("XMLStreamReader trying to resolve external entity pubId=%s sysId=%s baseURI=%s ns=%s", //$NON-NLS-1$
            publicID,
            systemID,
            baseURI,
            namespace));
        return ClosedInputStream.CLOSED_INPUT_STREAM;
    }

}