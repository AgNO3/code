/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( SwapFileSystem.class )
public class SwapFileSystemImpl extends AbstractFileSystemImpl implements SwapFileSystem {

    /**
     * 
     */
    private static final long serialVersionUID = -7678187900365586199L;
    private boolean active;


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("Swapspace (uuid=%s active=%s)", //$NON-NLS-1$
            this.getUuid(),
            Boolean.toString(this.active));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.AbstractFileSystemImpl#getFsType()
     */
    @Override
    public FileSystemType getFsType () {
        return FileSystemType.SWAP;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.fs.SwapFileSystem#isActive()
     */
    @Override
    public boolean isActive () {
        return this.active;
    }


    /**
     * @param active
     *            the active to set
     */
    public void setActive ( boolean active ) {
        this.active = active;
    }

}
