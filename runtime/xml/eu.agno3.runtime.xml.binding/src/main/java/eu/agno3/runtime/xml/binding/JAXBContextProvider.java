/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


/**
 * @author mbechler
 * 
 */
public interface JAXBContextProvider {

    /**
     * @param classes
     * @return a JAXB context for the specified classes
     * @throws JAXBException
     */
    JAXBContext getContext ( Class<?>... classes ) throws JAXBException;
}
