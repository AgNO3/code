/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.05.2014 by mbechler
 */
package eu.agno3.runtime.cdi;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @author mbechler
 * 
 */
public class CDICacheEntry {

    private Set<String> beanClasses = new HashSet<>();
    private Map<String, Set<String>> classAnnotations = new HashMap<>();
    private long lastModified;


    /**
     * 
     * @param lastModified
     */
    public CDICacheEntry ( long lastModified ) {
        this.lastModified = lastModified;
    }


    /**
     * @return the beanClasses
     */
    public Set<String> getBeanClasses () {
        return this.beanClasses;
    }


    /**
     * @return the classAnnotations
     */
    public Map<String, Set<String>> getClassAnnotations () {
        return this.classAnnotations;
    }


    /**
     * @return the lastModified
     */
    public long getLastModified () {
        return this.lastModified;
    }

}
