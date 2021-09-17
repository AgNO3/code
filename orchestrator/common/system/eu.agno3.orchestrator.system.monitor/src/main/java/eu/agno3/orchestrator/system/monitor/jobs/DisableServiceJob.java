/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.03.2016 by mbechler
 */
package eu.agno3.orchestrator.system.monitor.jobs;


import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;


/**
 * @author mbechler
 *
 */
public class DisableServiceJob extends AbstractServiceJob {

    /**
     * 
     */
    private static final long serialVersionUID = 6058826328309101956L;


    /**
     * 
     */
    public DisableServiceJob () {
        super();
    }


    /**
     * 
     * @param service
     */
    public DisableServiceJob ( ServiceStructuralObject service ) {
        super(service);
    }
}
