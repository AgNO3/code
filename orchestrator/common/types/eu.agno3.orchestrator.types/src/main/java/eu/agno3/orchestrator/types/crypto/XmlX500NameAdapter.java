/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.12.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.RFC4519Style;


/**
 * @author mbechler
 *
 */
public class XmlX500NameAdapter extends XmlAdapter<String, X500Name> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( X500Name n ) throws Exception {
        if ( n == null ) {
            return null;
        }

        return RFC4519Style.INSTANCE.toString(n);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public X500Name unmarshal ( String v ) throws Exception {
        if ( v == null ) {
            return null;
        }

        return new X500Name(RFC4519Style.INSTANCE.fromString(v));
    }

}
