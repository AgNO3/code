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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.info.platform.MemoryInformation;
import eu.agno3.orchestrator.system.info.platform.MemoryInformationImpl;


/**
 * @author mbechler
 * 
 */
public class MemoryInformationProvider {

    private static final String SWAP_FREE = "SwapFree"; //$NON-NLS-1$
    private static final String SWAP_TOTAL = "SwapTotal"; //$NON-NLS-1$
    private static final String CACHED = "Cached"; //$NON-NLS-1$
    private static final String BUFFERS = "Buffers"; //$NON-NLS-1$
    private static final String MEM_FREE = "MemFree"; //$NON-NLS-1$
    private static final String MEM_TOTAL = "MemTotal"; //$NON-NLS-1$
    private static final String MEMINFO_CHARSET = "ASCII"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(MemoryInformationProvider.class);
    private static final String PROC_MEMINFO = "/proc/meminfo"; //$NON-NLS-1$

    private static final Set<String> EXTRACT_PROPERTIES = new HashSet<>();
    static {
        EXTRACT_PROPERTIES.add(MEM_TOTAL);
        EXTRACT_PROPERTIES.add(MEM_FREE);
        EXTRACT_PROPERTIES.add(BUFFERS);
        EXTRACT_PROPERTIES.add(CACHED);
        EXTRACT_PROPERTIES.add(SWAP_TOTAL);
        EXTRACT_PROPERTIES.add(SWAP_FREE);
    }


    /**
     * @return the memory information
     */
    public MemoryInformation getMemoryInformation () {
        MemoryInformationImpl memInfo = new MemoryInformationImpl();
        Map<String, Long> properties = getProcMemProperties();

        if ( !properties.keySet().containsAll(EXTRACT_PROPERTIES) ) {
            log.warn("Failed to extract memory information"); //$NON-NLS-1$
            return null;
        }

        memInfo.setTotalPhysicalMemory(properties.get(MEM_TOTAL));
        memInfo.setCurrentPhysicalMemoryFree(properties.get(MEM_FREE));
        memInfo.setCurrentPhysicalMemoryUsedBuffers(properties.get(BUFFERS));
        memInfo.setCurrentPhysicalMemoryUsedCache(properties.get(CACHED));
        memInfo.setTotalSwapMemory(properties.get(SWAP_TOTAL));
        memInfo.setCurrentSwapMemoryFree(properties.get(SWAP_FREE));
        return memInfo;
    }


    /**
     * @return
     */
    private static Map<String, Long> getProcMemProperties () {
        Map<String, Long> res = new HashMap<>();
        Path procMeminfo = FileSystems.getDefault().getPath(PROC_MEMINFO);

        try ( FileChannel chan = FileChannel.open(procMeminfo, StandardOpenOption.READ);
              Reader br = Channels.newReader(chan, MEMINFO_CHARSET);
              BufferedReader r = new BufferedReader(br) ) {

            String line;
            while ( ( line = r.readLine() ) != null ) {
                line = line.trim();
                if ( line.isEmpty() || line.indexOf(':') < 0 ) {
                    continue;
                }

                String[] parts = line.split(Pattern.quote(":"), 2); //$NON-NLS-1$
                String fieldName = parts[ 0 ].trim();
                if ( !EXTRACT_PROPERTIES.contains(fieldName) ) {
                    continue;
                }

                long longValue = valueToLong(parts[ 1 ].trim());
                res.put(fieldName, longValue);
            }

        }
        catch ( IOException e ) {
            log.warn("Failed to read /proc/meminfo", e); //$NON-NLS-1$
        }

        return res;
    }


    /**
     * @param trim
     * @return
     */
    private static long valueToLong ( String val ) {

        if ( val.endsWith(" kB") ) { //$NON-NLS-1$
            return 1024 * Long.parseLong(val.substring(0, val.length() - 3));
        }

        throw new IllegalArgumentException("Unknown value format " + val); //$NON-NLS-1$
    }

}
