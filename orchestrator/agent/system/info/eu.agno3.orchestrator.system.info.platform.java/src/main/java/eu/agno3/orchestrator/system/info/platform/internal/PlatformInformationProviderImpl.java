/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.connector.AgentConfiguration;
import eu.agno3.orchestrator.system.info.platform.CPUFeature;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformationImpl;
import eu.agno3.orchestrator.system.info.platform.PlatformInformationProvider;
import eu.agno3.orchestrator.system.info.platform.PlatformType;


/**
 * @author mbechler
 * 
 */
@Component ( service = PlatformInformationProvider.class )
public class PlatformInformationProviderImpl implements PlatformInformationProvider {

    private CPUInformationProvider cpuInfoProvider = new CPUInformationProvider();
    private MemoryInformationProvider memoryInfoProvider = new MemoryInformationProvider();
    private AgentConfiguration agentConfiguration;


    /**
     * 
     * @param ac
     */
    @Reference
    public synchronized void setAgentConfiguration ( AgentConfiguration ac ) {
        this.agentConfiguration = ac;
    }


    /**
     * 
     * @param ac
     */
    public synchronized void unsetAgentConfiguration ( AgentConfiguration ac ) {
        if ( this.agentConfiguration == ac ) {
            this.agentConfiguration = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.PlatformInformationProvider#getInformation()
     */
    @Override
    public PlatformInformation getInformation () {
        PlatformInformationImpl info = new PlatformInformationImpl();
        info.setCpuInformation(this.cpuInfoProvider.getCPUInformation());
        info.setMemoryInformation(this.memoryInfoProvider.getMemoryInformation());
        info.setPlatformType(this.guessPlatformType(info));
        info.setAgentId(this.agentConfiguration.getAgentId());
        return info;
    }


    /**
     * @param info
     * @return
     */
    protected PlatformType guessPlatformType ( PlatformInformation info ) {
        if ( info.getCpuInformation().getCpuCores().get(0).getFeatures().contains(CPUFeature.VIRTUALIZED) ) {
            return PlatformType.VMWARE;
        }
        return PlatformType.PHYSICAL;
    }

}
