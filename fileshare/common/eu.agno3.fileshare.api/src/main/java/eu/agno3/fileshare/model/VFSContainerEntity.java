/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


/**
 * @author mbechler
 *
 */
public interface VFSContainerEntity extends VFSEntity {

    /**
     * @return whether to allow overwriting files
     */
    boolean getAllowFileOverwrite ();


    /**
     * @param allowFileOverwrite
     */
    void setAllowFileOverwrite ( boolean allowFileOverwrite );


    /**
     * @return whether to send notifications for the entity
     */
    boolean getSendNotifications ();


    /**
     * @param sendNotifications
     */
    void setSendNotifications ( boolean sendNotifications );


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#cloneShallow()
     */
    @Override
    public VFSContainerEntity cloneShallow ();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#cloneShallow(boolean)
     */
    @Override
    public VFSContainerEntity cloneShallow ( boolean b );


    /**
     * @return the combined children size if known
     */
    Long getChildrenSize ();


    /**
     * 
     * @return the number of children
     */
    Long getNumChildren ();


    /**
     * @return whether the container is empty
     */
    boolean isEmpty ();

}
