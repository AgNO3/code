/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.adapter.localestr;

import java.util.ArrayList;
import java.util.List;



/**
 * 
 * @author mbechler
 *
 */
public class AdaptedMap {

    private List<Entry> entries = new ArrayList<>();


    /**
     * @return the entry
     */
    public List<Entry> getEntries () {
        return this.entries;
    }


    /**
     * @param entry
     *            the entry to set
     */
    public void setEntries ( List<Entry> entry ) {
        this.entries = entry;
    }
}