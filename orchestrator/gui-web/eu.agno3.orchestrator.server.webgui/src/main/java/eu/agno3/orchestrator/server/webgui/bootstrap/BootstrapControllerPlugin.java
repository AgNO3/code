/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 28, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.bootstrap;


import eu.agno3.orchestrator.bootstrap.BootstrapContext;


/**
 * @author mbechler
 *
 */
public interface BootstrapControllerPlugin {

    /**
     * @return id for this plugin
     */
    String getId ();


    /**
     * @return facelet template to include
     */
    String getIncludeTemplate ();


    /**
     * @param context
     */
    void contributeContext ( BootstrapContext context );


    /**
     * @return last step for this plugin
     * 
     */
    String getLastStep ();

}
