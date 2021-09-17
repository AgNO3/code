/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml.adapter;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.agno3.runtime.messaging.msg.ResponseStatus;


/**
 * @author mbechler
 * 
 */
public class ResponseStatusXmlAdapter extends XmlAdapter<String, ResponseStatus> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( ResponseStatus status ) {

        if ( status == null ) {
            return null;
        }

        return status.name();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public ResponseStatus unmarshal ( String val ) {
        return ResponseStatus.valueOf(val);
    }

}
