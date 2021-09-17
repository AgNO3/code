/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.util.List;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( CPUInformation.class )
public class CPUInformationImpl implements CPUInformation {

    private int totalCPUCount;
    private List<CPUCore> cpuCores;
    private float load1;
    private float load15;
    private float load5;

    /**
     * 
     */
    private static final long serialVersionUID = -3796344250659624449L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUInformation#getTotalCPUCount()
     */
    @Override
    public int getTotalCPUCount () {
        return this.totalCPUCount;
    }


    /**
     * @param totalCPUCount
     *            the totalCPUCount to set
     */
    public void setTotalCPUCount ( int totalCPUCount ) {
        this.totalCPUCount = totalCPUCount;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUInformation#getTotalCoreCount()
     */
    @Override
    public int getTotalCoreCount () {
        if ( this.cpuCores == null ) {
            return 0;
        }
        return this.cpuCores.size();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUInformation#getCpuCores()
     */
    @Override
    public List<CPUCore> getCpuCores () {
        return this.cpuCores;
    }


    /**
     * @param cpuCores
     *            the cpuCores to set
     */
    public void setCpuCores ( List<CPUCore> cpuCores ) {
        this.cpuCores = cpuCores;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.platform.CPUInformation#getLoad1()
     */
    @Override
    public float getLoad1 () {
        return this.load1;
    }


    /**
     * @param load1
     *            the load1 to set
     */
    public void setLoad1 ( float load1 ) {
        this.load1 = load1;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.platform.CPUInformation#getLoad5()
     */
    @Override
    public float getLoad5 () {
        return this.load5;
    }


    /**
     * @param load5
     *            the load5 to set
     */
    public void setLoad5 ( float load5 ) {
        this.load5 = load5;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.platform.CPUInformation#getLoad15()
     */
    @Override
    public float getLoad15 () {
        return this.load15;
    }


    /**
     * @param load15
     *            the load15 to set
     */
    public void setLoad15 ( float load15 ) {
        this.load15 = load15;
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.cpuCores == null ) ? 0 : this.cpuCores.hashCode() );
        result = prime * result + this.totalCPUCount;
        return result;
    }


    // -GENERATED

    @Override
    // +GENERATED
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( ! ( obj instanceof CPUInformationImpl ) )
            return false;
        CPUInformationImpl other = (CPUInformationImpl) obj;
        if ( this.cpuCores == null ) {
            if ( other.cpuCores != null )
                return false;
        }
        else if ( !this.cpuCores.equals(other.cpuCores) )
            return false;
        if ( this.totalCPUCount != other.totalCPUCount )
            return false;

        if ( this.load1 != other.load1 ) {
            return false;
        }

        if ( this.load5 != other.load5 ) {
            return false;
        }

        if ( this.load15 != other.load15 ) {
            return false;
        }

        return true;
    }
    // -GENERATED

}
