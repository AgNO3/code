/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.09.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding;


import java.util.Collections;
import java.util.Set;

import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 * 
 */
public class DefaultUnmarshallingSession implements UnmarshallingSession {

    /**
     * 
     */
    public DefaultUnmarshallingSession () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.binding.UnmarshallingSession#isValidating()
     */
    @Override
    public boolean isValidating () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.binding.UnmarshallingSession#getAdapters()
     */
    @Override
    public Set<XmlAdapter<?, ?>> getAdapters () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.binding.UnmarshallingSession#getListener()
     */
    @Override
    public Listener getListener () {
        return null;
    }

}
