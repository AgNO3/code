/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 * 
 */
public class NetworkAddressXmlAdapter extends XmlAdapter<String, NetworkAddress> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public NetworkAddress unmarshal ( String v ) {
        if ( v == null ) {
            return null;
        }
        return AbstractIPAddress.parse(v);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( NetworkAddress v ) {
        if ( v == null ) {
            return null;
        }
        return v.toString();
    }

}
