/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.predicates;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Predicate;


/**
 * @author mbechler
 * 
 */
final class TruePredicate implements Predicate {

    /**
     * 
     */
    private static final long serialVersionUID = 3180152885874897811L;


    @Override
    public boolean evaluate ( Context context ) {
        return true;
    }
}