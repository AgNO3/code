/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas;


import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;


/**
 * 
 * Cloned from CAS: Original implementation holds a reference to the Logger
 * 
 * @author mbechler
 *
 */
public class TGTExpirationPolicy implements ExpirationPolicy {

    private static final Logger log = Logger.getLogger(TGTExpirationPolicy.class);

    /**
     * 
     */
    private static final long serialVersionUID = -578365690356657233L;

    /** Maximum time this ticket is valid. */
    private long maxTimeToLiveInMilliSeconds;

    /** Time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;


    /**
     * 
     */
    public TGTExpirationPolicy () {
        this.timeToKillInMilliSeconds = 0;
        this.maxTimeToLiveInMilliSeconds = 0;
    }


    /**
     * @param maxTimeToLive
     * @param timeToKill
     * @param timeUnit
     * 
     */
    public TGTExpirationPolicy ( final long maxTimeToLive, final long timeToKill, final TimeUnit timeUnit ) {
        this.maxTimeToLiveInMilliSeconds = timeUnit.toMillis(maxTimeToLive);
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.ticket.ExpirationPolicy#isExpired(org.jasig.cas.ticket.TicketState)
     */
    @Override
    public boolean isExpired ( TicketState ticketState ) {
        final long currentSystemTimeInMillis = System.currentTimeMillis();

        // Ticket has been used, check maxTimeToLive (hard window)
        if ( ( currentSystemTimeInMillis - ticketState.getCreationTime() >= this.maxTimeToLiveInMilliSeconds ) ) {
            log.debug("Ticket is expired because the time since creation is greater than maxTimeToLiveInMilliSeconds"); //$NON-NLS-1$
            return true;
        }

        // Ticket is within hard window, check timeToKill (sliding window)
        if ( ( currentSystemTimeInMillis - ticketState.getLastTimeUsed() >= this.timeToKillInMilliSeconds ) ) {
            log.debug("Ticket is expired because the time since last use is greater than timeToKillInMilliseconds"); //$NON-NLS-1$
            return true;
        }

        return false;
    }

}
