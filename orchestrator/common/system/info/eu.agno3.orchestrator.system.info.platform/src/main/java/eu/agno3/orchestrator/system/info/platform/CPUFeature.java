/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author mbechler
 * 
 */
public enum CPUFeature {

    /**
     * Simultanious multi threading (e.g Intel Hyperthreading)
     */
    SMT,

    /**
     * Support for execute disabled memory
     */
    NON_EXEC,

    /**
     * Support for hardware virtualization support
     */
    VIRTUALIZATION,

    /**
     * Support for 64bit extensions
     */
    X86_64,

    /**
     * Phyiscal address extensions enabled
     */
    PAE,

    /**
     * Support for AES instructions
     */
    AES,

    /**
     * Running under a hypervisor
     */
    VIRTUALIZED,

    /**
     * Can run two threads at once
     */
    HT;

    private static final Map<String, CPUFeature> CPUINFO_TO_FEATURE = new HashMap<>();


    static {
        CPUINFO_TO_FEATURE.put("smt", SMT); //$NON-NLS-1$
        CPUINFO_TO_FEATURE.put("nx", NON_EXEC); //$NON-NLS-1$
        CPUINFO_TO_FEATURE.put("vmx", VIRTUALIZATION); //$NON-NLS-1$
        CPUINFO_TO_FEATURE.put("lm", X86_64); //$NON-NLS-1$
        CPUINFO_TO_FEATURE.put("pae", PAE); //$NON-NLS-1$
        CPUINFO_TO_FEATURE.put("aes", AES); //$NON-NLS-1$
        CPUINFO_TO_FEATURE.put("hypervisor", VIRTUALIZED); //$NON-NLS-1$
        CPUINFO_TO_FEATURE.put("ht", HT); //$NON-NLS-1$
    }


    /**
     * @param flag
     * @return a cpu feature for the flag, null if unknown
     */
    public static CPUFeature fromCpuInfoFlag ( String flag ) {
        return CPUINFO_TO_FEATURE.get(flag);
    }


    /**
     * @param flags
     *            a whitespace separated set of cpuinfo flags
     * @return a set of cpu features
     */
    public static Set<CPUFeature> fromCpuInfoFlags ( String flags ) {
        String[] fl = flags.split("\\s+"); //$NON-NLS-1$
        Set<CPUFeature> features = EnumSet.noneOf(CPUFeature.class);

        for ( String flag : fl ) {
            CPUFeature feature = fromCpuInfoFlag(flag);
            if ( feature != null ) {
                features.add(feature);
            }
        }

        return features;
    }
}
