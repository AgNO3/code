/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.components.localestr;


import java.util.Map;


/**
 * @author mbechler
 * @param <T>
 * @param <V>
 *
 */
public class MapValueWrapper <T, V> {

    private Map<T, V> map;
    private T key;


    /**
     * @param map
     * @param key
     * 
     */
    public MapValueWrapper ( Map<T, V> map, T key ) {
        this.map = map;
        this.key = key;
    }


    /**
     * 
     * @return the current value
     */
    public V getValue () {
        return this.map.get(this.key);

    }


    /**
     * 
     * @param v
     */
    public void setValue ( V v ) {
        this.map.put(this.key, v);
    }
}
