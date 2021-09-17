/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.name;


import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 * 
 */
public class HostOrAddressXmlAdapter extends XmlAdapter<String, HostOrAddress> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public HostOrAddress unmarshal ( String v ) {
        if ( v == null ) {
            return null;
        }
        return HostOrAddress.fromString(v);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( HostOrAddress v ) {
        if ( v == null ) {
            return null;
        }
        return v.toString();
    }

}
