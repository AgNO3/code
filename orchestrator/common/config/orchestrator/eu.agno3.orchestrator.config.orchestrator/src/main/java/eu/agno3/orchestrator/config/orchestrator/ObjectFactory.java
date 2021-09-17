/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * @return default impl
     */
    public OrchestratorConfiguration createOrchestratorConfiguration () {
        return new OrchestratorConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public OrchestratorEventLogConfiguration createOrchestratorEventLogConfiguration () {
        return new OrchestratorEventLogConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public OrchestratorWebConfiguration createOrchestratorWebConfiguration () {
        return new OrchestratorWebConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public OrchestratorAdvancedConfiguration createOrchestratorAdvancedConfiguration () {
        return new OrchestratorAdvancedConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public OrchestratorAuthenticationConfiguration createOrchestratorAuthenticationConfiguration () {
        return new OrchestratorAuthenticationConfigurationImpl();
    }
}
