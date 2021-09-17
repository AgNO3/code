/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.volume;


import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystem;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractVolumeImpl implements Volume {

    /**
     * 
     */
    private static final long serialVersionUID = 2482506902234909691L;
    private Drive drive;
    private long size;
    private FileSystem fileSystem;
    private String device;
    private String label;


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume#getDrive()
     */
    @Override
    public final Drive getDrive () {
        return this.drive;
    }


    /**
     * 
     * @param drive
     */
    public final void setDrive ( Drive drive ) {
        this.drive = drive;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.volume.Volume#getDevice()
     */
    @Override
    public String getDevice () {
        return this.device;
    }


    /**
     * @param device
     *            the device to set
     */
    public void setDevice ( String device ) {
        this.device = device;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume#getSize()
     */
    @Override
    public final long getSize () {
        return this.size;
    }


    /**
     * 
     * @param size
     */
    public final void setSize ( long size ) {
        this.size = size;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume#holdsFilesystem()
     */
    @Override
    public boolean holdsFilesystem () {
        return this.fileSystem != null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.Volume#getFileSystem()
     */
    @Override
    public FileSystem getFileSystem () {
        return this.fileSystem;
    }


    /**
     * @param fileSystem
     *            the fileSystem to set
     */
    public void setFileSystem ( FileSystem fileSystem ) {
        this.fileSystem = fileSystem;
    }


    /**
     * @param label
     */
    public void setLabel ( String label ) {
        this.label = label;
    }


    /**
     * @return the label
     */
    @Override
    public String getLabel () {
        return this.label;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("Volume (size=%d drive=[%s])", this.size, this.drive); //$NON-NLS-1$
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.drive == null ) ? 0 : this.drive.hashCode() );
        result = prime * result + (int) ( this.size ^ ( this.size >>> 32 ) );
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
        if ( ! ( obj instanceof AbstractVolumeImpl ) )
            return false;
        AbstractVolumeImpl other = (AbstractVolumeImpl) obj;
        if ( this.drive == null ) {
            if ( other.drive != null )
                return false;
        }
        else if ( !this.drive.equals(other.drive) )
            return false;
        if ( this.size != other.size )
            return false;
        return true;
    }
    // -GENERATED

}
