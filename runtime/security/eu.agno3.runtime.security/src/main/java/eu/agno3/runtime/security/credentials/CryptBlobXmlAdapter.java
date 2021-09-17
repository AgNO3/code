/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.agno3.runtime.crypto.wrap.CryptBlob;


/**
 * @author mbechler
 *
 */
public class CryptBlobXmlAdapter extends XmlAdapter<AdaptedCryptBlob, CryptBlob> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public CryptBlob unmarshal ( AdaptedCryptBlob v ) throws Exception {
        if ( v == null ) {
            return null;
        }
        return v.toCryptBlob();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public AdaptedCryptBlob marshal ( CryptBlob v ) throws Exception {
        if ( v == null ) {
            return null;
        }
        return AdaptedCryptBlob.fromCryptBlob(v);
    }

}
