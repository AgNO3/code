/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public class XmlDurationAdapter extends XmlAdapter<Long, Duration> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public Long marshal ( Duration p ) {
        if ( p == null ) {
            return null;
        }
        return p.getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Duration unmarshal ( Long ms ) {
        if ( ms == null ) {
            return null;
        }
        return new Duration((long) ms);
    }

}
