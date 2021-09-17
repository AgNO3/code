/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.audit;


import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.audit.MultiEntityFileshareEvent;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
public class MultiEntityFileshareAuditBuilder extends EntityFileshareAuditBuilder<MultiEntityFileshareEvent, MultiEntityFileshareAuditBuilder> {

    /**
     * @param logger
     */
    public MultiEntityFileshareAuditBuilder ( EventLogger logger ) {
        super(logger);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder#makeEvent()
     */
    @Override
    protected MultiEntityFileshareEvent makeEvent () {
        return new MultiEntityFileshareEvent();
    }


    /**
     * 
     * @param e
     * @return this
     */
    public MultiEntityFileshareAuditBuilder entity ( VFSEntity e ) {
        if ( e == null ) {
            return this;
        }
        this.event.getTargetEntityIds().add(e.getEntityKey());
        this.event.getTargetEntityNames().add(e.getLocalName() != null ? e.getLocalName() : "ROOT"); //$NON-NLS-1$
        this.event.getTargetEntityOwners().add(e.getOwner().getId());
        return this;
    }


    /**
     * 
     * @param e
     * @return this
     */
    public MultiEntityFileshareAuditBuilder parentEntity ( VFSContainerEntity e ) {
        if ( e == null ) {
            return this;
        }
        this.event.setTargetParentId(e.getEntityKey());
        this.event.setTargetParentName(e.getLocalName() != null ? e.getLocalName() : "ROOT"); //$NON-NLS-1$
        return this;
    }

}
