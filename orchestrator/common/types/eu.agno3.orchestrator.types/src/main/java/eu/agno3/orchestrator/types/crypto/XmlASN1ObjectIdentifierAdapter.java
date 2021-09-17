/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.12.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;


/**
 * @author mbechler
 *
 */
public class XmlASN1ObjectIdentifierAdapter extends XmlAdapter<String, ASN1ObjectIdentifier> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( ASN1ObjectIdentifier n ) throws Exception {
        if ( n == null ) {
            return null;
        }

        return n.getId();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public ASN1ObjectIdentifier unmarshal ( String v ) throws Exception {
        if ( v == null ) {
            return null;
        }

        return new ASN1ObjectIdentifier(v);
    }

}
