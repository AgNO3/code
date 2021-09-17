/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.audit;


import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.audit.SubjectFileshareEvent;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
public class SubjectFileshareAuditBuilder extends AbstractFileshareAuditBuilder<SubjectFileshareEvent, SubjectFileshareAuditBuilder> {

    /**
     * @param logger
     */
    public SubjectFileshareAuditBuilder ( EventLogger logger ) {
        super(logger);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder#makeEvent()
     */
    @Override
    protected SubjectFileshareEvent makeEvent () {
        return new SubjectFileshareEvent();
    }


    /**
     * 
     * @param s
     * @return this
     */
    public SubjectFileshareAuditBuilder subject ( Subject s ) {

        if ( s == null ) {
            return self();
        }

        this.event.setTargetId(s.getId());
        if ( s instanceof User ) {
            this.event.setTargetType("user"); //$NON-NLS-1$
            this.event.setTargetName( ( (User) s ).getPrincipal().toString());
        }
        else if ( s instanceof Group ) {
            this.event.setTargetType("group"); //$NON-NLS-1$
            this.event.setTargetName( ( (Group) s ).getName());
        }
        else {
            this.event.setTargetType("UNKNOWN"); //$NON-NLS-1$
        }

        return self();
    }
}
