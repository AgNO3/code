/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2016 by mbechler
 */
package eu.agno3.runtime.xml.binding;


import javax.xml.bind.JAXBContext;

import org.eclipse.persistence.oxm.XMLDescriptor;


/**
 * @author mbechler
 *
 */
public interface ModularContext {

    /**
     * 
     * @return context delegate, if set
     */
    JAXBContext getDelegate ();


    /**
     * @param cls
     * @return an descriptor for the given class
     */
    XMLDescriptor lookupDescriptor ( Class<?> cls );


    /**
     * Cleanup unnecessary memory references
     * 
     */
    void clearState ();

}