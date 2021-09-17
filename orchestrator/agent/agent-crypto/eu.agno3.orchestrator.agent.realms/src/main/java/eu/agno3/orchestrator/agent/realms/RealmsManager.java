/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.util.Map;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
public interface RealmsManager extends SystemService {

    /**
     * @return the known realm names
     * @throws KerberosException
     */
    Map<String, RealmType> listRealms () throws KerberosException;


    /**
     * @param realmName
     * @return whether a realm with this name exists
     */
    boolean exists ( String realmName );


    /**
     * @param realm
     * @param type
     * @return the manager for the given realm
     * @throws KerberosException
     */
    RealmManager getRealmManager ( String realm, RealmType type ) throws KerberosException;


    /**
     * 
     * @param realm
     * @return the manager for the given realm
     * @throws KerberosException
     */
    RealmManager getRealmManager ( String realm ) throws KerberosException;


    /**
     * @param realmName
     * @return the realms type
     * @throws KerberosException
     */
    RealmType getType ( String realmName ) throws KerberosException;


    /**
     * 
     */
    void runMaintenance ();

}
