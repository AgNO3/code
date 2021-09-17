/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter;


import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public class LocaleAdapter extends XmlAdapter<String, Locale> {

    @Override
    public String marshal ( Locale v ) {
        if ( v == null || Locale.ROOT == v ) {
            return null;
        }
        return v.toLanguageTag();
    }


    @Override
    public Locale unmarshal ( String v ) {
        if ( StringUtils.isBlank(v) || "und".equals(v) ) { //$NON-NLS-1$
            return Locale.ROOT;
        }
        return Locale.forLanguageTag(v);
    }

}
