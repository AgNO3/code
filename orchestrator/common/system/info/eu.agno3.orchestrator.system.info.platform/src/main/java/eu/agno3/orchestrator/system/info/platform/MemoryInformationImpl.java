/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( MemoryInformation.class )
public class MemoryInformationImpl implements MemoryInformation {

    /**
     * 
     */
    private static final long serialVersionUID = 2629337593674514162L;
    private long totalPhysicalMemory;
    private long physicalMemoryFree;
    private long physMemoryUsedBuffers;
    private long physMemoryUsedCache;

    private long totalSwapMemory;
    private long currentSwapMemoryFree;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getTotalPhysicalMemory()
     */
    @Override
    public long getTotalPhysicalMemory () {
        return this.totalPhysicalMemory;
    }


    /**
     * @param totalPhysicalMemory
     *            the totalPhysicalMemory to set
     */
    public void setTotalPhysicalMemory ( long totalPhysicalMemory ) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getCurrentPhysicalMemoryFree()
     */
    @Override
    public long getCurrentPhysicalMemoryFree () {
        return this.physicalMemoryFree;
    }


    /**
     * @param curPhysFree
     *            the currentPhysicalMemoryFree to set
     */
    public void setCurrentPhysicalMemoryFree ( long curPhysFree ) {
        this.physicalMemoryFree = curPhysFree;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getCurrentPhysicalMemoryUsedTotal()
     */
    @Override
    public long getCurrentPhysicalMemoryUsedTotal () {
        return this.totalPhysicalMemory - this.physicalMemoryFree;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getCurrentPhysicalMemoryUsedBuffers()
     */
    @Override
    public long getCurrentPhysicalMemoryUsedBuffers () {
        return this.physMemoryUsedBuffers;
    }


    /**
     * @param curPhysUsedBufs
     *            the currentPhysicalMemoryUsedBuffers to set
     */
    public void setCurrentPhysicalMemoryUsedBuffers ( long curPhysUsedBufs ) {
        this.physMemoryUsedBuffers = curPhysUsedBufs;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getCurrentPhysicalMemoryUsedCache()
     */
    @Override
    public long getCurrentPhysicalMemoryUsedCache () {
        return this.physMemoryUsedCache;
    }


    /**
     * @param curPhysUsedCache
     *            the currentPhysicalMemoryUsedCache to set
     */
    public void setCurrentPhysicalMemoryUsedCache ( long curPhysUsedCache ) {
        this.physMemoryUsedCache = curPhysUsedCache;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getTotalSwapMemory()
     */
    @Override
    public long getTotalSwapMemory () {
        return this.totalSwapMemory;
    }


    /**
     * @param totalSwapMemory
     *            the totalSwapMemory to set
     */
    public void setTotalSwapMemory ( long totalSwapMemory ) {
        this.totalSwapMemory = totalSwapMemory;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getCurrentSwapMemoryUsed()
     */
    @Override
    public long getCurrentSwapMemoryUsed () {
        return this.totalSwapMemory - this.currentSwapMemoryFree;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.MemoryInformation#getCurrentSwapMemoryFree()
     */
    @Override
    public long getCurrentSwapMemoryFree () {
        return this.currentSwapMemoryFree;
    }


    /**
     * @param currentSwapMemoryFree
     *            the currentSwapMemoryFree to set
     */
    public void setCurrentSwapMemoryFree ( long currentSwapMemoryFree ) {
        this.currentSwapMemoryFree = currentSwapMemoryFree;
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) ( this.totalPhysicalMemory ^ ( this.totalPhysicalMemory >>> 32 ) );
        result = prime * result + (int) ( this.totalSwapMemory ^ ( this.totalSwapMemory >>> 32 ) );
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
        if ( ! ( obj instanceof MemoryInformationImpl ) )
            return false;
        MemoryInformationImpl other = (MemoryInformationImpl) obj;
        if ( this.totalPhysicalMemory != other.totalPhysicalMemory )
            return false;
        if ( this.totalSwapMemory != other.totalSwapMemory )
            return false;
        return true;
    }
    // -GENERATED

}
