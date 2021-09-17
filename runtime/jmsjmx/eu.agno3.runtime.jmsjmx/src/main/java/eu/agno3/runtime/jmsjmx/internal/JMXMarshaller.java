/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx.internal;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
public class JMXMarshaller {

    /**
     * @param os
     * @param marshallingService
     * @param params
     * @return signatures
     * @throws IOException
     */
    public static String[] marshallObjects ( ObjectOutputStream os, XmlMarshallingService marshallingService, Object[] params ) throws IOException {
        String[] signatures = new String[params.length];
        for ( int i = 0; i < params.length; i++ ) {
            signatures[ i ] = marshallObject(os, marshallingService, params[ i ]);
        }
        return signatures;
    }


    /**
     * @param os
     * @param marshallingService
     * @param object
     * @return the signature
     * @throws IOException
     */
    public static String marshallObject ( ObjectOutputStream os, XmlMarshallingService marshallingService, Object object ) throws IOException {
        os.writeObject(object);

        if ( object == null ) {
            return StringUtils.EMPTY;
        }
        return object.getClass().getName();
    }


    /**
     * @param is
     * @param signature
     * @param marshallingService
     * @param cl
     * @return the unmarshalled objects
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object[] unmarshallObjects ( ObjectInputStream is, String[] signature, XmlMarshallingService marshallingService, ClassLoader cl )
            throws ClassNotFoundException, IOException {
        Object[] objects = new Object[signature.length];

        for ( int i = 0; i < signature.length; i++ ) {
            objects[ i ] = unmarshallObject(is, signature[ i ], marshallingService, cl);
        }
        return objects;
    }


    /**
     * @param is
     * @param type
     * @param marshallingService
     * @param cl
     * @return the unmarshalled object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object unmarshallObject ( ObjectInputStream is, String type, XmlMarshallingService marshallingService, ClassLoader cl )
            throws ClassNotFoundException, IOException {
        return is.readObject();
    }
}
