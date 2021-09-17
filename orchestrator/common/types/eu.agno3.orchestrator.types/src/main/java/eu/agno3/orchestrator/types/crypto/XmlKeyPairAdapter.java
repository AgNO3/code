/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import java.security.KeyPair;
import java.security.cert.CertificateException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;


/**
 * @author mbechler
 *
 */
public class XmlKeyPairAdapter extends XmlAdapter<String, KeyPair> {

    /**
     * {@inheritDoc}
     * 
     * @throws CertificateException
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public KeyPair unmarshal ( String v ) throws CertificateException {
        if ( v == null ) {
            return null;
        }

        return CryptoConvertUtil.convertRSAKeyPairFromBytes(Base64.decodeBase64(v));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( KeyPair v ) {
        if ( v == null ) {
            return null;
        }

        return Base64.encodeBase64String(CryptoConvertUtil.convertRSAKeyPairToBytes(v));
    }

}
