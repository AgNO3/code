/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.util.UUID;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( PlatformInformation.class )
public class PlatformInformationImpl implements PlatformInformation {

    /**
     * 
     */
    private static final long serialVersionUID = -4911450939725979569L;
    private CPUInformation cpuInfo;
    private MemoryInformation memInfo;
    private PlatformType platformType;
    private UUID agentId;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.PlatformInformation#getCpuInformation()
     */
    @Override
    public CPUInformation getCpuInformation () {
        return this.cpuInfo;
    }


    /**
     * @param cpuInfo
     *            the cpuInfo to set
     */
    public void setCpuInformation ( CPUInformation cpuInfo ) {
        this.cpuInfo = cpuInfo;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.PlatformInformation#getMemoryInformation()
     */
    @Override
    public MemoryInformation getMemoryInformation () {
        return this.memInfo;
    }


    /**
     * @param memInfo
     *            the memInfo to set
     */
    public void setMemoryInformation ( MemoryInformation memInfo ) {
        this.memInfo = memInfo;
    }


    /**
     * @return the platformType
     */
    @Override
    public PlatformType getPlatformType () {
        return this.platformType;
    }


    /**
     * @param platformType
     *            the platformType to set
     */
    public void setPlatformType ( PlatformType platformType ) {
        this.platformType = platformType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.platform.PlatformInformation#getAgentId()
     */
    @Override
    public UUID getAgentId () {
        return this.agentId;
    }


    /**
     * @param agentId
     *            the agentId to set
     */
    public void setAgentId ( UUID agentId ) {
        this.agentId = agentId;
    }
}
