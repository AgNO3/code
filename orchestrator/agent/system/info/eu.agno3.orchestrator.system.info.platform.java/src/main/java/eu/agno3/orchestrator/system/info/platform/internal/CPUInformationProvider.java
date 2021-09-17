/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform.internal;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.info.platform.CPUCore;
import eu.agno3.orchestrator.system.info.platform.CPUCoreImpl;
import eu.agno3.orchestrator.system.info.platform.CPUFeature;
import eu.agno3.orchestrator.system.info.platform.CPUInformation;
import eu.agno3.orchestrator.system.info.platform.CPUInformationImpl;


/**
 * @author mbechler
 * 
 */
public class CPUInformationProvider {

    /**
     * 
     */
    private static final String CPUINFO_CHARSET = "ASCII"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(CPUInformationProvider.class);
    private static final String PROC_CPUINFO = "/proc/cpuinfo"; //$NON-NLS-1$
    private static final String PROC_LOADAVG = "/proc/loadavg"; //$NON-NLS-1$


    /**
     * @return the cpu information
     */
    public CPUInformation getCPUInformation () {
        CPUInformationImpl cpuinfo = new CPUInformationImpl();
        List<Map<String, String>> cpus = getCPUProperties();

        List<CPUCore> cores = new ArrayList<>();
        Set<Integer> physicalIndices = new HashSet<>();

        int nthreads = 0;

        for ( Map<String, String> cpuProps : cpus ) {
            CPUCoreImpl core = this.getCPUCoreInformation(cpuProps);
            if ( core.getFeatures().contains(CPUFeature.HT) ) {
                nthreads += 2;
            }
            else {
                nthreads += 1;
            }
            physicalIndices.add(core.getPhysicalIndex());
            cores.add(core);
        }

        cpuinfo.setCpuCores(cores);
        cpuinfo.setTotalCPUCount(physicalIndices.size());
        setLoads(cpuinfo, nthreads);
        return cpuinfo;
    }


    /**
     * @param cpuinfo
     * @param cores
     */
    private static void setLoads ( CPUInformationImpl cpuinfo, int cores ) {
        Path procCpuinfo = FileSystems.getDefault().getPath(PROC_LOADAVG);

        try ( FileChannel chan = FileChannel.open(procCpuinfo, StandardOpenOption.READ);
              Reader rb = Channels.newReader(chan, CPUINFO_CHARSET);
              BufferedReader r = new BufferedReader(rb) ) {
            String l = r.readLine();
            StringTokenizer st = new StringTokenizer(l);

            cpuinfo.setLoad1(Float.parseFloat(st.nextToken()) / cores);
            cpuinfo.setLoad5(Float.parseFloat(st.nextToken()) / cores);
            cpuinfo.setLoad15(Float.parseFloat(st.nextToken()) / cores);
        }
        catch (
            IOException |
            NoSuchElementException |
            IllegalArgumentException e ) {
            log.warn("Failed to fetch cpu load information", e); //$NON-NLS-1$
        }
    }


    /**
     * @param cpuProps
     * @return
     */
    protected CPUCoreImpl getCPUCoreInformation ( Map<String, String> cpuProps ) {
        CPUCoreImpl core = new CPUCoreImpl();

        core.setModel(trimCPUModel(cpuProps.get("model name"))); //$NON-NLS-1$

        String physicalId = cpuProps.get("physical id"); //$NON-NLS-1$
        if ( physicalId == null ) {
            physicalId = cpuProps.get("processor"); //$NON-NLS-1$
        }

        core.setPhysicalIndex(Integer.parseInt(physicalId));

        String coreId = cpuProps.get("core id");//$NON-NLS-1$
        if ( coreId != null ) {
            core.setCoreIndex(Integer.parseInt(coreId));
        }

        String mhzSpec = cpuProps.get("cpu MHz"); //$NON-NLS-1$
        float mhz = Float.parseFloat(mhzSpec);
        core.setMaximumFrequency(Math.round(mhz));

        String cacheSpec = cpuProps.get("cache size"); //$NON-NLS-1$
        if ( cacheSpec != null ) {
            core.setCacheSize(parseCacheSize(cacheSpec));
        }

        core.setFeatures(CPUFeature.fromCpuInfoFlags(cpuProps.get("flags"))); //$NON-NLS-1$
        return core;
    }


    /**
     * @param string
     * @return
     */
    private static String trimCPUModel ( String string ) {
        return string.replaceAll(
            "\\s+", //$NON-NLS-1$
            " "); //$NON-NLS-1$
    }


    /**
     * @param cacheSpec
     * @return
     */
    private static int parseCacheSize ( String cacheSpec ) {
        if ( cacheSpec.endsWith(" KB") ) { //$NON-NLS-1$
            return 1024 * Integer.parseInt(cacheSpec.substring(0, cacheSpec.length() - 3));
        }

        return Integer.parseInt(cacheSpec);
    }


    /**
     * @return
     */
    private static List<Map<String, String>> getCPUProperties () {
        List<Map<String, String>> res = new ArrayList<>();
        Path procCpuinfo = FileSystems.getDefault().getPath(PROC_CPUINFO);

        try ( FileChannel chan = FileChannel.open(procCpuinfo, StandardOpenOption.READ);
              Reader rb = Channels.newReader(chan, CPUINFO_CHARSET);
              BufferedReader r = new BufferedReader(rb) ) {

            Map<String, String> cpuProps = new HashMap<>();
            String line;
            while ( ( line = r.readLine() ) != null ) {
                line = line.trim();

                if ( line.isEmpty() ) {
                    // each cpu section is terminated by an empty line
                    res.add(new HashMap<>(cpuProps));
                    cpuProps.clear();
                }

                if ( line.indexOf(':') < 0 ) {
                    continue;
                }

                String[] parts = line.split(Pattern.quote(":"), 2); //$NON-NLS-1$
                String property = parts[ 0 ].trim();
                String value = parts[ 1 ].trim();
                cpuProps.put(property, value);
            }

        }
        catch ( IOException e ) {
            log.warn("Failed to fetch cpu information", e); //$NON-NLS-1$
        }

        return res;
    }

}
