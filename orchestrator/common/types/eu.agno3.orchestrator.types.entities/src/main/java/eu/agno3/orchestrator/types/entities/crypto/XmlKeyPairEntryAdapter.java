/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.types.entities.crypto;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;

import eu.agno3.orchestrator.types.crypto.CryptoConvertUtil;


/**
 * @author mbechler
 *
 */
public class XmlKeyPairEntryAdapter extends XmlAdapter<String, KeyPairEntry> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( KeyPairEntry v ) {
        if ( v == null || v.getKeyPair() == null ) {
            return null;
        }
        return Base64.encodeBase64String(CryptoConvertUtil.convertRSAKeyPairToBytes(v.getKeyPair()));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public KeyPairEntry unmarshal ( String v ) {
        if ( v == null || v.isEmpty() ) {
            return new KeyPairEntry();
        }
        return new KeyPairEntry(CryptoConvertUtil.convertRSAKeyPairFromBytes(Base64.decodeBase64(v)));
    }

}
