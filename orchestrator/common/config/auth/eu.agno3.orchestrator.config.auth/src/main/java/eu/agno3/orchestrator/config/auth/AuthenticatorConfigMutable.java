/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


/**
 * @author mbechler
 *
 */
public interface AuthenticatorConfigMutable extends AuthenticatorConfig {

    /**
     * 
     * @param realm
     */
    void setRealm ( String realm );
}
