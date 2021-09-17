/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter;


import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 * 
 */
public class UUIDAdapter extends XmlAdapter<String, UUID> {

    private static final String NCNAME_COMPATABILITY_PREFIX = "u_"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( UUID uuid ) {

        if ( uuid == null ) {
            return null;
        }

        return NCNAME_COMPATABILITY_PREFIX + uuid;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public UUID unmarshal ( String string ) {
        String value = string;

        if ( value.startsWith(NCNAME_COMPATABILITY_PREFIX) ) {
            value = value.substring(NCNAME_COMPATABILITY_PREFIX.length());
        }

        return UUID.fromString(value);
    }

}
