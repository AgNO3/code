/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


/**
 * @author mbechler
 * 
 */
public interface ConfigurationObjectMutable extends ConfigurationObject {

    /**
     * @param name
     */
    void setName ( String name );


    /**
     * @param inherits
     */
    void setInherits ( ConfigurationObject inherits );

}
