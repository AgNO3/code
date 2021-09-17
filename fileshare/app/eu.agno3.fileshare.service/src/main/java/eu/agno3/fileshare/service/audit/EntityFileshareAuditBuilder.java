/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.audit;


import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.audit.EntityFileshareEvent;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 * @param <TEv>
 * @param <T>
 *
 */
public abstract class EntityFileshareAuditBuilder <TEv extends EntityFileshareEvent, T extends AbstractFileshareAuditBuilder<TEv, T>> extends
        AbstractFileshareAuditBuilder<TEv, T> {

    /**
     * @param logger
     */
    public EntityFileshareAuditBuilder ( EventLogger logger ) {
        super(logger);
    }


    /**
     * @param g
     * @return this
     */
    public T grant ( Grant g ) {
        if ( g == null ) {
            return self();
        }
        this.event.setGrantId(g.getId());

        if ( g instanceof MailGrant ) {
            this.event.setTokenGrantType("mail"); //$NON-NLS-1$
            this.event.setTokenGrantId( ( (MailGrant) g ).getMailAddress());
        }
        else if ( g instanceof TokenGrant ) {
            this.event.setTokenGrantType("link"); //$NON-NLS-1$
            this.event.setTokenGrantId( ( (TokenGrant) g ).getIdentifier());
        }

        return self();
    }


    /**
     * @param violation
     * @return this
     */
    public T policyViolation ( PolicyViolation violation ) {
        this.event.setPolicyViolation(violation.getKey());
        this.fail(AuditStatus.UNAUTHORIZED);
        return self();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder#fail(eu.agno3.fileshare.exceptions.FileshareException)
     */
    @Override
    public void fail ( FileshareException e ) {

        if ( e instanceof PolicyNotFulfilledException ) {
            policyViolation( ( (PolicyNotFulfilledException) e ).getViolation());
        }

        super.fail(e);
    }

}