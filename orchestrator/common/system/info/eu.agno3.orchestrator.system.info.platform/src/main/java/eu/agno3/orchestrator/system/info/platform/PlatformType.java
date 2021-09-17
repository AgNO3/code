/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


/**
 * @author mbechler
 * 
 */
public enum PlatformType {
    /**
     * Could not determine platform type reliably
     */
    UNKNOWN,

    /**
     * Bare metal machine
     */
    PHYSICAL,

    /**
     * VMWARE hypervisor
     */
    VMWARE
}
