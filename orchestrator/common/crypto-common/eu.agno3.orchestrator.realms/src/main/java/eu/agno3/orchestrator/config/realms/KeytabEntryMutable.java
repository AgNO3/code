/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.Set;

import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( KeytabEntry.class )
public interface KeytabEntryMutable extends KeytabEntry {

    /**
     * 
     * @param keyEntries
     */
    void setKeyImportEntries ( Set<KeyData> keyEntries );


    /**
     * 
     * @param keytabId
     */
    void setKeytabId ( String keytabId );

}
