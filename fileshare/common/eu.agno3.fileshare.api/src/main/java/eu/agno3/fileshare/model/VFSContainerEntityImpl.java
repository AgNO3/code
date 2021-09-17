/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.IOException;


/**
 * @author mbechler
 *
 */
public class VFSContainerEntityImpl extends VFSEntityImpl implements VFSContainerEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -4290551936830890799L;
    private boolean empty;
    private Long childrenSize;
    private Long numChildren;
    private boolean allowFileOverwrite;
    private boolean sendNotifications;


    /**
     * @param relativePath
     * @param group
     * @param readOnly
     * @throws IOException
     */
    public VFSContainerEntityImpl ( String relativePath, VirtualGroup group, boolean readOnly ) throws IOException {
        super(relativePath, group, readOnly);
    }


    /**
     * @param e
     * 
     */
    public VFSContainerEntityImpl ( VFSContainerEntityImpl e ) {
        super(e);
        this.numChildren = e.numChildren;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getEntityType()
     */
    @Override
    public EntityType getEntityType () {
        return EntityType.DIRECTORY;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#getAllowFileOverwrite()
     */
    @Override
    public boolean getAllowFileOverwrite () {
        return this.allowFileOverwrite;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#setAllowFileOverwrite(boolean)
     */
    @Override
    public void setAllowFileOverwrite ( boolean allowFileOverwrite ) {
        this.allowFileOverwrite = allowFileOverwrite;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#getSendNotifications()
     */
    @Override
    public boolean getSendNotifications () {
        return this.sendNotifications;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#setSendNotifications(boolean)
     */
    @Override
    public void setSendNotifications ( boolean sendNotifications ) {
        this.sendNotifications = sendNotifications;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#cloneShallow()
     */
    @Override
    public VFSContainerEntity cloneShallow () {
        return cloneShallow(true);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#cloneShallow(boolean)
     */
    @Override
    public VFSContainerEntity cloneShallow ( boolean b ) {
        // probably no need for clone
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#getChildrenSize()
     */
    @Override
    public Long getChildrenSize () {
        return this.childrenSize;
    }


    /**
     * @param childrenSize
     *            the childrenSize to set
     */
    public void setChildrenSize ( long childrenSize ) {
        this.childrenSize = childrenSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#getNumChildren()
     */
    @Override
    public Long getNumChildren () {
        return this.numChildren;
    }


    /**
     * @param numChildren
     *            the numChildren to set
     */
    public void setNumChildren ( long numChildren ) {
        this.numChildren = numChildren;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#isEmpty()
     */
    @Override
    public boolean isEmpty () {
        return this.empty;
    }


    /**
     * @param empty
     *            the empty to set
     */
    public void setEmpty ( boolean empty ) {
        this.empty = empty;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        if ( this.getEntityKey() != null ) {
            return this.getEntityKey().toString();
        }
        return super.toString();
    }
}
