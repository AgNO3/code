/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;


/**
 * @author mbechler
 * 
 */
public class XmlDateTimeAdapter extends XmlAdapter<String, DateTime> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public DateTime unmarshal ( String v ) {
        if ( v == null ) {
            return null;
        }
        return new DateTime(v);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( DateTime v ) {
        if ( v == null ) {
            return null;
        }
        return v.toString();
    }

}
