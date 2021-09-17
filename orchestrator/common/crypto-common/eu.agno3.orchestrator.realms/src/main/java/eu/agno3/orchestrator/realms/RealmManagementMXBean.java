/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


import java.util.List;

import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
public interface RealmManagementMXBean {

    /**
     * @return the known realms
     * @throws RealmManagementException
     */
    List<RealmInfo> getRealms () throws RealmManagementException;


    /**
     * @param realm
     * @return the realm info, or null if it does not exist
     * @throws RealmManagementException
     */
    RealmInfo getRealm ( String realm ) throws RealmManagementException;


    /**
     * @param realm
     * @param type
     * @param keytab
     * @param keys
     * @throws RealmManagementException
     */
    void addKeys ( String realm, RealmType type, String keytab, List<KeyData> keys ) throws RealmManagementException;


    /**
     * @param realm
     * @param type
     * @param keytab
     * @param keys
     * @throws RealmManagementException
     */
    void removeKeys ( String realm, RealmType type, String keytab, List<KeyInfo> keys ) throws RealmManagementException;


    /**
     * @param realm
     * @param type
     * @param keytab
     * @param initialKeys
     * @throws RealmManagementException
     */
    void createKeytab ( String realm, RealmType type, String keytab, List<KeyData> initialKeys ) throws RealmManagementException;


    /**
     * @param realm
     * @param type
     * @param keytab
     * @throws RealmManagementException
     */
    void deleteKeytab ( String realm, RealmType type, String keytab ) throws RealmManagementException;


    /**
     * @param realm
     * @param creds
     * @param user
     * @param password
     * @throws RealmManagementException
     */
    void joinAD ( String realm, WrappedCredentials creds ) throws RealmManagementException;


    /**
     * @param realm
     * @param machinePassword
     *            initial machine password, use default if null
     * @throws RealmManagementException
     */
    void joinADWithMachinePassword ( String realm, String machinePassword ) throws RealmManagementException;


    /**
     * @param realm
     * @throws RealmManagementException
     */
    void rekeyAD ( String realm ) throws RealmManagementException;


    /**
     * @param realm
     * @param creds
     * @param user
     * @param password
     * @throws RealmManagementException
     */
    void leaveAD ( String realm, WrappedCredentials creds ) throws RealmManagementException;

}
