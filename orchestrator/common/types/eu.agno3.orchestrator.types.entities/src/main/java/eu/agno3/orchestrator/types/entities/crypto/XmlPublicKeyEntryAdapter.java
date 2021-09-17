/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.types.entities.crypto;


import java.security.interfaces.RSAPublicKey;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.types.crypto.CryptoConvertUtil;


/**
 * @author mbechler
 *
 */
public class XmlPublicKeyEntryAdapter extends XmlAdapter<String, PublicKeyEntry> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( PublicKeyEntry v ) {
        if ( v == null || v.getPublicKey() == null || ! ( v.getPublicKey() instanceof RSAPublicKey ) ) {
            return null;
        }
        String keyData = Base64.encodeBase64String(CryptoConvertUtil.convertPublicToBytes(v.getPublicKey()));
        if ( !StringUtils.isBlank(v.getComment()) ) {
            return String.format("%s %s", keyData, v.getComment()); //$NON-NLS-1$
        }
        return keyData;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public PublicKeyEntry unmarshal ( String v ) {
        if ( v == null || v.isEmpty() ) {
            return new PublicKeyEntry();
        }

        String[] spl = StringUtils.split(v, " ", 2); //$NON-NLS-1$
        String comment = null;
        if ( spl.length == 2 ) {
            comment = spl[ 1 ];
        }
        return new PublicKeyEntry(CryptoConvertUtil.convertPublicFromBytes(Base64.decodeBase64(spl[ 0 ])), comment);
    }

}
