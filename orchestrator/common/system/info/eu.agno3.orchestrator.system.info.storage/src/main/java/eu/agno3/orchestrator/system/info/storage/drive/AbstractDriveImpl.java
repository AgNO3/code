/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


import java.util.List;

import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractDriveImpl implements Drive {

    /**
     * 
     */
    private static final long serialVersionUID = -3604164844638738273L;
    private Long size = null;
    private String id;
    private List<Volume> volumes;
    private boolean system;
    private String assignedAlias;


    /**
     * 
     */
    public AbstractDriveImpl () {
        super();
    }


    @Override
    public final Long getSize () {
        return this.size;
    }


    /**
     * @param size
     */
    public final void setSize ( Long size ) {
        this.size = size;
    }


    @Override
    public final String getId () {
        return this.id;
    }


    /**
     * @param id
     */
    public final void setId ( String id ) {
        this.id = id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.drive.Drive#getAssignedAlias()
     */
    @Override
    public String getAssignedAlias () {
        return this.assignedAlias;
    }


    /**
     * @param assignedAlias
     *            the assignedAlias to set
     */
    public void setAssignedAlias ( String assignedAlias ) {
        this.assignedAlias = assignedAlias;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.Drive#getVolumes()
     */
    @Override
    public List<Volume> getVolumes () {
        return this.volumes;
    }


    /**
     * @param volumes
     *            the volumes to set
     */
    public void setVolumes ( List<Volume> volumes ) {
        this.volumes = volumes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.drive.Drive#getSystem()
     */
    @Override
    public boolean getSystem () {
        return this.system;
    }


    /**
     * @param system
     *            the system to set
     */
    public void setSystem ( boolean system ) {
        this.system = system;
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        result = prime * result + ( ( this.size == null ) ? 0 : this.size.hashCode() );
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
        if ( ! ( obj instanceof AbstractDriveImpl ) )
            return false;
        AbstractDriveImpl other = (AbstractDriveImpl) obj;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        if ( this.size == null ) {
            if ( other.size != null )
                return false;
        }
        else if ( !this.size.equals(other.size) )
            return false;
        return true;
    }
    // -GENERATED

}