/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.versioning;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return default version info implementation
     */
    public VersionInfo createVersionInfo () {
        return new VersionInfoImpl();
    }
}
