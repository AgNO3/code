/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( DataFileSystem.class )
public class DataFileSystemImpl extends AbstractFileSystemImpl implements DataFileSystem {

    /**
     * 
     */
    private static final long serialVersionUID = 6244325066546452416L;
    private Set<String> mountPoints;
    private Long totalSpace = null;
    private Long usableSpace = null;
    private Long uncapturedSpace;


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s Filesystem (id=[%s])", this.getFsType(), this.getIdentifier()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem#getMountPoints()
     */
    @Override
    public Set<String> getMountPoints () {
        return this.mountPoints;
    }


    /**
     * @param mountPoints
     *            the mountPoints to set
     */
    public void setMountPoints ( Set<String> mountPoints ) {
        this.mountPoints = mountPoints;
    }


    /**
     * @param totalSpace
     */
    public void setTotalSpace ( Long totalSpace ) {
        this.totalSpace = totalSpace;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem#getTotalSpace()
     */
    @Override
    public Long getTotalSpace () {
        return this.totalSpace;
    }


    /**
     * @param usableSpace
     */
    public void setUsableSpace ( Long usableSpace ) {
        this.usableSpace = usableSpace;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem#getUsableSpace()
     */
    @Override
    public Long getUsableSpace () {
        return this.usableSpace;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem#getUncapturedSpace()
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
