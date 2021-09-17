/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;


/**
 * @author mbechler
 *
 */
public class XmlRSAPrivateKeyAdapter extends XmlAdapter<String, RSAPrivateKey> {

    /**
     * {@inheritDoc}
     * 
     * @throws CertificateException
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public RSAPrivateKey unmarshal ( String v ) throws CertificateException {
        if ( v == null ) {
            return null;
        }

        return CryptoConvertUtil.convertRSAPrivateFromBytes(Base64.decodeBase64(v));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( RSAPrivateKey v ) {
        if ( v == null ) {
            return null;
        }

        return Base64.encodeBase64String(CryptoConvertUtil.convertRSAPrivateToBytes(v));
    }

}
