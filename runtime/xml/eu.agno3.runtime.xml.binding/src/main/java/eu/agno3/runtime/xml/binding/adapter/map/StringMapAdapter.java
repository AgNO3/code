/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter.map;


import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * @author mbechler
 *
 */
public class StringMapAdapter extends XmlAdapter<AdaptedMap, Map<String, String>> {

    @Override
    public Map<String, String> unmarshal ( AdaptedMap adaptedMap ) throws Exception {
        if ( adaptedMap == null ) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for ( Entry entry : adaptedMap.getEntries() ) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }


    @Override
    public AdaptedMap marshal ( Map<String, String> map ) throws Exception {
        if ( map == null ) {
            return null;
        }
        AdaptedMap adaptedMap = new AdaptedMap();
        for ( Map.Entry<String, String> mapEntry : map.entrySet() ) {
            Entry entry = new Entry();
            entry.setKey(mapEntry.getKey());
            entry.setValue(mapEntry.getValue());
            adaptedMap.getEntries().add(entry);
        }
        return adaptedMap;
    }

}
