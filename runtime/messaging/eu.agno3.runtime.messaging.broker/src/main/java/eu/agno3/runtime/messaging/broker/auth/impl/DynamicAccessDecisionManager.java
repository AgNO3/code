/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.impl;


import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVote;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DynamicAccessDecisionManager.class
} )
public class DynamicAccessDecisionManager {

    private static final Logger log = Logger.getLogger(DynamicAccessDecisionManager.class);

    private SortedSet<DestinationAccessVoter> voters = new TreeSet<>(new VoterComparator());

    private ReadWriteLock votersLock = new ReentrantReadWriteLock();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindVoter ( DestinationAccessVoter voter ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Add voter " + voter.getClass().getName()); //$NON-NLS-1$
        }

        Lock l = this.votersLock.writeLock();
        l.lock();
        try {
            this.voters.add(voter);
        }
        finally {
            l.unlock();
        }
    }


    protected synchronized void unbindVoter ( DestinationAccessVoter voter ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Remove voter " + voter.getClass().getName()); //$NON-NLS-1$
        }
        Lock l = this.votersLock.writeLock();
        l.lock();
        try {
            this.voters.remove(voter);
        }
        finally {
            l.unlock();
        }
    }


    /**
     * @param connContext
     * @param context
     * @param dest
     * @param access
     * @return whether access should be allowed
     */
    public boolean decide ( ConnectionContext connContext, SecurityContext context, ActiveMQDestination dest, DestinationAccess access ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Check access %s for %s on %s", access.name(), context.getUserName(), dest.getQualifiedName())); //$NON-NLS-1$
        }

        if ( dest.isComposite() ) {
            for ( ActiveMQDestination compDest : dest.getCompositeDestinations() ) {
                if ( !this.decide(connContext, context, compDest, access) ) {
                    return false;
                }
            }
            return true;
        }

        return decideSimple(connContext, context, dest, access);
    }


    /**
     * @param connContext
     * @param context
     * @param dest
     * @param access
     * @return
     */
    private boolean decideSimple ( ConnectionContext connContext, SecurityContext context, ActiveMQDestination dest, DestinationAccess access ) {
        log.trace("Voters:"); //$NON-NLS-1$
        Lock l = this.votersLock.readLock();
        l.lock();
        try {
            for ( DestinationAccessVoter v : this.voters ) {
                DestinationAccessVote vote = v.vote(connContext, context, dest, access);

                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("%s by %s", vote.name(), v.getClass().getName())); //$NON-NLS-1$
                }

                switch ( vote ) {
                case NEUTRAL:
                    continue;
                case NEGATIVE:
                    return false;
                case POSITIVE:
                    return true;
                case UNSPECIFIED:
                default:
                    throw new SecurityException("Voter failed to return a proper result:" + v.getClass().getName()); //$NON-NLS-1$
                }
            }
            return false;
        }
        finally {
            l.unlock();
        }
    }
}
