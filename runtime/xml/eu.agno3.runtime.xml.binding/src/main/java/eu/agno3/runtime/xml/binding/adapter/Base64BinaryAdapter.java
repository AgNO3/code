/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public class Base64BinaryAdapter extends XmlAdapter<String, byte[]> {

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( byte[] b ) throws Exception {
        if ( b == null ) {
            return StringUtils.EMPTY;
        }
        return Base64.encodeBase64String(b);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public byte[] unmarshal ( String s ) throws Exception {
        if ( StringUtils.isBlank(s) ) {
            return new byte[0];
        }
        return Base64.decodeBase64(s);
    }

}
