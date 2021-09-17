/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2013 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.session;


/**
 * @author mbechler
 * 
 */
public interface ConfigSessionFactory {

    /**
     * @return a new system session (only usable for local access)
     */
    ConfigSession createSession ();

}
