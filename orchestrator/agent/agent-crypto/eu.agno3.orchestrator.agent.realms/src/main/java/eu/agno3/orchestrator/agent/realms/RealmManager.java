/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
public interface RealmManager {

    /**
     * 
     * @return the realm name
     */
    public String getRealmName ();


    /**
     * @return whether the realm is configured
     */
    boolean exists ();


    /**
     * @param properties
     * @param defaultAllowedUsers
     * @throws KerberosException
     */
    void create ( Properties properties, Set<UserPrincipal> defaultAllowedUsers ) throws KerberosException;


    /**
     * 
     * @param properties
     * @param defaultAllowedUsers
     * @param set
     * @throws KerberosException
     */
    void updateConfig ( Properties properties, Set<UserPrincipal> defaultAllowedUsers ) throws KerberosException;


    /**
     * 
     * @return the realm properties
     * @throws KerberosException
     */
    Properties getConfig () throws KerberosException;


    /**
     * @throws KerberosException
     */
    void delete () throws KerberosException;


    /**
     * @return the known keytabs
     */
    Collection<String> listKeytabs ();


    /**
     * 
     * @param keyTabId
     * @return the manager for the created keytab
     * @throws KerberosException
     */
    KeyTabManager createKeytab ( String keyTabId ) throws KerberosException;


    /**
     * @param keyTabId
     * @return a keytab manager for the given keytab
     * @throws KerberosException
     */
    KeyTabManager getKeytabManager ( String keyTabId ) throws KerberosException;


    /**
     * @return the users that are allowed to access this keystore
     * @throws UnixAccountException
     * @throws KerberosException
     */
    Set<UserPrincipal> getAllowedUsers () throws UnixAccountException, KerberosException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    void allowUser ( UserPrincipal user ) throws UnixAccountException, KerberosException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    void revokeUser ( UserPrincipal user ) throws UnixAccountException, KerberosException;


    /**
     * @return the type
     */
    RealmType getType ();


    /**
     * @return the realmPath
     */
    Path getRealmPath ();


    /**
     * @throws KerberosException
     * 
     */
    void runMaintenance () throws KerberosException;


    /**
     * @throws KerberosException
     */
    void notifyServices () throws KerberosException;

}