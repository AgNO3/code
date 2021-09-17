/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 *
 */
public interface JobFailureHandler {

    /**
     * 
     * @param ctx
     * @param ph
     */
    public void failed ( Context ctx, Phase ph );
}
