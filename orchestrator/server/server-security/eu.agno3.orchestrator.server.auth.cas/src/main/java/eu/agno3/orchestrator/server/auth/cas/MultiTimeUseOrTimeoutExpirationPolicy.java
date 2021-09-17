/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas;


import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;


/**
 * Cloned from CAS: Original implementation holds a reference to the Logger
 * 
 * @author mbechler
 *
 */
public class MultiTimeUseOrTimeoutExpirationPolicy implements ExpirationPolicy {

    /**
     * 
     */
    private static final long serialVersionUID = -9169899736438668661L;

    /** The time to kill in milliseconds. */
    private final long timeToKillInMilliSeconds;

    /** The maximum number of uses before expiration. */
    private final int numberOfUses;


    /**
     * 
     */
    public MultiTimeUseOrTimeoutExpirationPolicy () {
        this.timeToKillInMilliSeconds = 0;
        this.numberOfUses = 0;
    }


    /**
     * 
     * @param numberOfUses
     * @param timeToKillInMilliSeconds
     */
    public MultiTimeUseOrTimeoutExpirationPolicy ( final int numberOfUses, final long timeToKillInMilliSeconds ) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
        this.numberOfUses = numberOfUses;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.jasig.cas.ticket.ExpirationPolicy#isExpired(org.jasig.cas.ticket.TicketState)
     */
    @Override
    public boolean isExpired ( final TicketState ticketState ) {
        return ( ticketState == null ) || ( ticketState.getCountOfUses() >= this.numberOfUses )
                || ( System.currentTimeMillis() - ticketState.getLastTimeUsed() >= this.timeToKillInMilliSeconds );
    }

}
