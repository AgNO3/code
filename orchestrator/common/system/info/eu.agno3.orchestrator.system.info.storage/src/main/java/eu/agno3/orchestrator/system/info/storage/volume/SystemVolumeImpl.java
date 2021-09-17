/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.volume;


import eu.agno3.orchestrator.system.info.storage.fs.FileSystem;


/**
 * @author mbechler
 * 
 */
public class SystemVolumeImpl extends PhysicalVolumeImpl implements SystemVolume {

    /**
     * 
     */
    private static final long serialVersionUID = 4112019781829748967L;

    private SystemVolumeType type;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl#holdsFilesystem()
     */
    @Override
    public boolean holdsFilesystem () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl#getFileSystem()
     */
    @Override
    public FileSystem getFileSystem () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl#toString()
     */
    @Override
    public String toString () {
        return String.format("%s (index=%s size=%d drive=[%s])", this.getSystemVolumeType(), this.getIndex(), this.getSize(), this.getDrive()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.volume.SystemVolume#getSystemVolumeType()
     */
    @Override
    public SystemVolumeType getSystemVolumeType () {
        return this.type;
    }


    /**
     * @param type
     */
    public void setSystemVolumeType ( SystemVolumeType type ) {
        this.type = type;
    }


    @Override
    public int hashCode () {
        // +GENERATED
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( this.type == null ) ? 0 : this.type.hashCode() );
        return result;
        // -GENERATED
    }


    @Override
    public boolean equals ( Object obj ) {
        // +GENERATED
        if ( this == obj )
            return true;
        if ( !super.equals(obj) )
            return false;
        if ( ! ( obj instanceof SystemVolumeImpl ) )
            return false;
        SystemVolumeImpl other = (SystemVolumeImpl) obj;
        if ( this.type != other.type )
            return false;
        return true;
        // -GENERATED
    }

}