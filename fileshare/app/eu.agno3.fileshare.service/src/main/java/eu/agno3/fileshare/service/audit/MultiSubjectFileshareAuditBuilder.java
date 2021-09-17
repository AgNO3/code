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
import eu.agno3.fileshare.model.audit.MultiSubjectFileshareEvent;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
public class MultiSubjectFileshareAuditBuilder extends AbstractFileshareAuditBuilder<MultiSubjectFileshareEvent, MultiSubjectFileshareAuditBuilder> {

    /**
     * @param logger
     */
    public MultiSubjectFileshareAuditBuilder ( EventLogger logger ) {
        super(logger);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder#makeEvent()
     */
    @Override
    protected MultiSubjectFileshareEvent makeEvent () {
        return new MultiSubjectFileshareEvent();
    }


    /**
     * 
     * @param s
     * @return this
     */
    public MultiSubjectFileshareAuditBuilder subject ( Subject s ) {

        if ( s == null ) {
            return self();
        }

        this.event.getTargetIds().add(s.getId());
        if ( s instanceof User ) {
            this.event.getTargetTypes().add("user"); //$NON-NLS-1$
            this.event.getTargetNames().add( ( (User) s ).getPrincipal().toString());
        }
        else if ( s instanceof Group ) {
            this.event.getTargetTypes().add("group"); //$NON-NLS-1$
            this.event.getTargetNames().add( ( (Group) s ).getName());
        }
        else {
            this.event.getTargetTypes().add("UNKNOWN"); //$NON-NLS-1$
            this.event.getTargetNames().add(null);
        }

        return self();
    }
}
