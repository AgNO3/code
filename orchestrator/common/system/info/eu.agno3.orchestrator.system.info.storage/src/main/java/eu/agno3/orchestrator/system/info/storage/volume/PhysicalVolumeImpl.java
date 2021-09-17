/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.volume;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( PhysicalVolume.class )
public class PhysicalVolumeImpl extends AbstractVolumeImpl implements PhysicalVolume {

    private int index;
    private VolumeType type;

    /**
     * 
     */
    private static final long serialVersionUID = -4962852766611305287L;


    /**
     * 
     */
    public PhysicalVolumeImpl () {}


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( PhysicalVolume o ) {
        int res = Integer.compare(this.index, o.getIndex());
        if ( res == 0 && !this.equals(o) ) {
            return Integer.compare(System.identityHashCode(this), System.identityHashCode(o));
        }
        return res;
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + this.index;
        result = prime * result + ( ( this.type == null ) ? 0 : this.type.hashCode() );
        return result;
    }


    // -GENERATED

    @Override
    // +GENERATED
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( !super.equals(obj) )
            return false;
        if ( ! ( obj instanceof PhysicalVolumeImpl ) )
            return false;
        PhysicalVolumeImpl other = (PhysicalVolumeImpl) obj;
        if ( this.index != other.index )
            return false;
        if ( this.type != other.type )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl#toString()
     */
    @Override
    public String toString () {
        return String.format("Physical Volume (index=%s type=%s size=%d)", this.index, this.type, this.getSize()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume#getIndex()
     */
    @Override
    public int getIndex () {
        return this.index;
    }


    /**
     * @param index
     *            the index to set
     */
    public void setIndex ( int index ) {
        this.index = index;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume#getType()
     */
    @Override
    public VolumeType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( VolumeType type ) {
        this.type = type;
    }

}
