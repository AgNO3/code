/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.List;
import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareSecurityPolicyConfig.class )
public interface FileshareSecurityPolicyConfigMutable extends FileshareSecurityPolicyConfig {

    /**
     * 
     * @param defaultSharePasswordBits
     */
    void setDefaultSharePasswordBits ( Integer defaultSharePasswordBits );


    /**
     * 
     * @param userLabelRules
     */
    void setUserLabelRules ( List<FileshareUserLabelRule> userLabelRules );


    /**
     * 
     * @param defaultRootPolicy
     */
    void setDefaultRootLabel ( String defaultRootPolicy );


    /**
     * 
     * @param defaultEntityPolicy
     */
    void setDefaultEntityLabel ( String defaultEntityPolicy );


    /**
     * 
     * @param policies
     */
    void setPolicies ( Set<FileshareSecurityPolicy> policies );

}
