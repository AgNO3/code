/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import eu.agno3.orchestrator.config.model.base.config.ImmutableType;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:authenticator:ad" )
@ImmutableType ( ADAuthenticator.class )
public interface ADAuthenticator extends AuthenticatorMutable {

    /**
     * @return the dc1
     */
    String getDc1 ();


    /**
     * @return the dc2
     */
    String getDc2 ();


    /**
     * @return the domain
     */
    String getDomain ();

}