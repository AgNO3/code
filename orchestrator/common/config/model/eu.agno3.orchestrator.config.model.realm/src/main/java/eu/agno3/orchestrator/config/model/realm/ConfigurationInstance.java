/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


/**
 * @author mbechler
 * 
 */
public interface ConfigurationInstance extends ConfigurationObject {

    /**
     * @return the service that this configuration instance is attached to
     */
    ServiceStructuralObject getForService ();

}
