/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.config;


/**
 * @author mbechler
 * 
 */
public interface FilterChainEntry extends Comparable<FilterChainEntry> {

    /**
     * @return the priority of this entry, higher priority comes first
     */
    int getPriority ();


    /**
     * 
     * @return the path this entry applies to
     */
    String getPath ();


    /**
     * 
     * @return the shiro filter chain specification
     */
    String getChainDefinition ();

}
