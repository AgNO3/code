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
@MapAs ( RAIDDrive.class )
public class RAIDDriveImpl extends AbstractDriveImpl implements RAIDDrive {

    private RAIDLevel level;
    private int numDevices;
    private int numDegraded;
    private int numSpares;

    /**
     * 
     */
    private static final long serialVersionUID = 1031083826000012179L;


    /**
     * 
     */
    public RAIDDriveImpl () {}


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("RAID Array %s (level=%s size=%d drives=%d degraded=%d spare=%s)", //$NON-NLS-1$
            this.getId(),
            this.level,
            this.getSize(),
            this.getNumDevices(),
            this.getNumDegraded(),
            this.getNumSpares());
    }


    // +GENERATED

    @Override
    public int hashCode () {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( this.level == null ) ? 0 : this.level.hashCode() );
        result = prime * result + this.numDegraded;
        result = prime * result + this.numDevices;
        result = prime * result + this.numSpares;
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
        if ( ! ( obj instanceof RAIDDriveImpl ) )
            return false;
        RAIDDriveImpl other = (RAIDDriveImpl) obj;
        if ( this.level != other.level )
            return false;
        if ( this.numDegraded != other.numDegraded )
            return false;
        if ( this.numDevices != other.numDevices )
            return false;
        if ( this.numSpares != other.numSpares )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive#getRaidLevel()
     */
    @Override
    public final RAIDLevel getRaidLevel () {
        return this.level;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive#getNumDevices()
     */
    @Override
    public final int getNumDevices () {
        return this.numDevices;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive#getNumDegraded()
     */
    @Override
    public final int getNumDegraded () {
        return this.numDegraded;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive#getNumSpares()
     */
    @Override
    public final int getNumSpares () {
        return this.numSpares;
    }


    /**
     * @param level
     */
    public final void setRaidLevel ( RAIDLevel level ) {
        this.level = level;
    }


    /**
     * @param numDevices
     */
    public final void setNumDevices ( int numDevices ) {
        this.numDevices = numDevices;
    }


    /**
     * @param numDegraded
     */
    public final void setNumDegraded ( int numDegraded ) {
        this.numDegraded = numDegraded;
    }


    /**
     * @param numSpares
     */
    public final void setNumSpares ( int numSpares ) {
        this.numSpares = numSpares;
    }

}
