/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.policy;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import eu.agno3.fileshare.model.PolicyViolation;


/**
 * @author mbechler
 *
 */
@SessionScoped
public class PolicyCache implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6958032841759447235L;
    private Map<String, PolicyViolation> cache = new HashMap<>();


    /**
     * @return the cache
     */
    public Map<String, PolicyViolation> getCache () {
        return this.cache;
    }
}
