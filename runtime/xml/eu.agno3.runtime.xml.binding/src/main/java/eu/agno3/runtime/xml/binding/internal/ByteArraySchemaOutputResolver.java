/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2014 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
class ByteArraySchemaOutputResolver extends SchemaOutputResolver {

    private static final Logger log = Logger.getLogger(ByteArraySchemaOutputResolver.class);

    /**
     * 
     */
    private final Map<String, ByteArrayOutputStream> results = new HashMap<>();


    /**
     * @param results
     */
    ByteArraySchemaOutputResolver () {}


    @Override
    public Result createOutput ( String namespaceURI, String suggestedFileName ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Creating output for " + namespaceURI); //$NON-NLS-1$
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        this.results.put(namespaceURI, bos);
        Result r = new StreamResult(bos);
        r.setSystemId(namespaceURI);
        return r;
    }


    /**
     * @return the results
     */
    public Map<String, ByteArrayOutputStream> getResults () {
        return Collections.unmodifiableMap(this.results);
    }
}