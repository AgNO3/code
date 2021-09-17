/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;




/**
 * @author mbechler
 *
 */
public interface DefaultRealmServicesContext {

    /**
     * @return configuration service
     */
    ConfigurationServerService getConfigurationService ();


    /**
     * @return instance service
     */
    InstanceServerService getInstanceService ();


    /**
     * @return service service
     */
    ServiceServerService getServiceService ();


    /**
     * @return structural object service
     */
    StructuralObjectServerService getStructureService ();


    /**
     * @return inheritance service
     */
    InheritanceServerService getInheritanceService ();

}
