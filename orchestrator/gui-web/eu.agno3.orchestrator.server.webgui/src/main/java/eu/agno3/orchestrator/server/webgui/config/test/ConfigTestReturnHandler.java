/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 15, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.test;


import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;


/**
 * @author mbechler
 *
 */
public interface ConfigTestReturnHandler {

    /**
     * @param ctx
     * @param inter
     */
    void interact ( OuterWrapper<?> ctx, ConfigTestInteraction inter );

}
