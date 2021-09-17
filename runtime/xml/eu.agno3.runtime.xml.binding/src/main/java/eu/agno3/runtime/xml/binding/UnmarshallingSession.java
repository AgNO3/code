/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.09.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding;


import java.util.Set;

import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 * 
 */
public interface UnmarshallingSession {

    /**
     * 
     * @return whether to perform schema validation
     */
    boolean isValidating ();


    /**
     * @return the adapter instances to use in this session
     */
    Set<XmlAdapter<?, ?>> getAdapters ();


    /**
     * 
     * @return the listener to use in this session, null if none
     */
    Listener getListener ();

}
