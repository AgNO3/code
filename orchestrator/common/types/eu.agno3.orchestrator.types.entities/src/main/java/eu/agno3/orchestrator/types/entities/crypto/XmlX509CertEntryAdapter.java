/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.entities.crypto;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;

import eu.agno3.orchestrator.types.crypto.CryptoConvertUtil;


/**
 * @author mbechler
 *
 */
public class XmlX509CertEntryAdapter extends XmlAdapter<String, X509CertEntry> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public X509CertEntry unmarshal ( String v ) {

        if ( v == null ) {
            return new X509CertEntry();
        }

        return new X509CertEntry(CryptoConvertUtil.convertX509CertificateFromBytes(Base64.decodeBase64(v)));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( X509CertEntry v ) {
        if ( v == null || v.getCertificate() == null ) {
            return null;
        }
        return Base64.encodeBase64String(CryptoConvertUtil.convertX509CertificateToBytes(v.getCertificate()));
    }
}
