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
final class NotPredicate implements Predicate {

    /**
     * 
     */
    private static final long serialVersionUID = -4033832215904061675L;
    private Predicate p;


    /**
     * @param p
     */
    public NotPredicate ( Predicate p ) {
        this.p = p;
    }


    @Override
    public boolean evaluate ( Context context ) {
        return !this.p.evaluate(context);
    }
}