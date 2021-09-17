/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return default host config impl
     */
    public HostConfiguration createHostConfiguration () {
        return new HostConfigurationImpl();
    }


    /**
     * 
     * @return the default impl
     */
    public HostIdentification createHostIdentification () {
        return new HostIdentificationImpl();
    }
}
