/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter.localestr;


/**
 * 
 * @author mbechler
 *
 */
public class Entry {

    private String key;
    private String value;


    /**
     * @return the key
     */
    public String getKey () {
        return this.key;
    }


    /**
     * @param key
     *            the key to set
     */
    public void setKey ( String key ) {
        this.key = key;
    }


    /**
     * @return the value
     */
    public String getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( String value ) {
        this.value = value;
    }
}