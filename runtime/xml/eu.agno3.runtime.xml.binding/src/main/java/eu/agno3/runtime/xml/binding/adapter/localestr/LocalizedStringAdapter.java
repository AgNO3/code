/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter.localestr;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.agno3.runtime.xml.binding.adapter.LocaleAdapter;


/**
 * @author mbechler
 *
 */
public class LocalizedStringAdapter extends XmlAdapter<AdaptedMap, Map<Locale, String>> {

    private static final LocaleAdapter LOCALE_ADAPTER = new LocaleAdapter();


    @Override
    public Map<Locale, String> unmarshal ( AdaptedMap adaptedMap ) throws Exception {
        if ( adaptedMap == null ) {
            return null;
        }
        Map<Locale, String> map = new HashMap<>();
        for ( Entry entry : adaptedMap.getEntries() ) {
            map.put(LOCALE_ADAPTER.unmarshal(entry.getKey()), entry.getValue());
        }
        return map;
    }


    @Override
    public AdaptedMap marshal ( Map<Locale, String> map ) throws Exception {
        if ( map == null ) {
            return null;
        }
        AdaptedMap adaptedMap = new AdaptedMap();
        for ( Map.Entry<Locale, String> mapEntry : map.entrySet() ) {
            Entry entry = new Entry();
            entry.setKey(LOCALE_ADAPTER.marshal(mapEntry.getKey()));
            entry.setValue(mapEntry.getValue());
            adaptedMap.getEntries().add(entry);
        }
        return adaptedMap;
    }

}
