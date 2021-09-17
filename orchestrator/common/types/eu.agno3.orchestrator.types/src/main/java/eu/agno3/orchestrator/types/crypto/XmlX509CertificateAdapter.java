/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import java.security.cert.X509Certificate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;


/**
 * @author mbechler
 *
 */
public class XmlX509CertificateAdapter extends XmlAdapter<String, X509Certificate> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public X509Certificate unmarshal ( String v ) {
        if ( v == null ) {
            return null;
        }
        return CryptoConvertUtil.convertX509CertificateFromBytes(Base64.decodeBase64(v));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( X509Certificate v ) {
        if ( v == null ) {
            return null;
        }

        return Base64.encodeBase64String(CryptoConvertUtil.convertX509CertificateToBytes(v));
    }

}
