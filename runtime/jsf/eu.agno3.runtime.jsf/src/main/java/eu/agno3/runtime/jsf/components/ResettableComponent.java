/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components;


/**
 * @author mbechler
 *
 */
public interface ResettableComponent {

    /**
     * Reset component state
     * 
     * @return whether to handle children
     */
    boolean resetComponent ();
}
