/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web;


import javax.servlet.Filter;


/**
 * @author mbechler
 * 
 */
public interface SecurityFilter extends Filter {

    /**
     * @return filter name for security filter config
     */
    String getFilterName ();
}
