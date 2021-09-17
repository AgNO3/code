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
@MapAs ( LogicalVolume.class )
public class LVMLogicalVolumeImpl extends AbstractVolumeImpl implements LogicalVolume {

    /**
     * 
     */
    private static final long serialVersionUID = -8889146423496190449L;

    private String name;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.LogicalVolume#getName()
     */
    @Override
    public String getName () {
        return this.name;
    }


    /**
     * @param lvName
     */
    public void setName ( String lvName ) {
        this.name = lvName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl#getLabel()
     */
    @Override
    public String getLabel () {
        return getName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl#toString()
     */
    @Override
    public String toString () {
        return String.format("Logical Volume '%s' (vg=[%s] size=%d)", this.name, this.getDrive(), this.getSize()); //$NON-NLS-1$
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( this.name == null ) ? 0 : this.name.hashCode() );
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
        if ( ! ( obj instanceof LVMLogicalVolumeImpl ) )
            return false;
        LVMLogicalVolumeImpl other = (LVMLogicalVolumeImpl) obj;
        if ( this.name == null ) {
            if ( other.name != null )
                return false;
        }
        else if ( !this.name.equals(other.name) )
            return false;
        return true;
    }
    // -GENERATED

}
