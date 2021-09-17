/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.predicates;


import java.util.Collection;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Predicate;


/**
 * @author mbechler
 * 
 */
final class AllPredicate implements Predicate {

    /**
     * 
     */
    private static final long serialVersionUID = -3588958509823640254L;
    private Collection<Predicate> ps;


    /**
     * @param ps
     */
    public AllPredicate ( Collection<Predicate> ps ) {
        this.ps = ps;
    }


    @Override
    public boolean evaluate ( Context context ) {
        boolean res = true;
        for ( Predicate p : this.ps ) {
            res &= p.evaluate(context);
        }
        return res;
    }
}