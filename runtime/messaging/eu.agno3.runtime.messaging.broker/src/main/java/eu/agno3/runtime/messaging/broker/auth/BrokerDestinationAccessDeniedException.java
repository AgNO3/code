/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth;


import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;


/**
 * @author mbechler
 * 
 */
public class BrokerDestinationAccessDeniedException extends SecurityException {

    private final SecurityContext secContext;
    private final ActiveMQDestination destination;
    private final DestinationAccess requiredPerm;

    /**
     * 
     */
    private static final long serialVersionUID = 762351828101788206L;


    /**
     * @param secContext
     * @param dest
     * @param perm
     */
    public BrokerDestinationAccessDeniedException ( SecurityContext secContext, ActiveMQDestination dest, DestinationAccess perm ) {
        super(String.format("Destination access denied (%s on %s for %s)", perm.name(), dest.getPhysicalName(), secContext.getUserName())); //$NON-NLS-1$
        this.secContext = secContext;
        this.destination = dest;
        this.requiredPerm = perm;
    }


    /**
     * @return the secContext
     */
    public SecurityContext getSecContext () {
        return this.secContext;
    }


    /**
     * @return the destination
     */
    public ActiveMQDestination getDestination () {
        return this.destination;
    }


    /**
     * @return the requiredPerm
     */
    public DestinationAccess getRequiredPerm () {
        return this.requiredPerm;
    }
}
