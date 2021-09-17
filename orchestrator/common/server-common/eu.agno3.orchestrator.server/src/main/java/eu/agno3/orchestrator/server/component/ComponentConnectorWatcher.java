/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component;


import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;


/**
 * @author mbechler
 * 
 */
public interface ComponentConnectorWatcher {

    /**
     * @param componentId
     * @return the compnents's connector state
     */
    ComponentState getComponentConnectorState ( @NonNull UUID componentId );


    /**
     * @return a set of recently active component ids
     */
    Set<@NonNull UUID> getActiveComponentIds ();


    /**
     * 
     * @param componentId
     * @return the last time the client pinged
     */
    DateTime getLastPing ( @NonNull UUID componentId );
}
