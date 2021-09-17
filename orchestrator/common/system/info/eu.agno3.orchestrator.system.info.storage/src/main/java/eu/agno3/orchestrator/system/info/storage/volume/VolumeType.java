/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.volume;


import java.util.HashMap;
import java.util.Map;


/**
 * @author mbechler
 * 
 */
public enum VolumeType {
    /**
     * 
     */
    UNKNOWN,
    /**
     * 
     */
    UNPARTITIONED,
    /**
     * 
     */
    EMPTY,
    /**
     * 
     */
    LINUX_SWAP,
    /**
     * 
     */
    LINUX,
    /**
     * 
     */
    LVM_PV,
    /**
     * 
     */
    GPT,
    /**
     * 
     */
    EFI,
    /**
     * 
     */
    LINUX_RAID;

    private static Map<String, VolumeType> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("0x00", EMPTY); //$NON-NLS-1$
        TYPE_MAP.put("0x82", LINUX_SWAP); //$NON-NLS-1$
        TYPE_MAP.put("0x83", LINUX); //$NON-NLS-1$
        TYPE_MAP.put("0x8e", LVM_PV); //$NON-NLS-1$
        TYPE_MAP.put("0xef", EFI); //$NON-NLS-1$
        TYPE_MAP.put("0xee", GPT); //$NON-NLS-1$
        TYPE_MAP.put("0xfd", LINUX_RAID); //$NON-NLS-1$
    }


    /**
     * 
     * @param type
     * @return the parition type matching the type code
     */
    public static VolumeType fromTypeString ( String type ) {
        if ( type.isEmpty() ) {
            return UNPARTITIONED;
        }

        VolumeType t = TYPE_MAP.get(type);

        if ( t == null ) {
            return UNKNOWN;
        }

        return t;
    }

}
