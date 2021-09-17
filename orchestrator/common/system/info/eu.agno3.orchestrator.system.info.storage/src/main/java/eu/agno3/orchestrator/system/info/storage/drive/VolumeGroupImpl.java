/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


import java.util.List;

import eu.agno3.orchestrator.system.info.storage.volume.Volume;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( VolumeGroup.class )
public class VolumeGroupImpl implements VolumeGroup {

    /**
     * 
     */
    private static final long serialVersionUID = -8578892476845259713L;
    private String vgName;
    private List<Volume> volumes;
    private boolean system;
    private String assignedAlias;


    /**
     * 
     */
    public VolumeGroupImpl () {}


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("Volume group '%s'", this.vgName); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.Drive#getId()
     */
    @Override
    public String getId () {
        return vgNameToDriveId(this.vgName);
    }


    /**
     * @param id
     */
    public void setId ( String id ) {
        // ignore
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
     * @param vgName
     * @return the virtual drive id for this volume group
     */
    public static String vgNameToDriveId ( String vgName ) {
        return "LVM-" + vgName; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.Drive#getSize()
     */
    @Override
    public Long getSize () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.drive.VolumeGroup#getVolumeGroupName()
     */
    @Override
    public String getVolumeGroupName () {
        return this.vgName;
    }


    /**
     * 
     * @param vgName
     */
    public void setVolumeGroupName ( String vgName ) {
        this.vgName = vgName;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.vgName == null ) ? 0 : this.vgName.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( ! ( obj instanceof VolumeGroupImpl ) )
            return false;
        VolumeGroupImpl other = (VolumeGroupImpl) obj;
        if ( this.vgName == null ) {
            if ( other.vgName != null )
                return false;
        }
        else if ( !this.vgName.equals(other.vgName) )
            return false;
        return true;
    }
    // -GENERATED

}
