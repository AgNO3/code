/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web.agent;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.web.RuntimeConfiguration;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.base.units.file.contents.ContentsConfigurator;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.platform.MemoryInformation;


/**
 * @author mbechler
 *
 */
@Component ( service = RuntimeConfigUtil.class )
public class RuntimeConfigUtil {

    private static final Logger log = Logger.getLogger(RuntimeConfigUtil.class);
    private ExecutionConfigProperties execConfig;


    @Reference
    protected synchronized void setExecutionConfig ( ExecutionConfigProperties ec ) {
        this.execConfig = ec;
    }


    protected synchronized void unsetExecutionConfig ( ExecutionConfigProperties ec ) {
        if ( this.execConfig == ec ) {
            this.execConfig = null;
        }
    }


    /**
     * @param ctx
     * @param runtimeConfig
     * @throws InvalidParameterException
     * @throws UnitInitializationFailedException
     * @throws ServiceManagementException
     */
    public void configureDebugging ( RuntimeConfigContext<?, ?> ctx, RuntimeConfiguration runtimeConfig )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {
        PropertyConfigBuilder logCfg = PropertyConfigBuilder.get();
        for ( String dbg : runtimeConfig.getDebugPackages() ) {
            logCfg.p(dbg, "DEBUG"); //$NON-NLS-1$
        }
        for ( String dbg : runtimeConfig.getTracePackages() ) {
            logCfg.p(dbg, "TRACE"); //$NON-NLS-1$
        }
        ctx.instance("log", logCfg); //$NON-NLS-1$
    }


    /**
     * @param b
     * @param octx
     * @param rtc
     * @param minHeapMB
     * @param maxHeapMB
     * @param memoryShare
     * @param cfgFile
     * @param otherVmArgs
     * @param otherOpts
     * @param serviceGroup
     * @return whether service should be restarted
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws SystemInformationException
     */

    public boolean setupRuntimeConfig ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<?, ?> octx, RuntimeConfiguration rtc, int minHeapMB,
            int maxHeapMB, float memoryShare, Path cfgFile, List<String> otherVmArgs, Map<String, String> otherOpts, GroupPrincipal serviceGroup )
                    throws UnitInitializationFailedException, ServiceManagementException, SystemInformationException {
        return configureJava(b, octx, rtc, minHeapMB, maxHeapMB, memoryShare, cfgFile, otherVmArgs, otherOpts, serviceGroup);
    }


    /**
     * @param b
     * @param octx
     * @param rtc
     * @param minHeapMB
     * @param maxHeapMB
     * @param memoryShare
     * @param cfgFile
     * @param otherVmArgs
     * @param otherOpts
     * @param serviceGroup
     * @return
     * @throws SystemInformationException
     * @throws UnitInitializationFailedException
     */
    @SuppressWarnings ( "nls" )
    boolean configureJava ( JobBuilder b, ConfigurationJobContext<?, ?> octx, RuntimeConfiguration rtc, int minHeapMB, int maxHeapMB,
            float memoryShare, Path cfgFile, List<String> otherVmArgs, Map<String, String> otherOpts, GroupPrincipal serviceGroup )
                    throws SystemInformationException, UnitInitializationFailedException {
        int heapLimit = getMemoryLimit(octx, rtc, minHeapMB, maxHeapMB, memoryShare);
        List<String> vmargs = new ArrayList<>(otherVmArgs);
        vmargs.add(String.format("-Xms%dm", minHeapMB));
        vmargs.add(String.format("-Xmx%dm", heapLimit));
        vmargs.add("-XX:+UseG1GC");
        vmargs.add("-XX:+UseStringDeduplication");

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("VMOPTS=\"%s\"", StringUtils.join(vmargs, StringUtils.SPACE))); //$NON-NLS-1$
        sb.append(System.lineSeparator());

        for ( Entry<String, String> opt : otherOpts.entrySet() ) {
            sb.append(opt.getKey());
            sb.append('=');
            sb.append('"');
            sb.append(opt.getValue());
            sb.append('"');
            sb.append(System.lineSeparator());
        }

        byte[] configBytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
        byte[] oldBytes = null;
        try {
            oldBytes = Files.readAllBytes(PrefixUtil.resolvePrefix(this.execConfig, cfgFile));
            if ( Arrays.equals(oldBytes, configBytes) ) {
                return false;
            }
        }
        catch ( IOException e ) {
            log.debug("Failed to read current config", e);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Runtime config has changed");
            if ( oldBytes != null ) {
                log.debug("Old: " + new String(oldBytes, StandardCharsets.UTF_8));
            }
            log.debug("New: " + new String(configBytes, StandardCharsets.UTF_8));
        }

        ContentsConfigurator contents = b.add(Contents.class);

        contents.file(cfgFile). // $NON-NLS-1$
                content(configBytes).perms(FileSecurityUtils.getGroupReadFilePermissions());
        if ( serviceGroup != null ) {
            contents.group(serviceGroup);
        }

        return true;
    }


    /**
     * @param octx
     * @param rtc
     * @param minHeapMB
     * @param maxHeapMB
     * @param memoryShare
     * @return
     * @throws SystemInformationException
     */
    int getMemoryLimit ( ConfigurationJobContext<?, ?> octx, RuntimeConfiguration rtc, int minHeapMB, int maxHeapMB, float memoryShare )
            throws SystemInformationException {
        MemoryInformation memoryInfo = octx.platformInfo().getMemoryInformation();
        long physMemory = memoryInfo.getTotalPhysicalMemory() / 1024 / 1024;
        int heapLimit = minHeapMB;
        if ( rtc.getMemoryLimit() != null && rtc.getAutoMemoryLimit() != null && !rtc.getAutoMemoryLimit() ) {
            // memory limit is set manually
            if ( log.isDebugEnabled() ) {
                log.debug("Configured limit " + rtc.getMemoryLimit()); //$NON-NLS-1$
            }
            heapLimit = (int) ( rtc.getMemoryLimit() / 1024 / 1024 );
            if ( heapLimit <= 0 ) {
                heapLimit = minHeapMB;
            }
        }
        else {
            int assignMemory = Math.max(minHeapMB, (int) ( physMemory * memoryShare ));
            if ( log.isDebugEnabled() ) {
                log.debug("Assigned through share " + assignMemory); //$NON-NLS-1$
            }
            if ( maxHeapMB > 0 ) {
                assignMemory = Math.min(maxHeapMB, assignMemory);
            }
            heapLimit = assignMemory;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "System has a total of %d byte memory, minimum is %d maximum is %d share %.2f -> effective %d", //$NON-NLS-1$
                physMemory,
                minHeapMB,
                maxHeapMB,
                memoryShare,
                heapLimit));
        }
        return heapLimit;
    }

}
