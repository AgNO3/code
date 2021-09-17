/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.util.Collection;
import java.util.Collections;

import eu.agno3.fileshare.model.PolicyViolation;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class CollectionResult <T> {

    private Collection<T> collection;
    private int hiddenPolicy;
    private Collection<PolicyViolation> violations;


    /**
     * @param collection
     * @param hiddenPolicy
     * @param violations
     * 
     */
    public CollectionResult ( Collection<T> collection, int hiddenPolicy, Collection<PolicyViolation> violations ) {
        this.collection = collection;
        this.hiddenPolicy = hiddenPolicy;
        this.violations = violations;
    }


    /**
     * 
     */
    public CollectionResult () {
        this.collection = Collections.EMPTY_LIST;
        this.violations = Collections.EMPTY_LIST;
        this.hiddenPolicy = 0;
    }


    /**
     * @return the returned items
     */
    public Collection<T> getCollection () {
        return this.collection;
    }


    /**
     * @return the number of files hidden by policy
     */
    public int getNumHiddenPolicy () {
        return this.hiddenPolicy;
    }


    /**
     * @return the violations
     */
    public Collection<PolicyViolation> getViolations () {
        return this.violations;
    }

}
