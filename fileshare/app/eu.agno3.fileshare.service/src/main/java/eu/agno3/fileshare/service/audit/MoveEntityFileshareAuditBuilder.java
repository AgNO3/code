/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.audit;


import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.audit.MoveEntityFileshareEvent;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
public class MoveEntityFileshareAuditBuilder extends EntityFileshareAuditBuilder<MoveEntityFileshareEvent, MoveEntityFileshareAuditBuilder> {

    /**
     * @param logger
     */
    public MoveEntityFileshareAuditBuilder ( EventLogger logger ) {
        super(logger);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder#makeEvent()
     */
    @Override
    protected MoveEntityFileshareEvent makeEvent () {
        return new MoveEntityFileshareEvent();
    }


    /**
     * @param entity
     * @param parent
     * @param rename
     * @return this
     */
    public MoveEntityFileshareAuditBuilder source ( VFSEntity entity, VFSContainerEntity parent, String rename ) {
        if ( entity == null ) {
            return this;
        }
        this.event.getSourceEntityIds().add(entity.getEntityKey());
        this.event.getSourceEntityNames().add(entity.getLocalName() != null ? entity.getLocalName() : "ROOT"); //$NON-NLS-1$
        this.event.getSourceEntityOwners().add(entity.getOwner().getId());
        this.event.getSourceEntityRename().add(rename);
        if ( parent != null ) {
            this.event.getSourceEntityParentIds().add(parent.getEntityKey());
        }
        else {
            this.event.getSourceEntityParentIds().add(null);
        }
        return self();
    }


    /**
     * @param e
     * @return this
     */
    public MoveEntityFileshareAuditBuilder target ( VFSEntity e ) {
        if ( e == null ) {
            return this;
        }
        this.event.setTargetId(e.getEntityKey());
        this.event.setTargetName(e.getLocalName() != null ? e.getLocalName() : "ROOT"); //$NON-NLS-1$
        this.event.setTargetOwner(e.getOwner().getId());
        return self();
    }

}
