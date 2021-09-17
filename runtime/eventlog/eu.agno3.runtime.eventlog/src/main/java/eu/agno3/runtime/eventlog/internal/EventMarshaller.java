/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;


/**
 * @author mbechler
 *
 */
public class EventMarshaller {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final JsonFactory JF;


    static {
        OM.registerModule(new JodaModule());
        JF = new JsonFactory(OM);
    }


    /**
     * @return the om
     */
    public static ObjectMapper getObjectMapper () {
        return OM;
    }


    /**
     * @return the jf
     */
    public static JsonFactory getJsonFactory () {
        return JF;
    }


    /**
     * @param event
     * @return the marshalled event
     * @throws IOException
     */
    public static byte[] marshallEvent ( Object event ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( JsonGenerator g = getJsonFactory().createGenerator(bos) ) {
            g.writeObject(event);
        }
        return bos.toByteArray();
    }

}
