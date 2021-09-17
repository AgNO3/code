/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "vgroups", indexes = {
    @Index ( columnList = "vfs", unique = true )
} )
public class VirtualGroup extends Group {

    /**
     * 
     */
    private static final long serialVersionUID = -4231249089561982507L;

    private static final String VGROUP_REALM = "VFS"; //$NON-NLS-1$

    private String vfs;

    private Set<MappedContainerEntity> mappedContainers = new HashSet<>();
    private Set<MappedFileEntity> mappedFiles = new HashSet<>();


    /**
     * 
     */
    public VirtualGroup () {
        super();
    }


    /**
     * @param virtualGroup
     * @param refs
     */
    public VirtualGroup ( VirtualGroup virtualGroup, boolean refs ) {
        super(virtualGroup, refs);
        this.vfs = virtualGroup.vfs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Subject#cloneShallow()
     */
    @Override
    public VirtualGroup cloneShallow () {
        return this.cloneShallow(true);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Group#cloneShallow(boolean)
     */
    @Override
    public VirtualGroup cloneShallow ( boolean refs ) {
        return new VirtualGroup(this, refs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Group#getRealm()
     */
    @Override
    public String getRealm () {
        return VGROUP_REALM;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Group#setRealm(java.lang.String)
     */
    @Override
    public void setRealm ( String realm ) {
        // ignore
    }


    /**
     * @return the vfs
     */
    public String getVfs () {
        return this.vfs;
    }


    /**
     * @param vfs
     *            the vfs to set
     */
    public void setVfs ( String vfs ) {
        this.vfs = vfs;
    }


    /**
     * @return the mappedContainers
     */
    @OneToMany ( mappedBy = "vfs", cascade = CascadeType.ALL )
    public Set<MappedContainerEntity> getMappedContainers () {
        return this.mappedContainers;
    }


    /**
     * @param mappedContainers
     *            the mappedContainers to set
     */
    public void setMappedContainers ( Set<MappedContainerEntity> mappedContainers ) {
        this.mappedContainers = mappedContainers;
    }


    /**
     * @return the mappedFiles
     */
    @OneToMany ( mappedBy = "vfs", cascade = CascadeType.ALL )
    public Set<MappedFileEntity> getMappedFiles () {
        return this.mappedFiles;
    }


    /**
     * @param mappedFiles
     *            the mappedFiles to set
     */
    public void setMappedFiles ( Set<MappedFileEntity> mappedFiles ) {
        this.mappedFiles = mappedFiles;
    }

}
