/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


import java.util.HashMap;
import java.util.Map;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public enum RAIDLevel {

    RAID0, RAID1, RAID4, RAID5, RAID6, RAID10;

    private static final Map<String, RAIDLevel> STRING_TO_LEVEL = new HashMap<>();
    static {
        String[] levels = new String[] {
            "0", "1", "4", "5", "6", "10"
        };

        for ( String level : levels ) {
            RAIDLevel l = RAIDLevel.valueOf("RAID" + level);
            STRING_TO_LEVEL.put(level, l);
            STRING_TO_LEVEL.put("raid" + level, l);
        }
    }


    /**
     * @param spec
     *            a raid level specification (e.g. "0" or "raid0")
     * @return the raid level
     */
    public static final RAIDLevel fromString ( String spec ) {
        RAIDLevel l = STRING_TO_LEVEL.get(spec);
        if ( l == null ) {
            throw new IllegalArgumentException("Unsupported raid level " + spec);
        }
        return l;
    }

}
