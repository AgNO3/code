/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( CPUCore.class )
public class CPUCoreImpl implements CPUCore {

    /**
     * 
     */
    private static final long serialVersionUID = 8883430194148426805L;
    private int physicalIndex;
    private int coreIndex;
    private String model;
    private int maximumFrequency;
    private int cacheSize;
    private Set<CPUFeature> cpuFeatures = new HashSet<>();


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("CPU Core '%s' (cpu=%d core=%d freq=%d MHz cache=%d MB features=[%s])", //$NON-NLS-1$
            this.model,
            this.physicalIndex,
            this.coreIndex,
            this.maximumFrequency,
            this.cacheSize / 1024 / 1024,
            StringUtils.join(this.cpuFeatures, ",")); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUCore#getPhysicalIndex()
     */
    @Override
    public int getPhysicalIndex () {
        return this.physicalIndex;
    }


    /**
     * @param physicalIndex
     *            the physicalIndex to set
     */
    public void setPhysicalIndex ( int physicalIndex ) {
        this.physicalIndex = physicalIndex;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUCore#getCoreIndex()
     */
    @Override
    public int getCoreIndex () {
        return this.coreIndex;
    }


    /**
     * @param coreIndex
     *            the coreIndex to set
     */
    public void setCoreIndex ( int coreIndex ) {
        this.coreIndex = coreIndex;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUCore#getModel()
     */
    @Override
    public String getModel () {
        return this.model;
    }


    /**
     * @param model
     *            the model to set
     */
    public void setModel ( String model ) {
        this.model = model;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUCore#getMaximumFrequency()
     */
    @Override
    public int getMaximumFrequency () {
        return this.maximumFrequency;
    }


    /**
     * @param maximumFrequency
     *            the maximumFrequency to set
     */
    public void setMaximumFrequency ( int maximumFrequency ) {
        this.maximumFrequency = maximumFrequency;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUCore#getCacheSize()
     */
    @Override
    public int getCacheSize () {
        return this.cacheSize;
    }


    /**
     * @param cacheSize
     *            the cacheSize to set
     */
    public void setCacheSize ( int cacheSize ) {
        this.cacheSize = cacheSize;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.platform.CPUCore#getFeatures()
     */
    @Override
    public Set<CPUFeature> getFeatures () {
        return this.cpuFeatures;
    }


    /**
     * @param cpuFeatures
     *            the cpuFeatures to set
     */
    public void setFeatures ( Set<CPUFeature> cpuFeatures ) {
        this.cpuFeatures = cpuFeatures;
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.cacheSize;
        result = prime * result + this.coreIndex;
        result = prime * result + ( ( this.cpuFeatures == null ) ? 0 : this.cpuFeatures.hashCode() );
        result = prime * result + this.maximumFrequency;
        result = prime * result + ( ( this.model == null ) ? 0 : this.model.hashCode() );
        result = prime * result + this.physicalIndex;
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
        if ( ! ( obj instanceof CPUCoreImpl ) )
            return false;
        CPUCoreImpl other = (CPUCoreImpl) obj;
        if ( this.cacheSize != other.cacheSize )
            return false;
        if ( this.coreIndex != other.coreIndex )
            return false;
        if ( this.cpuFeatures == null ) {
            if ( other.cpuFeatures != null )
                return false;
        }
        else if ( !this.cpuFeatures.equals(other.cpuFeatures) )
            return false;
        if ( this.maximumFrequency != other.maximumFrequency )
            return false;
        if ( this.model == null ) {
            if ( other.model != null )
                return false;
        }
        else if ( !this.model.equals(other.model) )
            return false;
        if ( this.physicalIndex != other.physicalIndex )
            return false;
        return true;
    }
    // -GENERATED

}
