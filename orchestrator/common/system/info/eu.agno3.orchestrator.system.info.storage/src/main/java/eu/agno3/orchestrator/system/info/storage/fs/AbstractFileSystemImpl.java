/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractFileSystemImpl implements FileSystem {

    /**
     * 
     */
    private static final long serialVersionUID = 4816000208830713028L;
    private FileSystemType fsType;
    private UUID uuid;
    private String label;
    private String devPath;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.FileSystem#getIdentifier()
     */
    @Override
    public String getIdentifier () {
        if ( this.getLabel() != null ) {
            return this.getLabel();
        }

        if ( this.getUuid() != null ) {
            return this.getUuid().toString();
        }
        return this.devPath;
    }


    /**
     * 
     * @param id
     */
    public void setIdentifier ( String id ) {
        // ignore
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.fsType == null ) ? 0 : this.fsType.hashCode() );
        result = prime * result + ( ( this.label == null ) ? 0 : this.label.hashCode() );
        result = prime * result + ( ( this.uuid == null ) ? 0 : this.uuid.hashCode() );
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
        if ( ! ( obj instanceof AbstractFileSystemImpl ) )
            return false;
        AbstractFileSystemImpl other = (AbstractFileSystemImpl) obj;
        if ( this.fsType != other.fsType )
            return false;
        if ( this.label == null ) {
            if ( other.label != null )
                return false;
        }
        else if ( !this.label.equals(other.label) )
            return false;
        if ( this.uuid == null ) {
            if ( other.uuid != null )
                return false;
        }
        else if ( !this.uuid.equals(other.uuid) )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.FileSystem#getFsType()
     */
    @Override
    public FileSystemType getFsType () {
        return this.fsType;
    }


    /**
     * @param fsType
     *            the fsType to set
     */
    public void setFsType ( FileSystemType fsType ) {
        this.fsType = fsType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.FileSystem#getUuid()
     */
    @Override
    public UUID getUuid () {
        return this.uuid;
    }


    /**
     * @param uuid
     *            the uuid to set
     */
    public void setUuid ( UUID uuid ) {
        this.uuid = uuid;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.FileSystem#getLabel()
     */
    @Override
    public String getLabel () {
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    public void setLabel ( String label ) {
        this.label = label;
    }


    /**
     * @param devPath
     *            the devPath to set
     */
    public void setDevPath ( String devPath ) {
        this.devPath = devPath;
    }

}
