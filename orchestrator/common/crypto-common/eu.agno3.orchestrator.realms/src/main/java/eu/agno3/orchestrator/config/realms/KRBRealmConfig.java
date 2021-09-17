/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.List;

import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.types.net.validation.ValidHostOrAddress;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:realms:krb" )
public interface KRBRealmConfig extends RealmConfig {

    /**
     * 
     * @return static kdcs to use for this realm
     */
    @ValidHostOrAddress
    List<String> getKdcs ();


    /**
     * 
     * @return kpasswd server
     */
    @ValidHostOrAddress
    String getKpasswdServer ();


    /**
     * 
     * @return administration server
     */
    @ValidHostOrAddress
    String getAdminServer ();

}
