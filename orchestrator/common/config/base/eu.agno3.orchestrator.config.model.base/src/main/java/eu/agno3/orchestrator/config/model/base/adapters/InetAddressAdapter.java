/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.09.2013 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.adapters;


import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 * 
 */
public class InetAddressAdapter extends XmlAdapter<String, InetAddress> {

    /**
     * 
     */
    public InetAddressAdapter () {}


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( InetAddress v ) {

        if ( v == null ) {
            return StringUtils.EMPTY;
        }

        return v.getHostName();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnknownHostException
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public InetAddress unmarshal ( String v ) throws UnknownHostException {
        if ( !v.isEmpty() ) {
            return InetAddress.getByName(v);
        }
        return null;
    }

}
