/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.audit;


import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.audit.SingleEntityFileshareEvent;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
public class SingleEntityFileshareAuditBuilder extends EntityFileshareAuditBuilder<SingleEntityFileshareEvent, SingleEntityFileshareAuditBuilder> {

    /**
     * @param logger
     */
    public SingleEntityFileshareAuditBuilder ( EventLogger logger ) {
        super(logger);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder#makeEvent()
     */
    @Override
    protected SingleEntityFileshareEvent makeEvent () {
        return new SingleEntityFileshareEvent();
    }


    /**
     * 
     * @param e
     * @return this
     */
    public SingleEntityFileshareAuditBuilder entity ( VFSEntity e ) {
        if ( e == null ) {
            return this;
        }
        this.event.setTargetEntityId(e.getEntityKey());
        this.event.setTargetEntityName(e.getLocalName() != null ? e.getLocalName() : "ROOT"); //$NON-NLS-1$
        if ( e.getOwner() != null ) {
            this.event.setTargetEntityOwnerId(e.getOwner().getId());
        }
        this.event.setTargetEntityType(e instanceof FileEntity ? "file" : //$NON-NLS-1$
                "dir"); //$NON-NLS-1$

        this.event.setTargetLastModified(e.getLastModified() != null ? e.getLastModified().getMillis() : null);
        this.event.setTargetCreated(e.getCreated() != null ? e.getCreated().getMillis() : null);

        this.event.setTargetSecurityLabel(e.getSecurityLabel() != null ? e.getSecurityLabel().toString() : null);

        if ( e instanceof VFSFileEntity ) {
            this.event.setTargetSize( ( (VFSFileEntity) e ).getFileSize());
            this.event.setTargetContentType( ( (VFSFileEntity) e ).getContentType());
        }
        if ( e instanceof ContainerEntity && ( (ContainerEntity) e ).getParent() != null && this.event.getTargetParentId() == null ) {
            this.event.setTargetParentId( ( (ContainerEntity) e ).getParent().getEntityKey());
            this.event.setTargetParentName( ( (ContainerEntity) e ).getParent().getLocalName() != null ? ( (ContainerEntity) e ).getParent()
                    .getLocalName() : "ROOT"); //$NON-NLS-1$
        }
        return this;
    }


    /**
     * 
     * @param e
     * @return this
     */
    public SingleEntityFileshareAuditBuilder parentEntity ( VFSEntity e ) {
        if ( e == null ) {
            return this;
        }
        this.event.setTargetParentId(e.getEntityKey());
        this.event.setTargetParentName(e.getLocalName() != null ? e.getLocalName() : "ROOT"); //$NON-NLS-1$
        return this;
    }

}
