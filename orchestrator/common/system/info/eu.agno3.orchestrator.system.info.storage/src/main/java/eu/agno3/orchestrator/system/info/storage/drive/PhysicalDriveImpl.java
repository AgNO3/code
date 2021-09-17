/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( PhysicalDrive.class )
public class PhysicalDriveImpl extends AbstractDriveImpl implements PhysicalDrive {

    private static final long serialVersionUID = 8274278146143546394L;
    private String vendor;
    private String model;
    private String serial;
    private PartitionTableType partitionTableType;
    private String blockDeviceName;
    private Long uncapturedSpace;


    /**
     * 
     */
    public PhysicalDriveImpl () {}


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( this.model == null ) ? 0 : this.model.hashCode() );
        result = prime * result + ( ( this.serial == null ) ? 0 : this.serial.hashCode() );
        result = prime * result + ( ( this.vendor == null ) ? 0 : this.vendor.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( !super.equals(obj) )
            return false;
        if ( ! ( obj instanceof PhysicalDriveImpl ) )
            return false;
        PhysicalDriveImpl other = (PhysicalDriveImpl) obj;
        if ( this.model == null ) {
            if ( other.model != null )
                return false;
        }
        else if ( !this.model.equals(other.model) )
            return false;
        if ( this.serial == null ) {
            if ( other.serial != null )
                return false;
        }
        else if ( !this.serial.equals(other.serial) )
            return false;
        if ( this.vendor == null ) {
            if ( other.vendor != null )
                return false;
        }
        else if ( !this.vendor.equals(other.vendor) )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "Physical drive %s (vendor=%s model=%s serial=%s size=%d)", //$NON-NLS-1$
            this.getId(),
            this.vendor,
            this.model,
            this.serial,
            this.getSize());
    }


    @Override
    public final String getVendor () {
        return this.vendor;
    }


    /**
     * @param vendor
     */
    public final void setVendor ( String vendor ) {
        this.vendor = vendor;
    }


    @Override
    public final String getModel () {
        return this.model;
    }


    /**
     * @param model
     */
    public final void setModel ( String model ) {
        this.model = model;
    }


    @Override
    public final String getSerial () {
        return this.serial;
    }


    /**
     * @param serial
     */
    public final void setSerial ( String serial ) {
        this.serial = serial;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive#getPartitionTableType()
     */
    @Override
    public PartitionTableType getPartitionTableType () {
        return this.partitionTableType;
    }


    /**
     * @param partitionTableType
     *            the partitionTableType to set
     */
    public void setPartitionTableType ( PartitionTableType partitionTableType ) {
        this.partitionTableType = partitionTableType;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive#getBlockDeviceName()
     */
    @Override
    public String getBlockDeviceName () {
        return this.blockDeviceName;
    }


    /**
     * @param blockDeviceName
     *            the blockDeviceName to set
     */
    public void setBlockDeviceName ( String blockDeviceName ) {
        this.blockDeviceName = blockDeviceName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive#getUncapturedSpace()
     */
    @Override
    public Long getUncapturedSpace () {
        return this.uncapturedSpace;
    }


    /**
     * @param uncapturedSpace
     *            the uncapturedSpace to set
     */
    public void setUncapturedSpace ( Long uncapturedSpace ) {
        this.uncapturedSpace = uncapturedSpace;
    }

}
