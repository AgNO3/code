/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.predicates;


import java.util.Arrays;
import java.util.Collection;

import eu.agno3.orchestrator.system.base.execution.Predicate;


/**
 * @author mbechler
 * 
 */
public final class Predicates {

    /**
     * 
     */
    private Predicates () {}


    /**
     * 
     * @return a predicate that is always true
     */
    public static Predicate yes () {
        return new TruePredicate();
    }


    /**
     * 
     * @return a predicate that is always false
     */
    public static Predicate no () {
        return new FalsePredicate();
    }


    /**
     * 
     * @param p
     * @return a predicate that is true iff another is false
     */
    public static Predicate not ( Predicate p ) {
        return new NotPredicate(p);
    }


    /**
     * 
     * @param p
     * @return a predicate is true iff all given predicates are true
     */
    public static Predicate all ( Predicate... p ) {
        return new AllPredicate(Arrays.asList(p));
    }


    /**
     * 
     * @param p
     * @return a predicate is true iff all given predicates are true
     */
    public static Predicate all ( Collection<Predicate> p ) {
        return new AllPredicate(p);
    }
}
