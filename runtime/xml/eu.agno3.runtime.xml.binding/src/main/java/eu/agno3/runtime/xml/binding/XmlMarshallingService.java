/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding;


import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;


/**
 * @author mbechler
 * 
 */
public interface XmlMarshallingService {

    /**
     * @param o
     * @param writer
     * @throws XMLBindingException
     */
    void marshall ( Object o, XMLStreamWriter writer ) throws XMLBindingException;


    /**
     * @param clazz
     * @param reader
     * @return the unmarshalled object
     * @throws XMLBindingException
     */
    <T> T unmarshall ( Class<T> clazz, XMLStreamReader reader ) throws XMLBindingException;


    /**
     * @return a map of namespace to binary schema documents
     * @throws XMLBindingException
     * 
     */
    Map<String, ByteArrayOutputStream> generateSchemas () throws XMLBindingException;


    /**
     * @param clazz
     * @param reader
     * @param session
     * @return the unmarshalled object
     * @throws XMLBindingException
     */
    <T> T unmarshall ( Class<T> clazz, XMLStreamReader reader, UnmarshallingSession session ) throws XMLBindingException;

}
