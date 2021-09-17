/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * @author mbechler
 * 
 */
public enum FileSystemType {

    /**
     * 
     */
    UNKNOWN,
    /**
     * 
     */
    SWAP,
    /**
     * 
     */
    EXT2,
    /**
     * 
     */
    EXT3,
    /**
     * 
     */
    EXT4,
    /**
     * 
     */
    XFS,
    /**
     * 
     */
    REISERFS;

    private static final Map<String, FileSystemType> STRING_TO_TYPE = new HashMap<>();

    static {
        for ( FileSystemType t : EnumSet.complementOf(EnumSet.of(UNKNOWN)) ) {
            STRING_TO_TYPE.put(t.name().toLowerCase(), t);
        }
    }


    /**
     * @param spec
     * @return the appropriate type if available or UNKNOWN
     */
    public static final FileSystemType fromString ( String spec ) {
        FileSystemType t = STRING_TO_TYPE.get(spec);

        if ( t == null ) {
            return UNKNOWN;
        }
        return t;
    }

}
