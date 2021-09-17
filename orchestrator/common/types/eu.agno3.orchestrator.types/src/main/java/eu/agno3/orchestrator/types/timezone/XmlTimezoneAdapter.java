/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.timezone;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTimeZone;


/**
 * @author mbechler
 * 
 */
public class XmlTimezoneAdapter extends XmlAdapter<String, DateTimeZone> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public DateTimeZone unmarshal ( String v ) {
        if ( v == null ) {
            return null;
        }
        return DateTimeZone.forID(v);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( DateTimeZone v ) {
        if ( v == null ) {
            return null;
        }
        return v.getID();
    }

}
