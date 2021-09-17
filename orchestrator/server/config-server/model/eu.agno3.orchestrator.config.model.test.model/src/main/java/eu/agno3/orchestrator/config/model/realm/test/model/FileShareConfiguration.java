/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import eu.agno3.orchestrator.config.model.base.config.ImmutableType;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare" )
@ImmutableType ( FileShareConfiguration.class )
public interface FileShareConfiguration extends ConfigurationInstance {

    /**
     * @return the authenticators
     */
    @ReferencedObject
    AuthenticatorCollection getAuthenticators ();

}