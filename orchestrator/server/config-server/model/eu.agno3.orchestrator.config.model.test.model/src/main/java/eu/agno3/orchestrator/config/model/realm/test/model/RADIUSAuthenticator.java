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
@ObjectTypeName ( "urn:agno3:objects:1.0:authenticator:radius" )
@ImmutableType ( RADIUSAuthenticator.class )
public interface RADIUSAuthenticator extends AuthenticatorMutable {

    /**
     * @return the radius1
     */
    String getRadius1 ();


    /**
     * @return the radius2
     */
    String getRadius2 ();


    /**
     * @return the nasIp
     */
    String getNasIp ();


    /**
     * @return the nasSecret
     */
    String getNasSecret ();

}