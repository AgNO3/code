/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.web;


import java.util.List;
import java.util.Set;

import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;


/**
 * @author mbechler
 *
 */
public interface SSLConfigContext {

    /**
     * @param outer
     * @return the suggested subject
     */
    String getSubject ( OuterWrapper<?> outer );


    /**
     * @param outer
     * @return the suggested SANs
     */
    List<String> getSANs ( OuterWrapper<?> outer );


    /**
     * @param outer
     * @return the suggested extended key usage
     */
    Set<String> getEKUs ( OuterWrapper<?> outer );


    /**
     * @param outer
     * @return the suggested key usage
     */
    Set<String> getKeyUsage ( OuterWrapper<?> outer );

}
